package com.stacklogic.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a poker session.
 *
 * MODEL CLASS EXPLAINED:
 * ======================
 * A model class is a "plain old Java object" (POJO) that holds data.
 * It has:
 *   - Private fields to store data
 *   - Getters to read data
 *   - Setters to modify data
 *   - Optional: computed properties (like profit = cashOut - buyIn)
 *
 * This class maps directly to a row in the "sessions" database table.
 */
public class Session {

    // Database ID (-1 means not yet saved to database)
    private int id = -1;

    // Session details
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;  // Integer allows null

    // Game info
    private String stakes;       // "2NL", "5NL", etc.
    private String tableSize;    // "6max", "4max", "HU"
    private Integer handsPlayed;

    // Money
    private double buyIn;
    private double cashOut;
    private Double bbWon;        // Double allows null

    // Notes
    private String tags;         // Comma-separated: "tilted,ran bad"
    private String notes;

    // Formatters for date/time parsing
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Default constructor for creating new sessions.
     */
    public Session() {
        this.date = LocalDate.now();
    }

    /**
     * Constructor with all fields (used when loading from database).
     */
    public Session(int id, LocalDate date, LocalTime startTime, LocalTime endTime,
                   Integer durationMinutes, String stakes, String tableSize,
                   Integer handsPlayed, double buyIn, double cashOut,
                   Double bbWon, String tags, String notes) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.stakes = stakes;
        this.tableSize = tableSize;
        this.handsPlayed = handsPlayed;
        this.buyIn = buyIn;
        this.cashOut = cashOut;
        this.bbWon = bbWon;
        this.tags = tags;
        this.notes = notes;
    }

    // ==================== COMPUTED PROPERTIES ====================

    /**
     * Calculate profit/loss (auto-computed from buy-in and cash-out).
     */
    public double getProfit() {
        return cashOut - buyIn;
    }

    /**
     * Calculate BB/100 if we have hands played and bb won.
     * BB/100 = (big blinds won / hands played) * 100
     */
    public Double getBbPer100() {
        if (bbWon != null && handsPlayed != null && handsPlayed > 0) {
            return (bbWon / handsPlayed) * 100;
        }
        return null;
    }

    /**
     * Calculate hourly rate if we have duration.
     * Hourly = (profit / duration in hours)
     */
    public Double getHourlyRate() {
        if (durationMinutes != null && durationMinutes > 0) {
            double hours = durationMinutes / 60.0;
            return getProfit() / hours;
        }
        return null;
    }

    /**
     * Auto-calculate duration from start/end times if both are set.
     */
    public void calculateDurationFromTimes() {
        if (startTime != null && endTime != null) {
            int startMinutes = startTime.getHour() * 60 + startTime.getMinute();
            int endMinutes = endTime.getHour() * 60 + endTime.getMinute();

            // Handle sessions that cross midnight
            if (endMinutes < startMinutes) {
                endMinutes += 24 * 60;
            }

            this.durationMinutes = endMinutes - startMinutes;
        }
    }

    // ==================== GETTERS AND SETTERS ====================

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

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getStakes() {
        return stakes;
    }

    public void setStakes(String stakes) {
        this.stakes = stakes;
    }

    public String getTableSize() {
        return tableSize;
    }

    public void setTableSize(String tableSize) {
        this.tableSize = tableSize;
    }

    public Integer getHandsPlayed() {
        return handsPlayed;
    }

    public void setHandsPlayed(Integer handsPlayed) {
        this.handsPlayed = handsPlayed;
    }

    public double getBuyIn() {
        return buyIn;
    }

    public void setBuyIn(double buyIn) {
        this.buyIn = buyIn;
    }

    public double getCashOut() {
        return cashOut;
    }

    public void setCashOut(double cashOut) {
        this.cashOut = cashOut;
    }

    public Double getBbWon() {
        return bbWon;
    }

    public void setBbWon(Double bbWon) {
        this.bbWon = bbWon;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Check if this session has been saved to the database.
     */
    public boolean isSaved() {
        return id > 0;
    }

    /**
     * Format duration as "Xh Ym" string.
     */
    public String getDurationFormatted() {
        if (durationMinutes == null) return "";
        int hours = durationMinutes / 60;
        int mins = durationMinutes % 60;
        if (hours > 0) {
            return String.format("%dh %dm", hours, mins);
        }
        return String.format("%dm", mins);
    }

    @Override
    public String toString() {
        return String.format("Session[%s, %s, %s, $%.2f -> $%.2f, P/L: $%.2f]",
                date, stakes, tableSize, buyIn, cashOut, getProfit());
    }
}
