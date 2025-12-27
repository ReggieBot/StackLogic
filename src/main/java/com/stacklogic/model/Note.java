package com.stacklogic.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class for poker study notes.
 *
 * Notes are quick thoughts, hand analyses, or study reminders
 * that players can jot down during or after sessions.
 */
public class Note {

    private int id;
    private LocalDate date;
    private String tag;       // Category: "Preflop", "Postflop", "Mindset", "Opponent", etc.
    private String noteText;  // The actual note content
    private LocalDateTime createdAt;

    // Constructor for new notes (no ID yet)
    public Note(LocalDate date, String tag, String noteText) {
        this.date = date;
        this.tag = tag;
        this.noteText = noteText;
    }

    // Constructor for notes loaded from database
    public Note(int id, LocalDate date, String tag, String noteText, LocalDateTime createdAt) {
        this.id = id;
        this.date = date;
        this.tag = tag;
        this.noteText = noteText;
        this.createdAt = createdAt;
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Get a preview of the note (first line or first N characters).
     */
    public String getPreview() {
        if (noteText == null || noteText.isEmpty()) {
            return "";
        }
        // Get first line
        int newlineIndex = noteText.indexOf('\n');
        String firstLine = newlineIndex > 0 ? noteText.substring(0, newlineIndex) : noteText;

        // Truncate if too long
        if (firstLine.length() > 60) {
            return firstLine.substring(0, 57) + "...";
        }
        return firstLine;
    }

    /**
     * Get a title for the note (first line, shorter).
     */
    public String getTitle() {
        String preview = getPreview();
        if (preview.length() > 35) {
            return preview.substring(0, 32) + "...";
        }
        return preview.isEmpty() ? "Untitled Note" : preview;
    }
}
