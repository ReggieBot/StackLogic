package com.stacklogic.util;

import com.stacklogic.model.Note;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Note database operations.
 *
 * Provides CRUD operations for the hand_notes table.
 * All methods are static since we're using a singleton database connection.
 */
public class NoteDAO {

    /**
     * Create a new note.
     * Returns the generated ID.
     */
    public static int create(Note note) throws SQLException {
        String sql = """
            INSERT INTO hand_notes (date, tag, note_text)
            VALUES (?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, note.getDate().toString());
            pstmt.setString(2, note.getTag());
            pstmt.setString(3, note.getNoteText());

            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    note.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Update an existing note.
     */
    public static void update(Note note) throws SQLException {
        String sql = """
            UPDATE hand_notes SET
                date = ?, tag = ?, note_text = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, note.getDate().toString());
            pstmt.setString(2, note.getTag());
            pstmt.setString(3, note.getNoteText());
            pstmt.setInt(4, note.getId());

            pstmt.executeUpdate();
        }
    }

    /**
     * Delete a note by ID.
     */
    public static void delete(int id) throws SQLException {
        String sql = "DELETE FROM hand_notes WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Get a note by ID.
     */
    public static Note getById(int id) throws SQLException {
        String sql = "SELECT * FROM hand_notes WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNote(rs);
                }
            }
        }
        return null;
    }

    /**
     * Get all notes, ordered by date (newest first).
     */
    public static List<Note> getAll() throws SQLException {
        String sql = "SELECT * FROM hand_notes ORDER BY date DESC, id DESC";
        List<Note> notes = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
        }
        return notes;
    }

    /**
     * Get notes filtered by tag.
     */
    public static List<Note> getByTag(String tag) throws SQLException {
        String sql = "SELECT * FROM hand_notes WHERE tag = ? ORDER BY date DESC";
        List<Note> notes = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tag);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notes.add(mapResultSetToNote(rs));
                }
            }
        }
        return notes;
    }

    /**
     * Search notes by text content.
     */
    public static List<Note> search(String searchText) throws SQLException {
        String sql = "SELECT * FROM hand_notes WHERE note_text LIKE ? ORDER BY date DESC";
        List<Note> notes = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + searchText + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notes.add(mapResultSetToNote(rs));
                }
            }
        }
        return notes;
    }

    /**
     * Get distinct tags used in notes.
     */
    public static List<String> getAllTags() throws SQLException {
        String sql = "SELECT DISTINCT tag FROM hand_notes WHERE tag IS NOT NULL AND tag != '' ORDER BY tag";
        List<String> tags = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tags.add(rs.getString("tag"));
            }
        }
        return tags;
    }

    /**
     * Convert a database row to a Note object.
     */
    private static Note mapResultSetToNote(ResultSet rs) throws SQLException {
        LocalDate date = LocalDate.parse(rs.getString("date"));

        String createdAtStr = rs.getString("created_at");
        LocalDateTime createdAt = null;
        if (createdAtStr != null && !createdAtStr.isEmpty()) {
            // SQLite stores as "YYYY-MM-DD HH:MM:SS"
            try {
                createdAt = LocalDateTime.parse(createdAtStr.replace(" ", "T"));
            } catch (Exception e) {
                // Ignore parse errors for created_at
            }
        }

        return new Note(
            rs.getInt("id"),
            date,
            rs.getString("tag"),
            rs.getString("note_text"),
            createdAt
        );
    }
}
