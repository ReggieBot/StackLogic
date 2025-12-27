package com.stacklogic.model;

/**
 * Represents a poker starting hand like "AKs" or "72o".
 *
 * HAND NOTATION EXPLAINED:
 * ========================
 * Poker hands are written as two cards plus a suffix:
 *
 * - Pairs: "AA", "KK", "22" (no suffix needed, obviously same suit impossible)
 * - Suited: "AKs" means both cards are the same suit (e.g., A♠K♠)
 * - Offsuit: "AKo" means cards are different suits (e.g., A♠K♥)
 *
 * THE 13x13 GRID:
 * ===============
 * The range grid has 13 rows and 13 columns (A, K, Q, J, T, 9, 8, 7, 6, 5, 4, 3, 2).
 *
 *     A    K    Q    J    T    9  ...  2
 * A  [AA] [AKs][AQs][AJs][ATs][A9s]...[A2s]  <- Suited hands above diagonal
 * K  [AKo][KK] [KQs][KJs][KTs][K9s]...[K2s]
 * Q  [AQo][KQo][QQ] [QJs][QTs][Q9s]...[Q2s]
 * J  [AJo][KJo][QJo][JJ] [JTs][J9s]...[J2s]
 * T  [ATo][KTo][QTo][JTo][TT] [T9s]...[T2s]
 * 9  [A9o][K9o][Q9o][J9o][T9o][99] ...[92s]
 * .   .    .    .    .    .    .   .   .
 * 2  [A2o][K2o][Q2o][J2o][T2o][92o]...[22]
 *     ^
 *     Offsuit hands below diagonal
 *
 * - Diagonal = pairs (AA, KK, QQ, etc.)
 * - Above diagonal = suited hands
 * - Below diagonal = offsuit hands
 */
public class Hand {

    // The 13 card ranks from highest to lowest
    public static final String[] RANKS = {"A", "K", "Q", "J", "T", "9", "8", "7", "6", "5", "4", "3", "2"};

    private final String notation;  // e.g., "AKs", "72o", "AA"
    private final int row;          // Grid row (0-12)
    private final int col;          // Grid column (0-12)

    public Hand(String notation, int row, int col) {
        this.notation = notation;
        this.row = row;
        this.col = col;
    }

    public String getNotation() {
        return notation;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    /**
     * Check if this hand is a pocket pair (AA, KK, etc.)
     */
    public boolean isPair() {
        return row == col;
    }

    /**
     * Check if this hand is suited (above the diagonal)
     */
    public boolean isSuited() {
        return col > row;
    }

    /**
     * Check if this hand is offsuit (below the diagonal)
     */
    public boolean isOffsuit() {
        return row > col;
    }

    /**
     * Generate the hand notation for a given grid position.
     *
     * @param row Row index (0 = A, 1 = K, ..., 12 = 2)
     * @param col Column index (0 = A, 1 = K, ..., 12 = 2)
     * @return Hand notation like "AKs", "72o", or "AA"
     */
    public static String getNotation(int row, int col) {
        String rank1 = RANKS[row];
        String rank2 = RANKS[col];

        if (row == col) {
            // Pair - no suffix
            return rank1 + rank2;
        } else if (col > row) {
            // Suited - higher card first, add 's'
            return rank1 + rank2 + "s";
        } else {
            // Offsuit - higher card first, add 'o'
            return rank2 + rank1 + "o";
        }
    }

    /**
     * Create a Hand object for a given grid position.
     */
    public static Hand fromGridPosition(int row, int col) {
        return new Hand(getNotation(row, col), row, col);
    }

    /**
     * Generate all 169 starting hands in grid order.
     * Useful for iterating over the entire grid.
     */
    public static Hand[][] generateAllHands() {
        Hand[][] hands = new Hand[13][13];
        for (int row = 0; row < 13; row++) {
            for (int col = 0; col < 13; col++) {
                hands[row][col] = fromGridPosition(row, col);
            }
        }
        return hands;
    }

    @Override
    public String toString() {
        return notation;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Hand hand = (Hand) obj;
        return notation.equals(hand.notation);
    }

    @Override
    public int hashCode() {
        return notation.hashCode();
    }
}
