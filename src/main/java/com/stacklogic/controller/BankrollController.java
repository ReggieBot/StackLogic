package com.stacklogic.controller;

import com.stacklogic.model.BankrollTransaction;
import com.stacklogic.model.BankrollTransaction.TransactionType;
import com.stacklogic.util.BankrollDAO;
import com.stacklogic.util.BankrollDAO.BankrollStats;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controller for the Bankroll Tracker.
 *
 * THE 25 BUY-IN RULE:
 * ===================
 * A common bankroll management rule is to have at least 25 buy-ins
 * for the stakes you're playing. This protects you from going broke
 * during downswings.
 *
 * For example:
 *   - 2NL ($2 max buy-in): Need $50 bankroll
 *   - 5NL ($5 max buy-in): Need $125 bankroll
 *   - 10NL ($10 max buy-in): Need $250 bankroll
 *
 * This tracker shows if you're properly rolled for your stakes.
 */
public class BankrollController implements Initializable {

    // Overview labels
    @FXML private Label currentBankrollLabel;
    @FXML private Label totalDepositsLabel;
    @FXML private Label totalWithdrawalsLabel;
    @FXML private Label sessionProfitLabel;
    @FXML private Label recommendedStakesLabel;
    @FXML private Label stakesWarningLabel;

    // Form fields
    @FXML private RadioButton depositRadio;
    @FXML private RadioButton withdrawalRadio;
    @FXML private DatePicker datePicker;
    @FXML private TextField amountField;
    @FXML private TextField notesField;

    // Sync balance fields
    @FXML private TextField actualBalanceField;
    @FXML private Label syncDifferenceLabel;

    // Table
    @FXML private TableView<BankrollTransaction> transactionTable;
    @FXML private TableColumn<BankrollTransaction, String> colDate;
    @FXML private TableColumn<BankrollTransaction, String> colType;
    @FXML private TableColumn<BankrollTransaction, String> colAmount;
    @FXML private TableColumn<BankrollTransaction, String> colNotes;
    @FXML private TableColumn<BankrollTransaction, Void> colAction;

    // Stakes guide labels
    @FXML private Label stake2NL;
    @FXML private Label stake5NL;
    @FXML private Label stake10NL;
    @FXML private Label stake25NL;
    @FXML private Label stake50NL;
    @FXML private Label stake100NL;

    // Data
    private ObservableList<BankrollTransaction> transactions = FXCollections.observableArrayList();
    private ToggleGroup typeGroup;

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        // Set up radio button group
        typeGroup = new ToggleGroup();
        depositRadio.setToggleGroup(typeGroup);
        withdrawalRadio.setToggleGroup(typeGroup);
        depositRadio.setSelected(true);

        // Default date to today
        datePicker.setValue(LocalDate.now());

        // Set up table columns
        setupTableColumns();

        // Load data
        loadTransactions();
        updateOverview();

        // Show difference preview as user types actual balance
        actualBalanceField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateSyncDifferencePreview();
        });
    }

    /**
     * Update the sync difference preview label as user types.
     */
    private void updateSyncDifferencePreview() {
        try {
            if (actualBalanceField.getText().isEmpty()) {
                syncDifferenceLabel.setText("");
                return;
            }

            double actualBalance = Double.parseDouble(actualBalanceField.getText());
            BankrollStats stats = BankrollDAO.getStats();
            double difference = actualBalance - stats.currentBankroll;

            if (Math.abs(difference) < 0.01) {
                syncDifferenceLabel.setText("No adjustment needed");
                syncDifferenceLabel.setStyle("-fx-text-fill: #22c55e;");
            } else if (difference > 0) {
                syncDifferenceLabel.setText(String.format("+$%.2f (rakeback)", difference));
                syncDifferenceLabel.setStyle("-fx-text-fill: #22c55e;");
            } else {
                syncDifferenceLabel.setText(String.format("-$%.2f (rake)", Math.abs(difference)));
                syncDifferenceLabel.setStyle("-fx-text-fill: #ef4444;");
            }
        } catch (NumberFormatException e) {
            syncDifferenceLabel.setText("Invalid number");
            syncDifferenceLabel.setStyle("-fx-text-fill: #ef4444;");
        } catch (SQLException e) {
            syncDifferenceLabel.setText("");
        }
    }

    /**
     * Set up table column bindings.
     */
    private void setupTableColumns() {
        colDate.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getDate().toString()));

        colType.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getType().getDisplayName()));

        // Color-code the type column
        colType.setCellFactory(column -> new TableCell<BankrollTransaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Deposit")) {
                        setStyle("-fx-text-fill: #4ade80;");
                    } else {
                        setStyle("-fx-text-fill: #f87171;");
                    }
                }
            }
        });

        colAmount.setCellValueFactory(data ->
            new SimpleStringProperty(String.format("$%.2f", data.getValue().getAmount())));

        colNotes.setCellValueFactory(data -> {
            String notes = data.getValue().getNotes();
            return new SimpleStringProperty(notes != null ? notes : "");
        });

        // Delete button column
        colAction.setCellFactory(column -> new TableCell<BankrollTransaction, Void>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.getStyleClass().addAll("button", "danger", "small");
                deleteBtn.setOnAction(event -> {
                    BankrollTransaction transaction = getTableView().getItems().get(getIndex());
                    handleDelete(transaction);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });

        transactionTable.setItems(transactions);
    }

    /**
     * Load transactions from database.
     */
    private void loadTransactions() {
        try {
            transactions.clear();
            transactions.addAll(BankrollDAO.getAll());
        } catch (SQLException e) {
            showError("Failed to load transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update the overview display.
     */
    private void updateOverview() {
        try {
            BankrollStats stats = BankrollDAO.getStats();

            // Current bankroll
            currentBankrollLabel.setText(String.format("$%.2f", stats.currentBankroll));
            currentBankrollLabel.getStyleClass().removeAll("profit-positive", "profit-negative");
            if (stats.currentBankroll > 0) {
                currentBankrollLabel.getStyleClass().add("profit-positive");
            } else if (stats.currentBankroll < 0) {
                currentBankrollLabel.getStyleClass().add("profit-negative");
            }

            // Breakdown
            totalDepositsLabel.setText(String.format("$%.2f", stats.totalDeposits));
            totalWithdrawalsLabel.setText(String.format("$%.2f", stats.totalWithdrawals));

            sessionProfitLabel.setText(String.format("$%.2f", stats.sessionProfit));
            sessionProfitLabel.getStyleClass().removeAll("profit-positive", "profit-negative");
            if (stats.sessionProfit > 0) {
                sessionProfitLabel.getStyleClass().add("profit-positive");
            } else if (stats.sessionProfit < 0) {
                sessionProfitLabel.getStyleClass().add("profit-negative");
            }

            // Recommended stakes
            recommendedStakesLabel.setText(stats.getRecommendedStakes());

            // Update stakes guide with current status
            updateStakesGuide(stats);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the stakes guide to show which levels are available.
     */
    private void updateStakesGuide(BankrollStats stats) {
        // Clear previous styling
        stake2NL.getStyleClass().removeAll("stake-available", "stake-unavailable");
        stake5NL.getStyleClass().removeAll("stake-available", "stake-unavailable");
        stake10NL.getStyleClass().removeAll("stake-available", "stake-unavailable");
        stake25NL.getStyleClass().removeAll("stake-available", "stake-unavailable");
        stake50NL.getStyleClass().removeAll("stake-available", "stake-unavailable");
        stake100NL.getStyleClass().removeAll("stake-available", "stake-unavailable");

        // Apply new styling based on bankroll
        stake2NL.getStyleClass().add(stats.isStakesWithinBankroll("2NL") ? "stake-available" : "stake-unavailable");
        stake5NL.getStyleClass().add(stats.isStakesWithinBankroll("5NL") ? "stake-available" : "stake-unavailable");
        stake10NL.getStyleClass().add(stats.isStakesWithinBankroll("10NL") ? "stake-available" : "stake-unavailable");
        stake25NL.getStyleClass().add(stats.isStakesWithinBankroll("25NL") ? "stake-available" : "stake-unavailable");
        stake50NL.getStyleClass().add(stats.isStakesWithinBankroll("50NL") ? "stake-available" : "stake-unavailable");
        stake100NL.getStyleClass().add(stats.isStakesWithinBankroll("100NL") ? "stake-available" : "stake-unavailable");
    }

    /**
     * Handle add transaction button.
     */
    @FXML
    private void handleAdd() {
        try {
            // Validate
            if (amountField.getText().isEmpty()) {
                showError("Please enter an amount");
                return;
            }

            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                showError("Amount must be greater than 0");
                return;
            }

            // Create transaction
            BankrollTransaction transaction = new BankrollTransaction();
            transaction.setDate(datePicker.getValue());
            transaction.setType(depositRadio.isSelected() ? TransactionType.DEPOSIT : TransactionType.WITHDRAWAL);
            transaction.setAmount(amount);
            transaction.setNotes(notesField.getText());

            // Save to database
            BankrollDAO.create(transaction);

            // Refresh
            loadTransactions();
            updateOverview();
            handleClear();

        } catch (NumberFormatException e) {
            showError("Please enter a valid amount");
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle delete transaction.
     */
    private void handleDelete(BankrollTransaction transaction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Transaction");
        alert.setHeaderText("Delete this transaction?");
        alert.setContentText(String.format("%s: $%.2f on %s",
            transaction.getType(), transaction.getAmount(), transaction.getDate()));

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                BankrollDAO.delete(transaction.getId());
                loadTransactions();
                updateOverview();
            } catch (SQLException e) {
                showError("Failed to delete: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle clear form.
     */
    @FXML
    private void handleClear() {
        depositRadio.setSelected(true);
        datePicker.setValue(LocalDate.now());
        amountField.clear();
        notesField.clear();
    }

    /**
     * Handle sync balance button.
     * Creates an adjustment transaction to match the actual GGPoker balance.
     * This accounts for rake paid and rakeback received.
     */
    @FXML
    private void handleSyncBalance() {
        try {
            if (actualBalanceField.getText().isEmpty()) {
                showError("Please enter your actual GGPoker balance");
                return;
            }

            double actualBalance = Double.parseDouble(actualBalanceField.getText());
            if (actualBalance < 0) {
                showError("Balance cannot be negative");
                return;
            }

            // Get current calculated bankroll
            BankrollStats stats = BankrollDAO.getStats();
            double currentCalculated = stats.currentBankroll;
            double difference = actualBalance - currentCalculated;

            if (Math.abs(difference) < 0.01) {
                // Already synced
                syncDifferenceLabel.setText("Already synced!");
                syncDifferenceLabel.setStyle("-fx-text-fill: #22c55e;");
                return;
            }

            // Confirm with user
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Sync Balance");
            confirm.setHeaderText("Sync to actual balance?");

            String adjustmentType = difference > 0 ? "add" : "subtract";
            String reason = difference > 0 ? "(rakeback, bonus, etc.)" : "(rake paid)";

            confirm.setContentText(String.format(
                "Current calculated: $%.2f\n" +
                "Actual GGPoker balance: $%.2f\n\n" +
                "This will %s $%.2f as an adjustment %s\n\n" +
                "Continue?",
                currentCalculated, actualBalance,
                adjustmentType, Math.abs(difference), reason
            ));

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                // Create adjustment transaction
                BankrollTransaction adjustment = new BankrollTransaction();
                adjustment.setDate(LocalDate.now());

                if (difference > 0) {
                    adjustment.setType(TransactionType.DEPOSIT);
                    adjustment.setAmount(difference);
                    adjustment.setNotes("Balance sync: rakeback/bonus adjustment");
                } else {
                    adjustment.setType(TransactionType.WITHDRAWAL);
                    adjustment.setAmount(Math.abs(difference));
                    adjustment.setNotes("Balance sync: rake adjustment");
                }

                BankrollDAO.create(adjustment);

                // Refresh display
                loadTransactions();
                updateOverview();
                actualBalanceField.clear();
                syncDifferenceLabel.setText("Synced!");
                syncDifferenceLabel.setStyle("-fx-text-fill: #22c55e;");
            }

        } catch (NumberFormatException e) {
            showError("Please enter a valid number");
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show error alert.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
