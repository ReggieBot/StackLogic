package com.stacklogic.util;

import com.stacklogic.model.Session;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Session database operations.
 *
 * WHAT IS A DAO?
 * ==============
 * A DAO is a pattern that separates database code from business logic.
 * All SQL queries for sessions live here, keeping other code clean.
 *
 * CRUD OPERATIONS:
 * ================
 *   - Create: INSERT new records
 *   - Read: SELECT existing records
 *   - Update: UPDATE existing records
 *   - Delete: DELETE records
 *
 * PREPARED STATEMENTS:
 * ====================
 * We use PreparedStatement instead of building SQL strings manually.
 * This prevents SQL injection attacks and handles escaping automatically.
 *
 * Instead of: "INSERT INTO sessions VALUES ('" + userInput + "')"  // DANGEROUS!
 * We use:     "INSERT INTO sessions VALUES (?)" then set parameters  // SAFE!
 */
public class SessionDAO {

    /**
     * Save a new session to the database.
     * Returns the generated ID.
     */
    public static int create(Session session) throws SQLException {
        String sql = """
            INSERT INTO sessions (date, start_time, end_time, duration_minutes,
                                  stakes, table_size, hands_played, buy_in,
                                  cash_out, profit, bb_won, tags, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set parameters (? placeholders in order)
            pstmt.setString(1, session.getDate().toString());
            pstmt.setString(2, session.getStartTime() != null ? session.getStartTime().toString() : null);
            pstmt.setString(3, session.getEndTime() != null ? session.getEndTime().toString() : null);
            pstmt.setObject(4, session.getDurationMinutes());  // setObject handles null
            pstmt.setString(5, session.getStakes());
            pstmt.setString(6, session.getTableSize());
            pstmt.setObject(7, session.getHandsPlayed());
            pstmt.setDouble(8, session.getBuyIn());
            pstmt.setDouble(9, session.getCashOut());
            pstmt.setDouble(10, session.getProfit());
            pstmt.setObject(11, session.getBbWon());
            pstmt.setString(12, session.getTags());
            pstmt.setString(13, session.getNotes());

            pstmt.executeUpdate();

            // Get the auto-generated ID
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    session.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Update an existing session.
     */
    public static void update(Session session) throws SQLException {
        String sql = """
            UPDATE sessions SET
                date = ?, start_time = ?, end_time = ?, duration_minutes = ?,
                stakes = ?, table_size = ?, hands_played = ?, buy_in = ?,
                cash_out = ?, profit = ?, bb_won = ?, tags = ?, notes = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, session.getDate().toString());
            pstmt.setString(2, session.getStartTime() != null ? session.getStartTime().toString() : null);
            pstmt.setString(3, session.getEndTime() != null ? session.getEndTime().toString() : null);
            pstmt.setObject(4, session.getDurationMinutes());
            pstmt.setString(5, session.getStakes());
            pstmt.setString(6, session.getTableSize());
            pstmt.setObject(7, session.getHandsPlayed());
            pstmt.setDouble(8, session.getBuyIn());
            pstmt.setDouble(9, session.getCashOut());
            pstmt.setDouble(10, session.getProfit());
            pstmt.setObject(11, session.getBbWon());
            pstmt.setString(12, session.getTags());
            pstmt.setString(13, session.getNotes());
            pstmt.setInt(14, session.getId());

            pstmt.executeUpdate();
        }
    }

    /**
     * Delete a session by ID.
     */
    public static void delete(int id) throws SQLException {
        String sql = "DELETE FROM sessions WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Get a session by ID.
     */
    public static Session getById(int id) throws SQLException {
        String sql = "SELECT * FROM sessions WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSession(rs);
                }
            }
        }
        return null;
    }

    /**
     * Get all sessions, ordered by date (newest first).
     */
    public static List<Session> getAll() throws SQLException {
        String sql = "SELECT * FROM sessions ORDER BY date DESC, id DESC";
        List<Session> sessions = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                sessions.add(mapResultSetToSession(rs));
            }
        }
        return sessions;
    }

    /**
     * Get sessions filtered by date range.
     */
    public static List<Session> getByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM sessions WHERE date >= ? AND date <= ? ORDER BY date DESC";
        List<Session> sessions = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapResultSetToSession(rs));
                }
            }
        }
        return sessions;
    }

    /**
     * Get sessions filtered by stakes.
     */
    public static List<Session> getByStakes(String stakes) throws SQLException {
        String sql = "SELECT * FROM sessions WHERE stakes = ? ORDER BY date DESC";
        List<Session> sessions = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, stakes);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapResultSetToSession(rs));
                }
            }
        }
        return sessions;
    }

    /**
     * Get aggregate stats for all sessions.
     */
    public static SessionStats getStats() throws SQLException {
        String sql = """
            SELECT
                COUNT(*) as session_count,
                COALESCE(SUM(profit), 0) as total_profit,
                COALESCE(SUM(duration_minutes), 0) as total_minutes,
                COALESCE(SUM(hands_played), 0) as total_hands,
                COALESCE(SUM(bb_won), 0) as total_bb_won
            FROM sessions
        """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return new SessionStats(
                    rs.getInt("session_count"),
                    rs.getDouble("total_profit"),
                    rs.getInt("total_minutes"),
                    rs.getInt("total_hands"),
                    rs.getDouble("total_bb_won")
                );
            }
        }
        return new SessionStats(0, 0, 0, 0, 0);
    }

    /**
     * Convert a database row to a Session object.
     */
    private static Session mapResultSetToSession(ResultSet rs) throws SQLException {
        // Parse date
        LocalDate date = LocalDate.parse(rs.getString("date"));

        // Parse optional times
        String startTimeStr = rs.getString("start_time");
        LocalTime startTime = startTimeStr != null ? LocalTime.parse(startTimeStr) : null;

        String endTimeStr = rs.getString("end_time");
        LocalTime endTime = endTimeStr != null ? LocalTime.parse(endTimeStr) : null;

        // Get optional integers (need to check for null)
        Integer durationMinutes = rs.getObject("duration_minutes") != null ?
            rs.getInt("duration_minutes") : null;
        Integer handsPlayed = rs.getObject("hands_played") != null ?
            rs.getInt("hands_played") : null;
        Double bbWon = rs.getObject("bb_won") != null ?
            rs.getDouble("bb_won") : null;

        return new Session(
            rs.getInt("id"),
            date,
            startTime,
            endTime,
            durationMinutes,
            rs.getString("stakes"),
            rs.getString("table_size"),
            handsPlayed,
            rs.getDouble("buy_in"),
            rs.getDouble("cash_out"),
            bbWon,
            rs.getString("tags"),
            rs.getString("notes")
        );
    }

    /**
     * Simple class to hold aggregate statistics.
     */
    public static class SessionStats {
        public final int sessionCount;
        public final double totalProfit;
        public final int totalMinutes;
        public final int totalHands;
        public final double totalBbWon;

        public SessionStats(int sessionCount, double totalProfit, int totalMinutes,
                           int totalHands, double totalBbWon) {
            this.sessionCount = sessionCount;
            this.totalProfit = totalProfit;
            this.totalMinutes = totalMinutes;
            this.totalHands = totalHands;
            this.totalBbWon = totalBbWon;
        }

        public double getHourlyRate() {
            if (totalMinutes > 0) {
                return totalProfit / (totalMinutes / 60.0);
            }
            return 0;
        }

        public double getBbPer100() {
            if (totalHands > 0) {
                return (totalBbWon / totalHands) * 100;
            }
            return 0;
        }
    }
}
