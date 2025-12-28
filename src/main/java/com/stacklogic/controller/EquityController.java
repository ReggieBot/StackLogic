package com.stacklogic.controller;

import com.stacklogic.model.Card;
import com.stacklogic.model.Position;
import com.stacklogic.model.Range;
import com.stacklogic.util.EquityCalculator;
import com.stacklogic.util.EquityCalculator.EquityResult;
import com.stacklogic.util.RangeLoader;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the Equity Calculator.
 *
 * EQUITY CALCULATION EXPLAINED:
 * =============================
 * Equity is your probability of winning the hand at showdown.
 *
 * Example: You have K♥9♥, villain has A♠K♠, board is K♣4♥7♠
 * - You have top pair with 9 kicker
 * - Villain has top pair with ace kicker
 * - You need to catch a 9 (3 outs) or running hearts for a flush
 * - Your equity is roughly ~15%
 *
 * This calculator uses Monte Carlo simulation:
 * 1. Deal random remaining cards
 * 2. Evaluate both hands
 * 3. Count wins/ties/losses
 * 4. Repeat thousands of times for accurate equity
 */
public class EquityController implements Initializable {

    // Card displays
    @FXML private HBox heroCardsDisplay;
    @FXML private HBox villainCardsDisplay;
    @FXML private HBox boardCardsDisplay;

    // Villain mode
    @FXML private ToggleButton villainModeToggle;
    @FXML private VBox villainRangeSection;
    @FXML private ComboBox<String> villainPositionCombo;
    @FXML private ComboBox<String> villainSituationCombo;
    @FXML private Label rangeInfoLabel;

    // Card picker
    @FXML private GridPane cardPickerGrid;
    @FXML private Label selectingLabel;
    @FXML private Label boardStageLabel;

    // Selection mode buttons
    @FXML private Button selectHeroBtn;
    @FXML private Button selectVillainBtn;
    @FXML private Button selectBoardBtn;

    // Results
    @FXML private StackPane loadingPane;
    @FXML private VBox resultsPane;
    @FXML private Label heroEquityLabel;
    @FXML private Label villainEquityLabel;
    @FXML private HBox heroEquityBar;
    @FXML private HBox villainEquityBar;
    @FXML private Label winsLabel;
    @FXML private Label tiesLabel;
    @FXML private Label lossesLabel;
    @FXML private Label trialsLabel;
    @FXML private Label heroHandTypeLabel;
    @FXML private Label villainHandTypeLabel;
    @FXML private Label recommendationLabel;

    // State
    private enum SelectionMode { HERO, VILLAIN, BOARD }
    private SelectionMode currentMode = SelectionMode.HERO;

    private List<Card> heroCards = new ArrayList<>();
    private List<Card> villainCards = new ArrayList<>();
    private List<Card> boardCards = new ArrayList<>();
    private boolean useVillainRange = false;
    private Range selectedRange = null;

    // Card buttons for enabling/disabling
    private Map<Integer, Button> cardButtons = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        buildCardPicker();
        setupVillainMode();
        updateSelectionModeUI();
    }

    /**
     * Build the 4x13 card picker grid (4 suits x 13 ranks)
     */
    private void buildCardPicker() {
        cardPickerGrid.getChildren().clear();
        cardButtons.clear();

        // Add rank headers (A, K, Q, J, T, 9, 8, 7, 6, 5, 4, 3, 2)
        for (int rank = 12; rank >= 0; rank--) {
            Label header = new Label(Card.RANK_SYMBOLS[rank]);
            header.getStyleClass().add("card-header");
            header.setMinWidth(32);
            header.setAlignment(Pos.CENTER);
            cardPickerGrid.add(header, 12 - rank, 0);
        }

        // Add suit labels and cards
        String[] suitLabels = {"♥", "♦", "♣", "♠"};
        String[] suitClasses = {"hearts", "diamonds", "clubs", "spades"};

        for (int suit = 0; suit < 4; suit++) {
            // Suit label
            Label suitLabel = new Label(suitLabels[suit]);
            suitLabel.getStyleClass().addAll("suit-label", suitClasses[suit]);
            suitLabel.setMinWidth(20);
            cardPickerGrid.add(suitLabel, 13, suit + 1);

            // Card buttons
            for (int rank = 12; rank >= 0; rank--) {
                Card card = new Card(rank, suit);
                Button btn = new Button(card.getDisplay());
                btn.getStyleClass().addAll("card-btn", suitClasses[suit]);
                btn.setMinWidth(36);
                btn.setMinHeight(32);
                btn.setOnAction(e -> handleCardClick(card, btn));
                cardButtons.put(card.getId(), btn);
                cardPickerGrid.add(btn, 12 - rank, suit + 1);
            }
        }
    }

    /**
     * Setup villain position and situation dropdowns
     */
    private void setupVillainMode() {
        // Populate position dropdown
        villainPositionCombo.getItems().addAll("UTG", "MP", "CO", "BTN", "SB", "BB");
        villainPositionCombo.setOnAction(e -> updateSituationCombo());

        // Situation will be populated based on position
        villainSituationCombo.setOnAction(e -> loadSelectedRange());
    }

    private void updateSituationCombo() {
        villainSituationCombo.getItems().clear();
        String position = villainPositionCombo.getValue();
        if (position == null) return;

        // Add RFI option
        villainSituationCombo.getItems().add("RFI (Open Raise)");

        // Add vs raise options based on position
        switch (position) {
            case "BB":
                villainSituationCombo.getItems().addAll(
                    "vs UTG", "vs MP", "vs CO", "vs BTN", "vs SB"
                );
                break;
            case "SB":
                villainSituationCombo.getItems().addAll(
                    "vs UTG", "vs MP", "vs CO", "vs BTN"
                );
                break;
            case "BTN":
                villainSituationCombo.getItems().addAll(
                    "vs UTG", "vs MP", "vs CO"
                );
                break;
            case "CO":
                villainSituationCombo.getItems().addAll(
                    "vs UTG", "vs MP"
                );
                break;
            case "MP":
                villainSituationCombo.getItems().add("vs UTG");
                break;
        }
    }

    private void loadSelectedRange() {
        String position = villainPositionCombo.getValue();
        String situation = villainSituationCombo.getValue();
        if (position == null || situation == null) {
            rangeInfoLabel.setText("No range selected");
            selectedRange = null;
            return;
        }

        Position pos = Position.valueOf(position);
        Range range;

        if (situation.equals("RFI (Open Raise)")) {
            range = RangeLoader.loadRfiRange(pos);
        } else {
            // Extract raiser position from "vs XXX"
            String raiserStr = situation.replace("vs ", "");
            Position raiserPos = Position.valueOf(raiserStr);
            range = RangeLoader.loadVsRaiseRange(pos, raiserPos);
        }

        if (range != null) {
            selectedRange = range;
            rangeInfoLabel.setText(String.format("%s - %.1f%% of hands",
                range.getName(), range.getPlayPercentage()));
        } else {
            rangeInfoLabel.setText("Range not found");
            selectedRange = null;
        }
    }

    /**
     * Handle clicking a card in the picker
     */
    private void handleCardClick(Card card, Button btn) {
        // Check if card is already used
        if (isCardUsed(card)) {
            // Remove from wherever it is
            removeCardFromAll(card);
            btn.getStyleClass().remove("selected");
            updateAllDisplays();
            return;
        }

        // Add to current selection
        switch (currentMode) {
            case HERO:
                if (heroCards.size() < 2) {
                    heroCards.add(card);
                    btn.getStyleClass().add("selected");
                    btn.getStyleClass().add("hero-selected");
                }
                break;
            case VILLAIN:
                if (!useVillainRange && villainCards.size() < 2) {
                    villainCards.add(card);
                    btn.getStyleClass().add("selected");
                    btn.getStyleClass().add("villain-selected");
                }
                break;
            case BOARD:
                if (boardCards.size() < 5) {
                    boardCards.add(card);
                    btn.getStyleClass().add("selected");
                    btn.getStyleClass().add("board-selected");
                }
                break;
        }

        updateAllDisplays();
    }

    private boolean isCardUsed(Card card) {
        return heroCards.contains(card) ||
               villainCards.contains(card) ||
               boardCards.contains(card);
    }

    private void removeCardFromAll(Card card) {
        heroCards.remove(card);
        villainCards.remove(card);
        boardCards.remove(card);

        Button btn = cardButtons.get(card.getId());
        if (btn != null) {
            btn.getStyleClass().removeAll("selected", "hero-selected", "villain-selected", "board-selected");
        }
    }

    private void updateAllDisplays() {
        updateCardDisplay(heroCardsDisplay, heroCards, "Hero");
        updateCardDisplay(villainCardsDisplay, villainCards, "Villain");
        updateCardDisplay(boardCardsDisplay, boardCards, "Board");
        updateBoardStageLabel();
    }

    private void updateCardDisplay(HBox display, List<Card> cards, String type) {
        display.getChildren().clear();

        if (cards.isEmpty()) {
            String text = type.equals("Board") ?
                "Click cards below to add board cards" :
                "Click cards below to select";
            Label placeholder = new Label(text);
            placeholder.getStyleClass().add("placeholder-text");
            display.getChildren().add(placeholder);
            return;
        }

        for (Card card : cards) {
            Label cardLabel = createCardLabel(card);
            display.getChildren().add(cardLabel);
        }
    }

    private Label createCardLabel(Card card) {
        Label label = new Label(card.getDisplay());
        label.getStyleClass().add("card-display");
        if (card.isRed()) {
            label.getStyleClass().add("red-card");
        } else {
            label.getStyleClass().add("black-card");
        }
        return label;
    }

    private void updateBoardStageLabel() {
        switch (boardCards.size()) {
            case 0: boardStageLabel.setText("(Preflop)"); break;
            case 1:
            case 2: boardStageLabel.setText("(Incomplete)"); break;
            case 3: boardStageLabel.setText("(Flop)"); break;
            case 4: boardStageLabel.setText("(Turn)"); break;
            case 5: boardStageLabel.setText("(River)"); break;
        }
    }

    private void updateSelectionModeUI() {
        selectHeroBtn.getStyleClass().remove("active");
        selectVillainBtn.getStyleClass().remove("active");
        selectBoardBtn.getStyleClass().remove("active");

        switch (currentMode) {
            case HERO:
                selectHeroBtn.getStyleClass().add("active");
                selectingLabel.setText("Selecting for: Your Hand");
                break;
            case VILLAIN:
                selectVillainBtn.getStyleClass().add("active");
                selectingLabel.setText("Selecting for: Villain");
                break;
            case BOARD:
                selectBoardBtn.getStyleClass().add("active");
                selectingLabel.setText("Selecting for: Board");
                break;
        }
    }

    // ========================================
    // FXML Event Handlers
    // ========================================

    @FXML
    private void handleSelectHero() {
        currentMode = SelectionMode.HERO;
        updateSelectionModeUI();
    }

    @FXML
    private void handleSelectVillain() {
        currentMode = SelectionMode.VILLAIN;
        updateSelectionModeUI();
    }

    @FXML
    private void handleSelectBoard() {
        currentMode = SelectionMode.BOARD;
        updateSelectionModeUI();
    }

    @FXML
    private void handleVillainModeToggle() {
        useVillainRange = !villainModeToggle.isSelected();

        if (useVillainRange) {
            villainModeToggle.setText("Use Range");
            villainRangeSection.setVisible(true);
            villainRangeSection.setManaged(true);
            villainCardsDisplay.setVisible(false);
            villainCardsDisplay.setManaged(false);
            // Clear specific villain cards
            clearVillainCards();
        } else {
            villainModeToggle.setText("Specific Hand");
            villainRangeSection.setVisible(false);
            villainRangeSection.setManaged(false);
            villainCardsDisplay.setVisible(true);
            villainCardsDisplay.setManaged(true);
            selectedRange = null;
        }
    }

    @FXML
    private void handleClearHero() {
        for (Card card : new ArrayList<>(heroCards)) {
            removeCardFromAll(card);
        }
        heroCards.clear();
        updateAllDisplays();
    }

    @FXML
    private void handleClearVillain() {
        clearVillainCards();
        selectedRange = null;
        villainPositionCombo.setValue(null);
        villainSituationCombo.getItems().clear();
        rangeInfoLabel.setText("No range selected");
    }

    private void clearVillainCards() {
        for (Card card : new ArrayList<>(villainCards)) {
            removeCardFromAll(card);
        }
        villainCards.clear();
        updateAllDisplays();
    }

    @FXML
    private void handleClearBoard() {
        for (Card card : new ArrayList<>(boardCards)) {
            removeCardFromAll(card);
        }
        boardCards.clear();
        updateAllDisplays();
    }

    @FXML
    private void handleClearAll() {
        handleClearHero();
        handleClearVillain();
        handleClearBoard();
        clearResults();
    }

    @FXML
    private void handleCalculate() {
        // Validate inputs
        if (heroCards.size() != 2) {
            showError("Please select exactly 2 cards for your hand");
            return;
        }

        if (!useVillainRange && villainCards.size() != 2) {
            showError("Please select exactly 2 cards for villain's hand");
            return;
        }

        if (useVillainRange && selectedRange == null) {
            showError("Please select a range for villain");
            return;
        }

        // Show loading
        loadingPane.setVisible(true);
        loadingPane.setManaged(true);
        resultsPane.setVisible(false);

        // Run calculation in background
        CompletableFuture.supplyAsync(() -> {
            if (useVillainRange) {
                return EquityCalculator.calculateVsRange(heroCards, selectedRange, boardCards);
            } else {
                return EquityCalculator.calculate(heroCards, villainCards, boardCards);
            }
        }).thenAccept(result -> {
            Platform.runLater(() -> displayResults(result));
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                showError("Calculation error: " + e.getMessage());
                loadingPane.setVisible(false);
                loadingPane.setManaged(false);
                resultsPane.setVisible(true);
            });
            return null;
        });
    }

    private void displayResults(EquityResult result) {
        loadingPane.setVisible(false);
        loadingPane.setManaged(false);
        resultsPane.setVisible(true);

        // Equity values
        heroEquityLabel.setText(String.format("%.1f%%", result.getHeroEquityPercent()));
        villainEquityLabel.setText(String.format("%.1f%%", result.getVillainEquityPercent()));

        // Equity bars
        updateEquityBar(heroEquityBar, result.heroEquity, true);
        updateEquityBar(villainEquityBar, result.villainEquity, false);

        // Breakdown
        winsLabel.setText(String.format("%,d (%.1f%%)", result.wins,
            (result.wins * 100.0 / result.trials)));
        tiesLabel.setText(String.format("%,d (%.1f%%)", result.ties,
            (result.ties * 100.0 / result.trials)));
        lossesLabel.setText(String.format("%,d (%.1f%%)", result.losses,
            (result.losses * 100.0 / result.trials)));
        trialsLabel.setText(String.format("%,d", result.trials));

        // Hand types
        heroHandTypeLabel.setText(result.heroHandType);
        villainHandTypeLabel.setText(result.villainHandType);

        // Recommendation
        updateRecommendation(result);
    }

    private void updateEquityBar(HBox bar, double equity, boolean isHero) {
        bar.getChildren().clear();

        Region filled = new Region();
        filled.getStyleClass().add(isHero ? "equity-fill-hero" : "equity-fill-villain");
        filled.prefWidthProperty().bind(bar.widthProperty().multiply(equity));

        Region empty = new Region();
        empty.getStyleClass().add("equity-fill-empty");
        HBox.setHgrow(empty, Priority.ALWAYS);

        bar.getChildren().addAll(filled, empty);
    }

    private void updateRecommendation(EquityResult result) {
        recommendationLabel.getStyleClass().removeAll(
            "recommendation-call", "recommendation-fold", "recommendation-neutral"
        );

        double equity = result.heroEquity;

        if (equity >= 0.65) {
            recommendationLabel.setText(
                String.format("STRONG - You're a %.0f%% favorite", equity * 100)
            );
            recommendationLabel.getStyleClass().add("recommendation-call");
        } else if (equity >= 0.50) {
            recommendationLabel.setText(
                String.format("AHEAD - Slight edge at %.0f%%", equity * 100)
            );
            recommendationLabel.getStyleClass().add("recommendation-call");
        } else if (equity >= 0.35) {
            recommendationLabel.setText(
                String.format("UNDERDOG - %.0f%% equity, need good pot odds", equity * 100)
            );
            recommendationLabel.getStyleClass().add("recommendation-neutral");
        } else {
            recommendationLabel.setText(
                String.format("BEHIND - Only %.0f%% equity", equity * 100)
            );
            recommendationLabel.getStyleClass().add("recommendation-fold");
        }
    }

    private void clearResults() {
        heroEquityLabel.setText("--");
        villainEquityLabel.setText("--");
        heroEquityBar.getChildren().clear();
        villainEquityBar.getChildren().clear();
        winsLabel.setText("--");
        tiesLabel.setText("--");
        lossesLabel.setText("--");
        trialsLabel.setText("--");
        heroHandTypeLabel.setText("--");
        villainHandTypeLabel.setText("--");
        recommendationLabel.setText("Select cards and calculate");
        recommendationLabel.getStyleClass().removeAll(
            "recommendation-call", "recommendation-fold", "recommendation-neutral"
        );
    }

    private void showError(String message) {
        recommendationLabel.setText(message);
        recommendationLabel.getStyleClass().removeAll(
            "recommendation-call", "recommendation-fold", "recommendation-neutral"
        );
        recommendationLabel.getStyleClass().add("recommendation-fold");
    }
}
