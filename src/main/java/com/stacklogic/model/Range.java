package com.stacklogic.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a poker range - a mapping of hands to actions.
 *
 * WHAT IS A RANGE?
 * ================
 * A range is the set of hands you would play in a certain way from a certain position.
 * For example, your "UTG RFI range" might include:
 *   - RAISE: AA, KK, QQ, JJ, TT, AKs, AQs, AKo, etc.
 *   - FOLD: Everything else
 *
 * This class stores which action to take with each of the 169 starting hands.
 * Hands not in the range are assumed to be FOLD.
 */
public class Range {

    private final String name;                    // e.g., "UTG RFI", "BTN vs CO raise"
    private final Position position;              // Your position
    private final String situation;               // "RFI" or "vs UTG" etc.
    private final Map<String, Action> hands;      // Hand notation -> Action

    public Range(String name, Position position, String situation) {
        this.name = name;
        this.position = position;
        this.situation = situation;
        this.hands = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }

    public String getSituation() {
        return situation;
    }

    /**
     * Set the action for a specific hand.
     *
     * @param handNotation The hand notation (e.g., "AKs", "72o")
     * @param action The action to take with this hand
     */
    public void setHandAction(String handNotation, Action action) {
        hands.put(handNotation.toUpperCase(), action);
    }

    /**
     * Get the action for a specific hand.
     * Returns FOLD if the hand is not in the range.
     *
     * @param handNotation The hand notation
     * @return The action for this hand
     */
    public Action getHandAction(String handNotation) {
        return hands.getOrDefault(handNotation.toUpperCase(), Action.FOLD);
    }

    /**
     * Get the action for a hand at a specific grid position.
     */
    public Action getHandAction(int row, int col) {
        String notation = Hand.getNotation(row, col);
        return getHandAction(notation);
    }

    /**
     * Check if a hand is in this range (not folded).
     */
    public boolean containsHand(String handNotation) {
        Action action = getHandAction(handNotation);
        return action != Action.FOLD;
    }

    /**
     * Get the number of hands that aren't folded.
     */
    public int getHandCount() {
        return (int) hands.values().stream()
                .filter(action -> action != Action.FOLD)
                .count();
    }

    /**
     * Calculate what percentage of hands are played (not folded).
     * There are 169 unique starting hands.
     */
    public double getPlayPercentage() {
        return (getHandCount() / 169.0) * 100;
    }

    @Override
    public String toString() {
        return name + " (" + String.format("%.1f", getPlayPercentage()) + "% of hands)";
    }
}
