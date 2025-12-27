package com.stacklogic.model;

/**
 * Represents positions at a poker table.
 *
 * WHAT IS AN ENUM?
 * ================
 * An enum (enumeration) is a special type that represents a fixed set of constants.
 * Instead of using strings like "UTG" or "BTN" scattered throughout your code,
 * you define them once here. This prevents typos and lets the compiler catch errors.
 *
 * POKER POSITIONS EXPLAINED:
 * ==========================
 * Positions are named relative to the dealer button, which rotates each hand.
 * Earlier positions act first and are at a disadvantage (less information).
 *
 * In a 6-max game:
 *   UTG (Under the Gun) - First to act preflop, worst position
 *   MP (Middle Position) - Second to act
 *   CO (Cutoff) - One before the button
 *   BTN (Button) - Best position, acts last postflop
 *   SB (Small Blind) - Posts small blind, acts first postflop
 *   BB (Big Blind) - Posts big blind, closes preflop action
 *
 * As the table gets smaller, early positions are removed first.
 */
public enum Position {
    // Each enum constant has a display name and the minimum table size where it exists
    UTG("UTG", 6),      // Only exists at 6-max or larger
    MP("MP", 5),        // Only exists at 5-max or larger
    CO("CO", 4),        // Only exists at 4-max or larger
    BTN("BTN", 2),      // Always exists (minimum 2 players)
    SB("SB", 2),        // Always exists
    BB("BB", 2);        // Always exists

    private final String displayName;
    private final int minTableSize;

    /**
     * Constructor for enum constants.
     * Each constant (UTG, MP, etc.) calls this with its values.
     */
    Position(String displayName, int minTableSize) {
        this.displayName = displayName;
        this.minTableSize = minTableSize;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinTableSize() {
        return minTableSize;
    }

    /**
     * Check if this position exists at a given table size.
     * For example, UTG doesn't exist at a 4-handed table.
     */
    public boolean existsAtTableSize(int tableSize) {
        return tableSize >= minTableSize;
    }

    /**
     * Get the next position in rotation order.
     * Used for the "Next Position" button.
     */
    public Position nextPosition(int tableSize) {
        Position[] allPositions = Position.values();
        int currentIndex = this.ordinal();

        // Find the next position that exists at this table size
        for (int i = 1; i <= allPositions.length; i++) {
            int nextIndex = (currentIndex + i) % allPositions.length;
            Position next = allPositions[nextIndex];
            if (next.existsAtTableSize(tableSize)) {
                return next;
            }
        }
        return this; // Fallback (shouldn't happen)
    }

    @Override
    public String toString() {
        return displayName;
    }
}
