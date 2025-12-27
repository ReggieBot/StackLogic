package com.stacklogic.controller;

import com.stacklogic.model.Action;
import com.stacklogic.model.Hand;
import com.stacklogic.model.Position;
import com.stacklogic.model.Range;
import com.stacklogic.util.RangeLoader;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.CacheHint;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Preflop Range Viewer.
 *
 * This controller:
 * 1. Creates the 13x13 hand grid dynamically
 * 2. Handles position/table size/situation selection
 * 3. Loads and displays ranges from JSON files
 * 4. Updates the grid colors based on the current range
 */
public class RangeController implements Initializable {

    // FXML-injected UI components
    @FXML private ComboBox<Position> positionCombo;
    @FXML private ComboBox<String> tableSizeCombo;
    @FXML private ComboBox<String> situationCombo;
    @FXML private Button nextPositionBtn;
    @FXML private GridPane handGrid;
    @FXML private Label rangeNameLabel;
    @FXML private Label statsLabel;

    // Store references to grid cells for easy updating
    // gridCells[row][col] gives us the label at that position
    private Label[][] gridCells = new Label[13][13];

    // Current state
    private int currentTableSize = 6;
    private Range currentRange = null;

    /**
     * Initialize the controller after FXML loads.
     */
    @Override
    public void initialize(URL url, ResourceBundle resources) {
        // Build the 13x13 grid
        createHandGrid();

        // Set up table size options
        tableSizeCombo.setItems(FXCollections.observableArrayList(
            "6-max", "5-max", "4-max", "3-max", "Heads Up"
        ));
        tableSizeCombo.setValue("6-max");

        // Set up situation options (we'll update this when position changes)
        updateSituationOptions();

        // Set up position combo (filtered by table size)
        updatePositionCombo();

        // Add listeners for when selections change
        tableSizeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateTableSize(newVal);
        });

        positionCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateSituationOptions();
                loadAndDisplayRange();
            }
        });

        situationCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadAndDisplayRange();
            }
        });

        // Select defaults
        positionCombo.getSelectionModel().selectFirst();
        situationCombo.getSelectionModel().selectFirst();
    }

    /**
     * Create the 13x13 grid of hand labels.
     * This is done in code because creating 169 labels in FXML would be tedious.
     *
     * PERFORMANCE NOTE: We enable caching on each cell to improve resize performance.
     * Caching tells JavaFX to render the node to an image and reuse it, rather than
     * re-rendering from scratch each frame. This significantly improves performance
     * when the window is resized.
     */
    private void createHandGrid() {
        // Enable caching on the entire grid for better resize performance
        handGrid.setCache(true);
        handGrid.setCacheHint(CacheHint.SPEED);

        for (int row = 0; row < 13; row++) {
            for (int col = 0; col < 13; col++) {
                // Get the hand notation for this cell
                String handNotation = Hand.getNotation(row, col);

                // Create a label for this cell
                Label cell = new Label(handNotation);
                cell.getStyleClass().addAll("hand-cell", "hand-fold");

                // Enable caching on each cell for better performance during resize
                // CacheHint.SPEED tells JavaFX to prioritize speed over quality
                cell.setCache(true);
                cell.setCacheHint(CacheHint.SPEED);

                // Store reference for later updates
                gridCells[row][col] = cell;

                // Add to grid (GridPane uses column, row order!)
                handGrid.add(cell, col, row);
            }
        }
    }

    /**
     * Update the position combo box based on current table size.
     * Some positions don't exist at smaller table sizes.
     */
    private void updatePositionCombo() {
        List<Position> validPositions = new ArrayList<>();

        for (Position pos : Position.values()) {
            if (pos.existsAtTableSize(currentTableSize)) {
                validPositions.add(pos);
            }
        }

        Position currentSelection = positionCombo.getValue();
        positionCombo.setItems(FXCollections.observableArrayList(validPositions));

        // Try to keep current selection if still valid
        if (currentSelection != null && validPositions.contains(currentSelection)) {
            positionCombo.setValue(currentSelection);
        } else {
            positionCombo.getSelectionModel().selectFirst();
        }
    }

    /**
     * Update situation options based on selected position.
     * RFI is always available. "vs [position]" options depend on who can raise before you.
     */
    private void updateSituationOptions() {
        Position myPosition = positionCombo.getValue();
        if (myPosition == null) return;

        List<String> situations = new ArrayList<>();
        situations.add("RFI (Open Raise)");

        // Add "vs [position]" for each position that acts before us
        for (Position pos : Position.values()) {
            if (pos.existsAtTableSize(currentTableSize) && pos.ordinal() < myPosition.ordinal()) {
                situations.add("vs " + pos.getDisplayName() + " Raise");
            }
        }

        String currentSelection = situationCombo.getValue();
        situationCombo.setItems(FXCollections.observableArrayList(situations));

        if (currentSelection != null && situations.contains(currentSelection)) {
            situationCombo.setValue(currentSelection);
        } else {
            situationCombo.getSelectionModel().selectFirst();
        }
    }

    /**
     * Handle table size changes.
     */
    private void updateTableSize(String sizeStr) {
        if (sizeStr == null) return;

        // Parse table size from string
        switch (sizeStr) {
            case "6-max" -> currentTableSize = 6;
            case "5-max" -> currentTableSize = 5;
            case "4-max" -> currentTableSize = 4;
            case "3-max" -> currentTableSize = 3;
            case "Heads Up" -> currentTableSize = 2;
        }

        // Update available positions
        updatePositionCombo();
    }

    /**
     * Load and display the range for the current selection.
     */
    private void loadAndDisplayRange() {
        Position position = positionCombo.getValue();
        String situation = situationCombo.getValue();

        if (position == null || situation == null) {
            return;
        }

        Range range = null;

        if (situation.startsWith("RFI")) {
            // Load RFI range
            range = RangeLoader.loadRfiRange(position);
        } else if (situation.startsWith("vs ")) {
            // Parse the raiser position from "vs UTG Raise" -> "UTG"
            String raiserStr = situation.replace("vs ", "").replace(" Raise", "");
            try {
                Position raiser = Position.valueOf(raiserStr.toUpperCase());
                range = RangeLoader.loadVsRaiseRange(position, raiser);
            } catch (IllegalArgumentException e) {
                System.err.println("Unknown position: " + raiserStr);
            }
        }

        // If no range file exists, create empty range
        if (range == null) {
            range = RangeLoader.createEmptyRange(position, situation);
        }

        currentRange = range;
        updateGridDisplay();
    }

    /**
     * Update all grid cells to reflect the current range.
     */
    private void updateGridDisplay() {
        if (currentRange == null) {
            rangeNameLabel.setText("No range loaded");
            statsLabel.setText("");
            return;
        }

        // Update header
        rangeNameLabel.setText(currentRange.getName());
        statsLabel.setText(String.format("Playing %.1f%% of hands (%d/169)",
                currentRange.getPlayPercentage(),
                currentRange.getHandCount()));

        // Update each cell
        for (int row = 0; row < 13; row++) {
            for (int col = 0; col < 13; col++) {
                Label cell = gridCells[row][col];
                Action action = currentRange.getHandAction(row, col);

                // Remove old action class and add new one
                cell.getStyleClass().removeAll("hand-raise", "hand-call", "hand-fold", "hand-3bet");
                cell.getStyleClass().add(action.getCssClass());
            }
        }
    }

    /**
     * Handle "Next Position" button click.
     * Cycles to the next position at the current table size.
     */
    @FXML
    private void handleNextPosition() {
        Position current = positionCombo.getValue();
        if (current != null) {
            Position next = current.nextPosition(currentTableSize);
            positionCombo.setValue(next);
        }
    }
}
