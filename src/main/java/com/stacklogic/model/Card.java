package com.stacklogic.model;

/**
 * Represents a single playing card with rank and suit.
 *
 * CARD REPRESENTATION:
 * ====================
 * Each card has two components:
 * - Rank: 2-10, J, Q, K, A (internally 0-12, where 0=2 and 12=A)
 * - Suit: Hearts, Diamonds, Clubs, Spades (0-3)
 *
 * Card notation examples:
 * - "Ah" = Ace of hearts
 * - "Kd" = King of diamonds
 * - "2c" = Two of clubs
 * - "Ts" = Ten of spades (T is used for 10)
 */
public class Card implements Comparable<Card> {

    // Rank constants (index in RANKS array)
    public static final int TWO = 0;
    public static final int THREE = 1;
    public static final int FOUR = 2;
    public static final int FIVE = 3;
    public static final int SIX = 4;
    public static final int SEVEN = 5;
    public static final int EIGHT = 6;
    public static final int NINE = 7;
    public static final int TEN = 8;
    public static final int JACK = 9;
    public static final int QUEEN = 10;
    public static final int KING = 11;
    public static final int ACE = 12;

    // Suit constants
    public static final int HEARTS = 0;
    public static final int DIAMONDS = 1;
    public static final int CLUBS = 2;
    public static final int SPADES = 3;

    // Display strings
    public static final String[] RANK_SYMBOLS = {"2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A"};
    public static final String[] SUIT_SYMBOLS = {"h", "d", "c", "s"};
    public static final String[] SUIT_UNICODE = {"♥", "♦", "♣", "♠"};
    public static final String[] RANK_NAMES = {"Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Jack", "Queen", "King", "Ace"};
    public static final String[] SUIT_NAMES = {"Hearts", "Diamonds", "Clubs", "Spades"};

    private final int rank;  // 0-12 (2-A)
    private final int suit;  // 0-3 (h, d, c, s)

    public Card(int rank, int suit) {
        if (rank < 0 || rank > 12) {
            throw new IllegalArgumentException("Rank must be 0-12, got: " + rank);
        }
        if (suit < 0 || suit > 3) {
            throw new IllegalArgumentException("Suit must be 0-3, got: " + suit);
        }
        this.rank = rank;
        this.suit = suit;
    }

    /**
     * Parse a card from notation like "Ah", "Kd", "2c", "Ts"
     */
    public static Card fromString(String notation) {
        if (notation == null || notation.length() != 2) {
            throw new IllegalArgumentException("Card notation must be 2 characters: " + notation);
        }

        String rankChar = notation.substring(0, 1).toUpperCase();
        String suitChar = notation.substring(1, 2).toLowerCase();

        int rank = -1;
        for (int i = 0; i < RANK_SYMBOLS.length; i++) {
            if (RANK_SYMBOLS[i].equals(rankChar)) {
                rank = i;
                break;
            }
        }
        if (rank == -1) {
            throw new IllegalArgumentException("Invalid rank: " + rankChar);
        }

        int suit = -1;
        for (int i = 0; i < SUIT_SYMBOLS.length; i++) {
            if (SUIT_SYMBOLS[i].equals(suitChar)) {
                suit = i;
                break;
            }
        }
        if (suit == -1) {
            throw new IllegalArgumentException("Invalid suit: " + suitChar);
        }

        return new Card(rank, suit);
    }

    public int getRank() {
        return rank;
    }

    public int getSuit() {
        return suit;
    }

    public String getRankSymbol() {
        return RANK_SYMBOLS[rank];
    }

    public String getSuitSymbol() {
        return SUIT_SYMBOLS[suit];
    }

    public String getSuitUnicode() {
        return SUIT_UNICODE[suit];
    }

    public boolean isRed() {
        return suit == HEARTS || suit == DIAMONDS;
    }

    public boolean isBlack() {
        return suit == CLUBS || suit == SPADES;
    }

    /**
     * Returns notation like "Ah", "Kd"
     */
    public String getNotation() {
        return RANK_SYMBOLS[rank] + SUIT_SYMBOLS[suit];
    }

    /**
     * Returns display string like "A♥", "K♦"
     */
    public String getDisplay() {
        return RANK_SYMBOLS[rank] + SUIT_UNICODE[suit];
    }

    /**
     * Unique integer ID for this card (0-51)
     */
    public int getId() {
        return rank * 4 + suit;
    }

    /**
     * Create a card from its ID (0-51)
     */
    public static Card fromId(int id) {
        if (id < 0 || id > 51) {
            throw new IllegalArgumentException("Card ID must be 0-51, got: " + id);
        }
        return new Card(id / 4, id % 4);
    }

    @Override
    public int compareTo(Card other) {
        // Compare by rank first (higher is better), then by suit
        if (this.rank != other.rank) {
            return Integer.compare(this.rank, other.rank);
        }
        return Integer.compare(this.suit, other.suit);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return rank == card.rank && suit == card.suit;
    }

    @Override
    public int hashCode() {
        return getId();
    }

    @Override
    public String toString() {
        return getNotation();
    }
}
