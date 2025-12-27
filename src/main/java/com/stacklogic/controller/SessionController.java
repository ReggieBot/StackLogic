package com.stacklogic.controller;

import com.stacklogic.model.Session;
import com.stacklogic.util.SessionDAO;
import com.stacklogic.util.SessionDAO.SessionStats;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Session Tracker.
 *
 * Handles:
 * - Adding new sessions
 * - Editing existing sessions
 * - Deleting sessions
 * - Displaying session history in a table
 * - Calculating and displaying stats
 */
public class SessionController implements Initializable {

    // Form fields
    @FXML private DatePicker datePicker;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private TextField durationField;
    @FXML private ComboBox<String> stakesCombo;
    @FXML private ComboBox<String> tableSizeCombo;
    @FXML private TextField handsField;
    @FXML private TextField buyInField;
    @FXML private TextField cashOutField;
    @FXML private Label profitLabel;
    @FXML private TextField bbWonField;
    @FXML private ToggleButton tagAGame;
    @FXML private ToggleButton tagTilted;
    @FXML private ToggleButton tagTired;
    @FXML private ToggleButton tagRanBad;
    @FXML private ToggleButton tagRanGood;
    @FXML private TextArea notesArea;

    // Buttons
    @FXML private Button saveButton;
    @FXML private Button deleteButton;

    // Stats labels
    @FXML private Label statSessionCount;
    @FXML private Label statTotalProfit;
    @FXML private Label statHourlyRate;
    @FXML private Label statHoursPlayed;

    // Table
    @FXML private TableView<Session> sessionTable;
    @FXML private TableColumn<Session, String> colDate;
    @FXML private TableColumn<Session, String> colStakes;
    @FXML private TableColumn<Session, String> colDuration;
    @FXML private TableColumn<Session, String> colBuyIn;
    @FXML private TableColumn<Session, String> colCashOut;
    @FXML private TableColumn<Session, String> colProfit;
    @FXML private TableColumn<Session, String> colTags;

    // Data
    private ObservableList<Session> sessions = FXCollections.observableArrayList();
    private Session editingSession = null;  // null = creating new, not null = editing

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        // Set up combo boxes
        stakesCombo.setItems(FXCollections.observableArrayList(
            "2NL", "5NL", "10NL", "25NL", "50NL", "100NL"
        ));
        stakesCombo.setValue("2NL");

        tableSizeCombo.setItems(FXCollections.observableArrayList(
            "6-max", "4-max", "Heads Up"
        ));
        tableSizeCombo.setValue("6-max");

        // Set default date to today
        datePicker.setValue(LocalDate.now());

        // Auto-calculate profit when buy-in or cash-out changes
        buyInField.textProperty().addListener((obs, o, n) -> updateProfitDisplay());
        cashOutField.textProperty().addListener((obs, o, n) -> updateProfitDisplay());

        // Auto-calculate duration when start/end times change
        startTimeField.textProperty().addListener((obs, o, n) -> autoCalculateDuration());
        endTimeField.textProperty().addListener((obs, o, n) -> autoCalculateDuration());

        // Set up table columns
        setupTableColumns();

        // Load data from database
        loadSessions();

        // Handle table row selection for editing
        sessionTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    loadSessionForEditing(newSelection);
                }
            }
        );
    }

    /**
     * Set up how each table column gets its data.
     */
    private void setupTableColumns() {
        // Date column
        colDate.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getDate().toString()));

        // Stakes column
        colStakes.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStakes()));

        // Duration column (formatted)
        colDuration.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getDurationFormatted()));

        // Buy-in column (formatted as currency)
        colBuyIn.setCellValueFactory(data ->
            new SimpleStringProperty(String.format("$%.2f", data.getValue().getBuyIn())));

        // Cash-out column
        colCashOut.setCellValueFactory(data ->
            new SimpleStringProperty(String.format("$%.2f", data.getValue().getCashOut())));

        // Profit column with color
        colProfit.setCellValueFactory(data ->
            new SimpleStringProperty(String.format("$%.2f", data.getValue().getProfit())));

        colProfit.setCellFactory(column -> new TableCell<Session, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Parse the value to determine color
                    double value = Double.parseDouble(item.replace("$", ""));
                    if (value > 0) {
                        setStyle("-fx-text-fill: #4ade80;");  // Green
                    } else if (value < 0) {
                        setStyle("-fx-text-fill: #f87171;");  // Red
                    } else {
                        setStyle("-fx-text-fill: #eaeaea;");  // Neutral
                    }
                }
            }
        });

        // Tags column
        colTags.setCellValueFactory(data -> {
            String tags = data.getValue().getTags();
            return new SimpleStringProperty(tags != null ? tags : "");
        });

        // Bind data to table
        sessionTable.setItems(sessions);
    }

    /**
     * Load all sessions from database.
     */
    private void loadSessions() {
        try {
            sessions.clear();
            sessions.addAll(SessionDAO.getAll());
            updateStats();
        } catch (SQLException e) {
            showError("Failed to load sessions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update the stats summary.
     */
    private void updateStats() {
        try {
            SessionStats stats = SessionDAO.getStats();

            statSessionCount.setText(String.valueOf(stats.sessionCount));

            statTotalProfit.setText(String.format("$%.2f", stats.totalProfit));
            statTotalProfit.getStyleClass().removeAll("profit-positive", "profit-negative");
            if (stats.totalProfit > 0) {
                statTotalProfit.getStyleClass().add("profit-positive");
            } else if (stats.totalProfit < 0) {
                statTotalProfit.getStyleClass().add("profit-negative");
            }

            statHourlyRate.setText(String.format("$%.2f/hr", stats.getHourlyRate()));

            int hours = stats.totalMinutes / 60;
            int mins = stats.totalMinutes % 60;
            statHoursPlayed.setText(String.format("%dh %dm", hours, mins));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the profit display as user types.
     */
    private void updateProfitDisplay() {
        try {
            double buyIn = buyInField.getText().isEmpty() ? 0 : Double.parseDouble(buyInField.getText());
            double cashOut = cashOutField.getText().isEmpty() ? 0 : Double.parseDouble(cashOutField.getText());
            double profit = cashOut - buyIn;

            profitLabel.setText(String.format("$%.2f", profit));
            profitLabel.getStyleClass().removeAll("profit-positive", "profit-negative", "profit-neutral");

            if (profit > 0) {
                profitLabel.getStyleClass().add("profit-positive");
            } else if (profit < 0) {
                profitLabel.getStyleClass().add("profit-negative");
            } else {
                profitLabel.getStyleClass().add("profit-neutral");
            }
        } catch (NumberFormatException e) {
            profitLabel.setText("$0.00");
        }
    }

    /**
     * Auto-calculate duration from start and end times.
     */
    private void autoCalculateDuration() {
        try {
            if (!startTimeField.getText().isEmpty() && !endTimeField.getText().isEmpty()) {
                LocalTime start = LocalTime.parse(startTimeField.getText());
                LocalTime end = LocalTime.parse(endTimeField.getText());

                int startMins = start.getHour() * 60 + start.getMinute();
                int endMins = end.getHour() * 60 + end.getMinute();

                // Handle crossing midnight
                if (endMins < startMins) {
                    endMins += 24 * 60;
                }

                durationField.setText(String.valueOf(endMins - startMins));
            }
        } catch (DateTimeParseException e) {
            // Invalid time format, ignore
        }
    }

    /**
     * Load a session into the form for editing.
     */
    private void loadSessionForEditing(Session session) {
        editingSession = session;

        datePicker.setValue(session.getDate());
        startTimeField.setText(session.getStartTime() != null ? session.getStartTime().toString() : "");
        endTimeField.setText(session.getEndTime() != null ? session.getEndTime().toString() : "");
        durationField.setText(session.getDurationMinutes() != null ? session.getDurationMinutes().toString() : "");
        stakesCombo.setValue(session.getStakes());
        tableSizeCombo.setValue(session.getTableSize());
        handsField.setText(session.getHandsPlayed() != null ? session.getHandsPlayed().toString() : "");
        buyInField.setText(String.format("%.2f", session.getBuyIn()));
        cashOutField.setText(String.format("%.2f", session.getCashOut()));
        bbWonField.setText(session.getBbWon() != null ? session.getBbWon().toString() : "");
        notesArea.setText(session.getNotes() != null ? session.getNotes() : "");

        // Set tags
        String tags = session.getTags() != null ? session.getTags() : "";
        tagAGame.setSelected(tags.contains("A-game"));
        tagTilted.setSelected(tags.contains("Tilted"));
        tagTired.setSelected(tags.contains("Tired"));
        tagRanBad.setSelected(tags.contains("Ran Bad"));
        tagRanGood.setSelected(tags.contains("Ran Good"));

        // Show delete button, change save button text
        saveButton.setText("Update Session");
        deleteButton.setVisible(true);
        deleteButton.setManaged(true);
    }

    /**
     * Handle save button click.
     */
    @FXML
    private void handleSave() {
        try {
            // Validate required fields
            if (datePicker.getValue() == null) {
                showError("Please select a date");
                return;
            }
            if (buyInField.getText().isEmpty() || cashOutField.getText().isEmpty()) {
                showError("Please enter buy-in and cash-out amounts");
                return;
            }

            // Create or update session
            Session session = editingSession != null ? editingSession : new Session();

            session.setDate(datePicker.getValue());

            // Parse optional times
            if (!startTimeField.getText().isEmpty()) {
                session.setStartTime(LocalTime.parse(startTimeField.getText()));
            }
            if (!endTimeField.getText().isEmpty()) {
                session.setEndTime(LocalTime.parse(endTimeField.getText()));
            }

            // Parse optional duration
            if (!durationField.getText().isEmpty()) {
                session.setDurationMinutes(Integer.parseInt(durationField.getText()));
            }

            session.setStakes(stakesCombo.getValue());
            session.setTableSize(tableSizeCombo.getValue());

            // Parse optional hands
            if (!handsField.getText().isEmpty()) {
                session.setHandsPlayed(Integer.parseInt(handsField.getText()));
            }

            session.setBuyIn(Double.parseDouble(buyInField.getText()));
            session.setCashOut(Double.parseDouble(cashOutField.getText()));

            // Parse optional BB won
            if (!bbWonField.getText().isEmpty()) {
                session.setBbWon(Double.parseDouble(bbWonField.getText()));
            }

            // Collect tags
            StringBuilder tags = new StringBuilder();
            if (tagAGame.isSelected()) tags.append("A-game,");
            if (tagTilted.isSelected()) tags.append("Tilted,");
            if (tagTired.isSelected()) tags.append("Tired,");
            if (tagRanBad.isSelected()) tags.append("Ran Bad,");
            if (tagRanGood.isSelected()) tags.append("Ran Good,");

            if (tags.length() > 0) {
                session.setTags(tags.substring(0, tags.length() - 1));  // Remove trailing comma
            }

            session.setNotes(notesArea.getText());

            // Save to database
            if (editingSession != null) {
                SessionDAO.update(session);
            } else {
                SessionDAO.create(session);
            }

            // Calculate the session result for review
            double profit = session.getProfit();
            boolean wasWinningSession = profit > 0;
            boolean wasLosingSession = profit < 0;

            // Refresh and clear form
            loadSessions();
            handleClear();

            // Show session review prompts for reflection
            showSessionReview(session, wasWinningSession, wasLosingSession);

        } catch (NumberFormatException e) {
            showError("Please enter valid numbers");
        } catch (DateTimeParseException e) {
            showError("Please enter valid times (HH:MM format)");
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle clear button click.
     */
    @FXML
    private void handleClear() {
        editingSession = null;

        datePicker.setValue(LocalDate.now());
        startTimeField.clear();
        endTimeField.clear();
        durationField.clear();
        stakesCombo.setValue("2NL");
        tableSizeCombo.setValue("6-max");
        handsField.clear();
        buyInField.clear();
        cashOutField.clear();
        bbWonField.clear();
        notesArea.clear();

        tagAGame.setSelected(false);
        tagTilted.setSelected(false);
        tagTired.setSelected(false);
        tagRanBad.setSelected(false);
        tagRanGood.setSelected(false);

        profitLabel.setText("$0.00");
        profitLabel.getStyleClass().removeAll("profit-positive", "profit-negative");

        saveButton.setText("Save Session");
        deleteButton.setVisible(false);
        deleteButton.setManaged(false);

        sessionTable.getSelectionModel().clearSelection();
    }

    /**
     * Handle delete button click.
     */
    @FXML
    private void handleDelete() {
        if (editingSession == null) return;

        // Confirm deletion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Session");
        alert.setHeaderText("Are you sure you want to delete this session?");
        alert.setContentText("This action cannot be undone.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                SessionDAO.delete(editingSession.getId());
                loadSessions();
                handleClear();
            } catch (SQLException e) {
                showError("Failed to delete session: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Show an error alert.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show session review prompts after saving.
     * Helps the player reflect on their session and identify areas for improvement.
     */
    private void showSessionReview(Session session, boolean wasWinning, boolean wasLosing) {
        Alert reviewAlert = new Alert(Alert.AlertType.INFORMATION);
        reviewAlert.setTitle("Session Review");
        reviewAlert.setHeaderText(wasWinning ? "Great session! +$" + String.format("%.2f", session.getProfit())
                                             : wasLosing ? "Tough session. -$" + String.format("%.2f", Math.abs(session.getProfit()))
                                             : "Break-even session.");

        // Build review questions based on session context
        StringBuilder review = new StringBuilder();

        if (wasWinning) {
            review.append("Take a moment to reflect on what went well:\n\n");
            review.append("• Did you stick to your preflop ranges?\n");
            review.append("• Were you making value bets with your strong hands?\n");
            review.append("• Did you avoid tilting after bad beats?\n");
            review.append("• Did position influence your decisions correctly?\n\n");
            review.append("Remember: Results ≠ Quality of play. Review your biggest hands!");
        } else if (wasLosing) {
            review.append("Losing sessions happen. Reflect honestly:\n\n");
            review.append("• Did you make any calls you knew were bad?\n");
            review.append("• Were you playing too many hands OOP?\n");
            review.append("• Did you chase draws without proper odds?\n");
            review.append("• Did tilt affect your decisions after losses?\n");
            review.append("• Were you paying attention or distracted?\n\n");
            review.append("Tip: Save your biggest losing hands for later review!");
        } else {
            review.append("Break-even sessions can be valuable:\n\n");
            review.append("• Were there spots where you could have extracted more value?\n");
            review.append("• Did you miss any bluff opportunities?\n");
            review.append("• Were your bet sizes optimal?\n\n");
            review.append("Small adjustments can turn break-even into profit!");
        }

        // Add context-specific tips based on tags
        String tags = session.getTags() != null ? session.getTags() : "";

        if (tags.contains("Tilted")) {
            review.append("\n\n⚠️ You marked this session as TILTED.\n");
            review.append("Consider: What triggered it? How can you prevent it next time?\n");
            review.append("Maybe implement a stop-loss rule for future sessions.");
        }

        if (tags.contains("Tired")) {
            review.append("\n\n⚠️ You marked this session as TIRED.\n");
            review.append("Playing tired hurts your decision making. Consider shorter sessions");
            review.append(" or scheduling play when you're more alert.");
        }

        if (tags.contains("A-game")) {
            review.append("\n\n✓ You felt like you played your A-game!\n");
            review.append("What conditions helped? Try to recreate them next session.");
        }

        if (tags.contains("Ran Bad")) {
            review.append("\n\nYou felt like variance was against you.\n");
            review.append("That's poker! Focus on what you can control: your decisions.");
        }

        if (tags.contains("Ran Good")) {
            review.append("\n\nYou felt like variance was on your side.\n");
            review.append("Don't let this mask any mistakes - review your hands anyway!");
        }

        reviewAlert.setContentText(review.toString());

        // Style the dialog
        DialogPane dialogPane = reviewAlert.getDialogPane();
        dialogPane.setMinWidth(500);
        dialogPane.setMinHeight(400);

        // Apply dark theme to dialog
        dialogPane.setStyle(
            "-fx-background-color: #1a1a24; " +
            "-fx-font-size: 13px;"
        );
        dialogPane.lookup(".content").setStyle("-fx-text-fill: #f0f0f0;");
        dialogPane.lookup(".header-panel").setStyle(
            "-fx-background-color: #252532; " +
            "-fx-text-fill: " + (wasWinning ? "#22c55e" : wasLosing ? "#ef4444" : "#f0f0f0") + ";"
        );

        // Add "Skip Reviews" button option
        ButtonType skipButton = new ButtonType("Got it!", ButtonBar.ButtonData.OK_DONE);
        ButtonType reviewHandsButton = new ButtonType("Review in Notes", ButtonBar.ButtonData.LEFT);
        reviewAlert.getButtonTypes().setAll(reviewHandsButton, skipButton);

        Optional<ButtonType> result = reviewAlert.showAndWait();

        // If user wants to review, switch to notes tab (if we had tab access)
        // For now, just acknowledge
        if (result.isPresent() && result.get() == reviewHandsButton) {
            // Could implement tab switching here in the future
            Alert notePrompt = new Alert(Alert.AlertType.INFORMATION);
            notePrompt.setTitle("Add to Notes");
            notePrompt.setHeaderText("Session notes tip:");
            notePrompt.setContentText(
                "Go to the Notes tab to write down:\n\n" +
                "• Hands where you were unsure of the right play\n" +
                "• Patterns you noticed in opponents\n" +
                "• Leaks you want to work on\n" +
                "• Concepts to study before next session"
            );
            notePrompt.getDialogPane().setStyle("-fx-background-color: #1a1a24;");
            notePrompt.showAndWait();
        }
    }
}
