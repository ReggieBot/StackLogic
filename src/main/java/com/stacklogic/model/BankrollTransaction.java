package com.stacklogic.model;

import java.time.LocalDate;

/**
 * Represents a bankroll transaction (deposit or withdrawal).
 *
 * BANKROLL VS SESSION PROFIT:
 * ===========================
 * Your bankroll changes in two ways:
 * 1. Session results (wins/losses) - tracked in Session
 * 2. Deposits/Withdrawals - tracked here
 *
 * Total Bankroll = Deposits - Withdrawals + Session Profits
 *
 * This separation helps you see:
 * - How much you've put in (deposits)
 * - How much you've taken out (withdrawals)
 * - How much you've actually won/lost (session profit)
 */
public class BankrollTransaction {

    public enum TransactionType {
        DEPOSIT("Deposit"),
        WITHDRAWAL("Withdrawal");

        private final String displayName;

        TransactionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private int id = -1;
    private LocalDate date;
    private TransactionType type;
    private double amount;
    private String notes;

    public BankrollTransaction() {
        this.date = LocalDate.now();
        this.type = TransactionType.DEPOSIT;
    }

    public BankrollTransaction(int id, LocalDate date, TransactionType type, double amount, String notes) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.notes = notes;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Get the effective amount (positive for deposits, negative for withdrawals).
     */
    public double getEffectiveAmount() {
        return type == TransactionType.DEPOSIT ? amount : -amount;
    }

    @Override
    public String toString() {
        return String.format("%s: %s $%.2f", date, type, amount);
    }
}
