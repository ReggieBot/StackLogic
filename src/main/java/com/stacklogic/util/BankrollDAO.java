package com.stacklogic.util;

import com.stacklogic.model.BankrollTransaction;
import com.stacklogic.model.BankrollTransaction.TransactionType;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Bankroll transactions.
 *
 * Handles all database operations for deposits and withdrawals.
 */
public class BankrollDAO {

    /**
     * Create a new transaction.
     */
    public static int create(BankrollTransaction transaction) throws SQLException {
        String sql = """
            INSERT INTO bankroll_transactions (date, type, amount, notes)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, transaction.getDate().toString());
            pstmt.setString(2, transaction.getType().name());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getNotes());

            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    transaction.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Delete a transaction by ID.
     */
    public static void delete(int id) throws SQLException {
        String sql = "DELETE FROM bankroll_transactions WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Get all transactions ordered by date (newest first).
     */
    public static List<BankrollTransaction> getAll() throws SQLException {
        String sql = "SELECT * FROM bankroll_transactions ORDER BY date DESC, id DESC";
        List<BankrollTransaction> transactions = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    /**
     * Get total deposits.
     */
    public static double getTotalDeposits() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM bankroll_transactions WHERE type = 'DEPOSIT'";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0;
    }

    /**
     * Get total withdrawals.
     */
    public static double getTotalWithdrawals() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM bankroll_transactions WHERE type = 'WITHDRAWAL'";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0;
    }

    /**
     * Get net deposits (deposits - withdrawals).
     */
    public static double getNetDeposits() throws SQLException {
        return getTotalDeposits() - getTotalWithdrawals();
    }

    /**
     * Calculate current bankroll.
     * Bankroll = Net Deposits + Session Profits
     */
    public static double getCurrentBankroll() throws SQLException {
        double netDeposits = getNetDeposits();
        SessionDAO.SessionStats stats = SessionDAO.getStats();
        return netDeposits + stats.totalProfit;
    }

    /**
     * Get bankroll summary statistics.
     */
    public static BankrollStats getStats() throws SQLException {
        double deposits = getTotalDeposits();
        double withdrawals = getTotalWithdrawals();
        SessionDAO.SessionStats sessionStats = SessionDAO.getStats();

        double netDeposits = deposits - withdrawals;
        double currentBankroll = netDeposits + sessionStats.totalProfit;

        return new BankrollStats(deposits, withdrawals, sessionStats.totalProfit, currentBankroll);
    }

    /**
     * Convert a database row to a BankrollTransaction.
     */
    private static BankrollTransaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        return new BankrollTransaction(
            rs.getInt("id"),
            LocalDate.parse(rs.getString("date")),
            TransactionType.valueOf(rs.getString("type")),
            rs.getDouble("amount"),
            rs.getString("notes")
        );
    }

    /**
     * Container for bankroll statistics.
     */
    public static class BankrollStats {
        public final double totalDeposits;
        public final double totalWithdrawals;
        public final double sessionProfit;
        public final double currentBankroll;

        public BankrollStats(double totalDeposits, double totalWithdrawals,
                            double sessionProfit, double currentBankroll) {
            this.totalDeposits = totalDeposits;
            this.totalWithdrawals = totalWithdrawals;
            this.sessionProfit = sessionProfit;
            this.currentBankroll = currentBankroll;
        }

        /**
         * Get recommended max buy-in based on 25 buy-in rule.
         * If bankroll is $50, max buy-in is $2 (for 2NL).
         */
        public double getRecommendedMaxBuyIn() {
            return currentBankroll / 25.0;
        }

        /**
         * Get the recommended stakes based on bankroll.
         */
        public String getRecommendedStakes() {
            double maxBuyIn = getRecommendedMaxBuyIn();

            if (maxBuyIn >= 100) return "100NL ($100 max buy-in)";
            if (maxBuyIn >= 50) return "50NL ($50 max buy-in)";
            if (maxBuyIn >= 25) return "25NL ($25 max buy-in)";
            if (maxBuyIn >= 10) return "10NL ($10 max buy-in)";
            if (maxBuyIn >= 5) return "5NL ($5 max buy-in)";
            if (maxBuyIn >= 2) return "2NL ($2 max buy-in)";
            return "Build bankroll (need $50+ for 2NL)";
        }

        /**
         * Check if a given stakes level is within bankroll.
         */
        public boolean isStakesWithinBankroll(String stakes) {
            double requiredBankroll = switch (stakes) {
                case "2NL" -> 50;    // 25 x $2
                case "5NL" -> 125;   // 25 x $5
                case "10NL" -> 250;  // 25 x $10
                case "25NL" -> 625;  // 25 x $25
                case "50NL" -> 1250; // 25 x $50
                case "100NL" -> 2500; // 25 x $100
                default -> 0;
            };
            return currentBankroll >= requiredBankroll;
        }
    }
}
