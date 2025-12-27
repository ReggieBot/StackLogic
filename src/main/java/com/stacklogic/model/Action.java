package com.stacklogic.model;

/**
 * Represents possible actions for a hand in a range.
 *
 * Each action has:
 * - A display name for the UI
 * - A CSS class for styling (color-coding in the grid)
 */
public enum Action {
    RAISE("Raise", "hand-raise"),       // Open raise (green)
    CALL("Call", "hand-call"),          // Call a raise (yellow)
    FOLD("Fold", "hand-fold"),          // Fold (gray)
    THREE_BET("3-Bet", "hand-3bet");    // Re-raise (red)

    private final String displayName;
    private final String cssClass;

    Action(String displayName, String cssClass) {
        this.displayName = displayName;
        this.cssClass = cssClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the CSS class to apply for this action.
     * These classes are defined in style.css
     */
    public String getCssClass() {
        return cssClass;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
