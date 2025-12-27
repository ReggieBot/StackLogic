package com.stacklogic.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Pot Odds Calculator.
 *
 * POT ODDS MATH EXPLAINED:
 * ========================
 *
 * Pot Odds = how much you need to call vs what you can win
 *
 * Example: Pot is $10, villain bets $5
 *   - You need to call $5 to win $15 (pot + villain's bet)
 *   - Pot odds = 5 / (15 + 5) = 5/20 = 25%
 *   - As a ratio: 15:5 = 3:1 (you're getting 3-to-1 on your call)
 *
 * Required Equity = the minimum equity you need to break even
 *   - If pot odds are 25%, you need at least 25% equity to call profitably
 *
 * Expected Value (EV):
 *   EV = (equity × pot you win) - ((1 - equity) × amount you lose)
 *   - If EV > 0, the call is profitable (+EV)
 *   - If EV < 0, the call loses money (-EV)
 */
public class PotOddsController implements Initializable {

    // Input fields
    @FXML private TextField potSizeField;
    @FXML private TextField villainBetField;
    @FXML private Slider equitySlider;
    @FXML private TextField equityField;
    @FXML private Label equityDisplayLabel;

    // Result labels
    @FXML private Label potOddsPercentLabel;
    @FXML private Label potOddsRatioLabel;
    @FXML private Label requiredEquityLabel;
    @FXML private Label yourEquityLabel;
    @FXML private Label evFormulaLabel;
    @FXML private Label evResultLabel;
    @FXML private Label recommendationLabel;

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        // Sync slider with text field and display label
        equitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int equity = newVal.intValue();
            equityDisplayLabel.setText(equity + "%");
            equityField.setText(String.valueOf(equity));
        });

        // Sync text field back to slider
        equityField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                try {
                    int value = Integer.parseInt(newVal);
                    if (value >= 0 && value <= 100) {
                        equitySlider.setValue(value);
                    }
                } catch (NumberFormatException ignored) {
                    // Invalid input, ignore
                }
            }
        });

        // Auto-calculate when inputs change
        potSizeField.textProperty().addListener((obs, oldVal, newVal) -> autoCalculate());
        villainBetField.textProperty().addListener((obs, oldVal, newVal) -> autoCalculate());
        equitySlider.valueProperty().addListener((obs, oldVal, newVal) -> autoCalculate());

        // Restrict text fields to numbers only
        addNumericFilter(potSizeField);
        addNumericFilter(villainBetField);
        addIntegerFilter(equityField);
    }

    /**
     * Add a filter to only allow numeric input (including decimals).
     */
    private void addNumericFilter(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                field.setText(oldVal);
            }
        });
    }

    /**
     * Add a filter to only allow integer input.
     */
    private void addIntegerFilter(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                field.setText(oldVal);
            }
        });
    }

    /**
     * Auto-calculate when all required fields have values.
     */
    private void autoCalculate() {
        String potText = potSizeField.getText();
        String betText = villainBetField.getText();

        if (!potText.isEmpty() && !betText.isEmpty()) {
            try {
                double pot = Double.parseDouble(potText);
                double bet = Double.parseDouble(betText);
                if (pot > 0 && bet > 0) {
                    calculate(pot, bet, equitySlider.getValue());
                }
            } catch (NumberFormatException ignored) {
                // Invalid input
            }
        }
    }

    /**
     * Handle Calculate button click.
     */
    @FXML
    private void handleCalculate() {
        try {
            String potText = potSizeField.getText();
            String betText = villainBetField.getText();

            if (potText.isEmpty() || betText.isEmpty()) {
                showError("Please enter pot size and villain's bet");
                return;
            }

            double pot = Double.parseDouble(potText);
            double bet = Double.parseDouble(betText);
            double equity = equitySlider.getValue();

            if (pot <= 0 || bet <= 0) {
                showError("Values must be greater than 0");
                return;
            }

            calculate(pot, bet, equity);

        } catch (NumberFormatException e) {
            showError("Please enter valid numbers");
        }
    }

    /**
     * Perform the pot odds calculation.
     */
    private void calculate(double pot, double villainBet, double equityPercent) {
        // Convert equity percentage to decimal
        double equity = equityPercent / 100.0;

        // Total pot after villain's bet (what you can win)
        double totalPot = pot + villainBet;

        // Amount you need to call
        double callAmount = villainBet;

        // Pot odds as percentage
        // Pot odds = call / (totalPot + call)
        double potOddsPercent = (callAmount / (totalPot + callAmount)) * 100;

        // Pot odds as ratio (what you win : what you risk)
        // Ratio is totalPot : callAmount
        double ratioWin = totalPot / callAmount;

        // Required equity to break even = pot odds percentage
        double requiredEquity = potOddsPercent;

        // EV calculation
        // EV = (equity × pot you win) - ((1 - equity) × amount you lose)
        // If you call and win: you win totalPot
        // If you call and lose: you lose callAmount
        double evWin = equity * totalPot;
        double evLose = (1 - equity) * callAmount;
        double ev = evWin - evLose;

        // Update display
        potOddsPercentLabel.setText(String.format("%.1f%%", potOddsPercent));
        potOddsRatioLabel.setText(String.format("%.1f : 1", ratioWin));
        requiredEquityLabel.setText(String.format("%.1f%%", requiredEquity));
        yourEquityLabel.setText(String.format("%.0f%%", equityPercent));

        // Show EV formula
        evFormulaLabel.setText(String.format(
            "EV = (%.0f%% × $%.2f) - (%.0f%% × $%.2f)",
            equityPercent, totalPot,
            100 - equityPercent, callAmount
        ));

        // Show EV result with color
        evResultLabel.setText(String.format("EV = $%.2f per call", ev));
        evResultLabel.getStyleClass().removeAll("profit-positive", "profit-negative", "profit-neutral");

        if (ev > 0.01) {
            evResultLabel.getStyleClass().add("profit-positive");
        } else if (ev < -0.01) {
            evResultLabel.getStyleClass().add("profit-negative");
        } else {
            evResultLabel.getStyleClass().add("profit-neutral");
        }

        // Show recommendation
        updateRecommendation(equity, requiredEquity / 100, ev);
    }

    /**
     * Update the recommendation based on calculations.
     */
    private void updateRecommendation(double equity, double requiredEquity, double ev) {
        recommendationLabel.getStyleClass().removeAll(
            "recommendation-call", "recommendation-fold", "recommendation-neutral"
        );

        double equityDiff = equity - requiredEquity;

        if (ev > 0.01) {
            // +EV call
            recommendationLabel.setText(String.format(
                "CALL is +EV! You have %.1f%% more equity than needed.",
                equityDiff * 100
            ));
            recommendationLabel.getStyleClass().add("recommendation-call");
        } else if (ev < -0.01) {
            // -EV call
            recommendationLabel.setText(String.format(
                "FOLD is correct. You need %.1f%% more equity to call.",
                -equityDiff * 100
            ));
            recommendationLabel.getStyleClass().add("recommendation-fold");
        } else {
            // Break even
            recommendationLabel.setText(
                "BREAK EVEN. Your equity exactly matches pot odds."
            );
            recommendationLabel.getStyleClass().add("recommendation-neutral");
        }
    }

    /**
     * Show an error message in the recommendation area.
     */
    private void showError(String message) {
        recommendationLabel.setText(message);
        recommendationLabel.getStyleClass().removeAll(
            "recommendation-call", "recommendation-fold", "recommendation-neutral"
        );
    }

    /**
     * Handle Clear button click.
     */
    @FXML
    private void handleClear() {
        potSizeField.clear();
        villainBetField.clear();
        equitySlider.setValue(50);
        equityField.setText("50");

        potOddsPercentLabel.setText("--");
        potOddsRatioLabel.setText("--");
        requiredEquityLabel.setText("--");
        yourEquityLabel.setText("--");
        evFormulaLabel.setText("");
        evResultLabel.setText("--");
        evResultLabel.getStyleClass().removeAll("profit-positive", "profit-negative", "profit-neutral");

        recommendationLabel.setText("Enter values to calculate");
        recommendationLabel.getStyleClass().removeAll(
            "recommendation-call", "recommendation-fold", "recommendation-neutral"
        );
    }
}
