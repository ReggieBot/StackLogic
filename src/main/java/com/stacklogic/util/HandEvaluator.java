package com.stacklogic.util;

import com.stacklogic.model.Card;
import java.util.*;

/**
 * Evaluates Texas Hold'em poker hands.
 *
 * HAND RANKINGS (best to worst):
 * ==============================
 * 1. Royal Flush     - A-K-Q-J-T all same suit
 * 2. Straight Flush  - 5 consecutive cards, same suit
 * 3. Four of a Kind  - 4 cards of same rank
 * 4. Full House      - 3 of a kind + pair
 * 5. Flush           - 5 cards same suit
 * 6. Straight        - 5 consecutive cards
 * 7. Three of a Kind - 3 cards of same rank
 * 8. Two Pair        - 2 different pairs
 * 9. One Pair        - 2 cards of same rank
 * 10. High Card      - Nothing, highest card wins
 *
 * EVALUATION APPROACH:
 * ====================
 * We use a single integer to represent hand strength where higher = better.
 * The format is: RRKKKKK where:
 * - RR = hand rank (1-10, multiplied by large factor)
 * - KKKKK = kickers for tiebreaking
 */
public class HandEvaluator {

    // Hand type constants
    public static final int HIGH_CARD = 1;
    public static final int ONE_PAIR = 2;
    public static final int TWO_PAIR = 3;
    public static final int THREE_OF_KIND = 4;
    public static final int STRAIGHT = 5;
    public static final int FLUSH = 6;
    public static final int FULL_HOUSE = 7;
    public static final int FOUR_OF_KIND = 8;
    public static final int STRAIGHT_FLUSH = 9;
    public static final int ROYAL_FLUSH = 10;

    // Multiplier to separate hand types
    private static final int TYPE_MULT = 1_000_000;
    private static final int KICKER_MULT = 13;

    /**
     * Evaluate a 7-card hand (2 hole + 5 board) and return a score.
     * Higher score = better hand.
     */
    public static int evaluate(List<Card> cards) {
        if (cards.size() < 5 || cards.size() > 7) {
            throw new IllegalArgumentException("Need 5-7 cards to evaluate, got: " + cards.size());
        }

        // Sort by rank descending
        List<Card> sorted = new ArrayList<>(cards);
        sorted.sort((a, b) -> Integer.compare(b.getRank(), a.getRank()));

        // Count ranks and suits
        int[] rankCounts = new int[13];
        int[] suitCounts = new int[4];
        for (Card c : cards) {
            rankCounts[c.getRank()]++;
            suitCounts[c.getSuit()]++;
        }

        // Check for flush (5+ cards of same suit)
        int flushSuit = -1;
        for (int s = 0; s < 4; s++) {
            if (suitCounts[s] >= 5) {
                flushSuit = s;
                break;
            }
        }

        // Check for straight
        int straightHigh = findStraightHigh(rankCounts);

        // Check for straight flush / royal flush
        if (flushSuit >= 0) {
            int sfHigh = findStraightFlushHigh(sorted, flushSuit);
            if (sfHigh >= 0) {
                if (sfHigh == Card.ACE) {
                    return ROYAL_FLUSH * TYPE_MULT;
                }
                return STRAIGHT_FLUSH * TYPE_MULT + sfHigh;
            }
        }

        // Find quads, trips, pairs
        List<Integer> quads = new ArrayList<>();
        List<Integer> trips = new ArrayList<>();
        List<Integer> pairs = new ArrayList<>();
        List<Integer> singles = new ArrayList<>();

        for (int r = 12; r >= 0; r--) {  // High to low
            if (rankCounts[r] == 4) quads.add(r);
            else if (rankCounts[r] == 3) trips.add(r);
            else if (rankCounts[r] == 2) pairs.add(r);
            else if (rankCounts[r] == 1) singles.add(r);
        }

        // Four of a kind
        if (!quads.isEmpty()) {
            int quadRank = quads.get(0);
            int kicker = findBestKicker(sorted, quadRank, 1);
            return FOUR_OF_KIND * TYPE_MULT + quadRank * KICKER_MULT + kicker;
        }

        // Full house
        if (!trips.isEmpty() && (!pairs.isEmpty() || trips.size() >= 2)) {
            int tripRank = trips.get(0);
            int pairRank;
            if (trips.size() >= 2) {
                pairRank = trips.get(1);
            } else {
                pairRank = pairs.get(0);
            }
            return FULL_HOUSE * TYPE_MULT + tripRank * KICKER_MULT + pairRank;
        }

        // Flush
        if (flushSuit >= 0) {
            int flushValue = evaluateFlush(sorted, flushSuit);
            return FLUSH * TYPE_MULT + flushValue;
        }

        // Straight
        if (straightHigh >= 0) {
            return STRAIGHT * TYPE_MULT + straightHigh;
        }

        // Three of a kind
        if (!trips.isEmpty()) {
            int tripRank = trips.get(0);
            List<Integer> kickers = findKickers(sorted, tripRank, 3, 2);
            int kickerValue = kickers.get(0) * KICKER_MULT + kickers.get(1);
            return THREE_OF_KIND * TYPE_MULT + tripRank * KICKER_MULT * KICKER_MULT + kickerValue;
        }

        // Two pair
        if (pairs.size() >= 2) {
            int highPair = pairs.get(0);
            int lowPair = pairs.get(1);
            int kicker = findBestKickerExcluding(sorted, highPair, lowPair);
            return TWO_PAIR * TYPE_MULT + highPair * KICKER_MULT * KICKER_MULT + lowPair * KICKER_MULT + kicker;
        }

        // One pair
        if (pairs.size() == 1) {
            int pairRank = pairs.get(0);
            List<Integer> kickers = findKickers(sorted, pairRank, 2, 3);
            int kickerValue = kickers.get(0) * KICKER_MULT * KICKER_MULT + kickers.get(1) * KICKER_MULT + kickers.get(2);
            return ONE_PAIR * TYPE_MULT + pairRank * KICKER_MULT * KICKER_MULT * KICKER_MULT + kickerValue;
        }

        // High card
        int highCardValue = 0;
        int mult = 1;
        for (int i = 4; i >= 0; i--) {
            highCardValue += singles.get(i) * mult;
            mult *= KICKER_MULT;
        }
        return HIGH_CARD * TYPE_MULT + highCardValue;
    }

    /**
     * Find the highest card of a straight, or -1 if no straight
     */
    private static int findStraightHigh(int[] rankCounts) {
        // Check A-high down to 5-high (wheel)
        for (int high = 12; high >= 4; high--) {
            boolean isStraight = true;
            for (int i = 0; i < 5; i++) {
                if (rankCounts[high - i] == 0) {
                    isStraight = false;
                    break;
                }
            }
            if (isStraight) return high;
        }

        // Check wheel (A-2-3-4-5)
        if (rankCounts[Card.ACE] > 0 && rankCounts[Card.TWO] > 0 &&
            rankCounts[Card.THREE] > 0 && rankCounts[Card.FOUR] > 0 &&
            rankCounts[Card.FIVE] > 0) {
            return Card.FIVE;  // 5-high straight
        }

        return -1;
    }

    /**
     * Find the highest card of a straight flush in the given suit
     */
    private static int findStraightFlushHigh(List<Card> cards, int suit) {
        // Get ranks that have this suit
        boolean[] hasRank = new boolean[13];
        for (Card c : cards) {
            if (c.getSuit() == suit) {
                hasRank[c.getRank()] = true;
            }
        }

        // Check for straight within this suit
        for (int high = 12; high >= 4; high--) {
            boolean isStraight = true;
            for (int i = 0; i < 5; i++) {
                if (!hasRank[high - i]) {
                    isStraight = false;
                    break;
                }
            }
            if (isStraight) return high;
        }

        // Check wheel
        if (hasRank[Card.ACE] && hasRank[Card.TWO] && hasRank[Card.THREE] &&
            hasRank[Card.FOUR] && hasRank[Card.FIVE]) {
            return Card.FIVE;
        }

        return -1;
    }

    /**
     * Evaluate a flush - returns value based on 5 highest cards of suit
     */
    private static int evaluateFlush(List<Card> cards, int suit) {
        List<Integer> flushRanks = new ArrayList<>();
        for (Card c : cards) {
            if (c.getSuit() == suit) {
                flushRanks.add(c.getRank());
            }
        }
        flushRanks.sort(Collections.reverseOrder());

        int value = 0;
        int mult = 1;
        for (int i = 4; i >= 0; i--) {
            value += flushRanks.get(i) * mult;
            mult *= KICKER_MULT;
        }
        return value;
    }

    /**
     * Find best kicker excluding cards of excludeRank
     */
    private static int findBestKicker(List<Card> sorted, int excludeRank, int count) {
        for (Card c : sorted) {
            if (c.getRank() != excludeRank) {
                return c.getRank();
            }
        }
        return 0;
    }

    /**
     * Find best kicker excluding two ranks
     */
    private static int findBestKickerExcluding(List<Card> sorted, int exclude1, int exclude2) {
        for (Card c : sorted) {
            if (c.getRank() != exclude1 && c.getRank() != exclude2) {
                return c.getRank();
            }
        }
        return 0;
    }

    /**
     * Find multiple kickers excluding a specific rank
     */
    private static List<Integer> findKickers(List<Card> sorted, int excludeRank, int excludeCount, int kickerCount) {
        List<Integer> kickers = new ArrayList<>();
        int excluded = 0;
        for (Card c : sorted) {
            if (c.getRank() == excludeRank && excluded < excludeCount) {
                excluded++;
                continue;
            }
            kickers.add(c.getRank());
            if (kickers.size() == kickerCount) break;
        }
        // Pad with zeros if needed
        while (kickers.size() < kickerCount) {
            kickers.add(0);
        }
        return kickers;
    }

    /**
     * Get a human-readable name for the hand type
     */
    public static String getHandTypeName(int score) {
        int handType = score / TYPE_MULT;
        switch (handType) {
            case ROYAL_FLUSH: return "Royal Flush";
            case STRAIGHT_FLUSH: return "Straight Flush";
            case FOUR_OF_KIND: return "Four of a Kind";
            case FULL_HOUSE: return "Full House";
            case FLUSH: return "Flush";
            case STRAIGHT: return "Straight";
            case THREE_OF_KIND: return "Three of a Kind";
            case TWO_PAIR: return "Two Pair";
            case ONE_PAIR: return "One Pair";
            default: return "High Card";
        }
    }

    /**
     * Get just the hand type from a score
     */
    public static int getHandType(int score) {
        return score / TYPE_MULT;
    }
}
