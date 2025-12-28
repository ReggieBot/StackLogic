package com.stacklogic.util;

import com.stacklogic.model.Card;
import com.stacklogic.model.Deck;
import com.stacklogic.model.Range;
import com.stacklogic.model.Action;
import com.stacklogic.model.Hand;

import java.util.*;
import java.util.concurrent.*;

/**
 * Monte Carlo equity calculator for Texas Hold'em.
 *
 * EQUITY CALCULATION:
 * ===================
 * Equity = probability of winning the hand
 *
 * We run many random simulations:
 * 1. Set up known cards (hero hand, board, optionally villain hand)
 * 2. If villain hand unknown, pick random hand from their range
 * 3. Deal remaining board cards randomly
 * 4. Evaluate both hands
 * 5. Count wins, ties, losses
 *
 * After many trials, equity = (wins + ties/2) / total_trials
 */
public class EquityCalculator {

    private static final int DEFAULT_TRIALS = 20000;
    private static final ExecutorService executor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
    );

    /**
     * Result of an equity calculation
     */
    public static class EquityResult {
        public final double heroEquity;      // 0.0 - 1.0
        public final double villainEquity;
        public final int wins;
        public final int ties;
        public final int losses;
        public final int trials;
        public final String heroHandType;    // Most common hand type
        public final String villainHandType;

        public EquityResult(double heroEquity, double villainEquity, int wins, int ties, int losses,
                           int trials, String heroHandType, String villainHandType) {
            this.heroEquity = heroEquity;
            this.villainEquity = villainEquity;
            this.wins = wins;
            this.ties = ties;
            this.losses = losses;
            this.trials = trials;
            this.heroHandType = heroHandType;
            this.villainHandType = villainHandType;
        }

        public double getHeroEquityPercent() {
            return heroEquity * 100;
        }

        public double getVillainEquityPercent() {
            return villainEquity * 100;
        }

        @Override
        public String toString() {
            return String.format("Hero: %.1f%% (W:%d T:%d L:%d) vs Villain: %.1f%%",
                getHeroEquityPercent(), wins, ties, losses, getVillainEquityPercent());
        }
    }

    /**
     * Calculate equity: Hero hand vs Villain hand on a given board
     *
     * @param heroCards   Hero's 2 hole cards
     * @param villainCards Villain's 2 hole cards
     * @param board       Community cards (0-5 cards)
     * @return EquityResult with win/tie/loss breakdown
     */
    public static EquityResult calculate(List<Card> heroCards, List<Card> villainCards, List<Card> board) {
        return calculate(heroCards, villainCards, board, DEFAULT_TRIALS);
    }

    public static EquityResult calculate(List<Card> heroCards, List<Card> villainCards,
                                        List<Card> board, int trials) {
        if (heroCards.size() != 2) {
            throw new IllegalArgumentException("Hero must have exactly 2 cards");
        }
        if (villainCards.size() != 2) {
            throw new IllegalArgumentException("Villain must have exactly 2 cards");
        }
        if (board.size() > 5) {
            throw new IllegalArgumentException("Board can have at most 5 cards");
        }

        // Check for duplicate cards
        Set<Integer> usedCards = new HashSet<>();
        for (Card c : heroCards) {
            if (!usedCards.add(c.getId())) {
                throw new IllegalArgumentException("Duplicate card: " + c);
            }
        }
        for (Card c : villainCards) {
            if (!usedCards.add(c.getId())) {
                throw new IllegalArgumentException("Duplicate card: " + c);
            }
        }
        for (Card c : board) {
            if (!usedCards.add(c.getId())) {
                throw new IllegalArgumentException("Duplicate card: " + c);
            }
        }

        int wins = 0;
        int ties = 0;
        int losses = 0;
        Map<Integer, Integer> heroHandTypes = new HashMap<>();
        Map<Integer, Integer> villainHandTypes = new HashMap<>();

        Random random = new Random();
        int cardsNeeded = 5 - board.size();

        for (int i = 0; i < trials; i++) {
            // Create deck and remove known cards
            Deck deck = new Deck(random.nextLong());
            deck.removeAll(heroCards);
            deck.removeAll(villainCards);
            deck.removeAll(board);

            // Deal remaining board cards
            List<Card> fullBoard = new ArrayList<>(board);
            if (cardsNeeded > 0) {
                fullBoard.addAll(deck.deal(cardsNeeded));
            }

            // Build complete hands (2 hole + 5 board)
            List<Card> heroFull = new ArrayList<>(heroCards);
            heroFull.addAll(fullBoard);

            List<Card> villainFull = new ArrayList<>(villainCards);
            villainFull.addAll(fullBoard);

            // Evaluate hands
            int heroScore = HandEvaluator.evaluate(heroFull);
            int villainScore = HandEvaluator.evaluate(villainFull);

            // Track hand types
            int heroType = HandEvaluator.getHandType(heroScore);
            int villainType = HandEvaluator.getHandType(villainScore);
            heroHandTypes.merge(heroType, 1, Integer::sum);
            villainHandTypes.merge(villainType, 1, Integer::sum);

            // Compare
            if (heroScore > villainScore) {
                wins++;
            } else if (heroScore < villainScore) {
                losses++;
            } else {
                ties++;
            }
        }

        double heroEquity = (wins + ties * 0.5) / trials;
        double villainEquity = 1.0 - heroEquity;

        // Find most common hand types
        String heroHandType = getMostCommonHandType(heroHandTypes);
        String villainHandType = getMostCommonHandType(villainHandTypes);

        return new EquityResult(heroEquity, villainEquity, wins, ties, losses,
                               trials, heroHandType, villainHandType);
    }

    /**
     * Calculate equity: Hero hand vs a Range of hands
     *
     * @param heroCards Hero's 2 hole cards
     * @param villainRange Villain's range (set of hands they could have)
     * @param board Community cards (0-5)
     * @return EquityResult averaged across all hands in range
     */
    public static EquityResult calculateVsRange(List<Card> heroCards, Range villainRange,
                                                List<Card> board) {
        return calculateVsRange(heroCards, villainRange, board, DEFAULT_TRIALS);
    }

    public static EquityResult calculateVsRange(List<Card> heroCards, Range villainRange,
                                                List<Card> board, int trials) {
        if (heroCards.size() != 2) {
            throw new IllegalArgumentException("Hero must have exactly 2 cards");
        }

        // Get all possible villain hands from the range
        List<List<Card>> villainHands = getHandCombosFromRange(villainRange, heroCards, board);

        if (villainHands.isEmpty()) {
            throw new IllegalArgumentException("No valid villain hands in range (all blocked)");
        }

        // Distribute trials across villain hands
        int trialsPerHand = Math.max(1, trials / villainHands.size());

        int totalWins = 0;
        int totalTies = 0;
        int totalLosses = 0;
        int totalTrials = 0;
        Map<Integer, Integer> heroHandTypes = new HashMap<>();
        Map<Integer, Integer> villainHandTypes = new HashMap<>();

        Random random = new Random();

        for (List<Card> villainCards : villainHands) {
            int cardsNeeded = 5 - board.size();

            for (int i = 0; i < trialsPerHand; i++) {
                Deck deck = new Deck(random.nextLong());
                deck.removeAll(heroCards);
                deck.removeAll(villainCards);
                deck.removeAll(board);

                List<Card> fullBoard = new ArrayList<>(board);
                if (cardsNeeded > 0) {
                    fullBoard.addAll(deck.deal(cardsNeeded));
                }

                List<Card> heroFull = new ArrayList<>(heroCards);
                heroFull.addAll(fullBoard);

                List<Card> villainFull = new ArrayList<>(villainCards);
                villainFull.addAll(fullBoard);

                int heroScore = HandEvaluator.evaluate(heroFull);
                int villainScore = HandEvaluator.evaluate(villainFull);

                int heroType = HandEvaluator.getHandType(heroScore);
                int villainType = HandEvaluator.getHandType(villainScore);
                heroHandTypes.merge(heroType, 1, Integer::sum);
                villainHandTypes.merge(villainType, 1, Integer::sum);

                if (heroScore > villainScore) {
                    totalWins++;
                } else if (heroScore < villainScore) {
                    totalLosses++;
                } else {
                    totalTies++;
                }
                totalTrials++;
            }
        }

        double heroEquity = (totalWins + totalTies * 0.5) / totalTrials;
        double villainEquity = 1.0 - heroEquity;

        String heroHandType = getMostCommonHandType(heroHandTypes);
        String villainHandType = getMostCommonHandType(villainHandTypes);

        return new EquityResult(heroEquity, villainEquity, totalWins, totalTies, totalLosses,
                               totalTrials, heroHandType, villainHandType);
    }

    /**
     * Convert a Range (abstract hand categories like "AKs") to concrete card combinations
     */
    private static List<List<Card>> getHandCombosFromRange(Range range, List<Card> heroCards, List<Card> board) {
        List<List<Card>> combos = new ArrayList<>();
        Set<Integer> blockedCards = new HashSet<>();

        for (Card c : heroCards) blockedCards.add(c.getId());
        for (Card c : board) blockedCards.add(c.getId());

        // Iterate through all 169 starting hands
        for (int row = 0; row < 13; row++) {
            for (int col = 0; col < 13; col++) {
                String notation = Hand.getNotation(row, col);
                Action action = range.getHandAction(notation);

                if (action == Action.FOLD) continue;

                // Generate all specific card combos for this hand type
                List<List<Card>> handCombos = generateCombos(row, col, blockedCards);
                combos.addAll(handCombos);
            }
        }

        return combos;
    }

    /**
     * Generate all 2-card combinations for a hand notation (e.g., "AKs" -> all suited AK combos)
     */
    private static List<List<Card>> generateCombos(int row, int col, Set<Integer> blockedCards) {
        List<List<Card>> combos = new ArrayList<>();
        int rank1 = 12 - row;  // Convert grid position to rank (A=12 is row 0)
        int rank2 = 12 - col;

        if (row == col) {
            // Pocket pair - 6 combos
            for (int s1 = 0; s1 < 4; s1++) {
                for (int s2 = s1 + 1; s2 < 4; s2++) {
                    Card c1 = new Card(rank1, s1);
                    Card c2 = new Card(rank1, s2);
                    if (!blockedCards.contains(c1.getId()) && !blockedCards.contains(c2.getId())) {
                        combos.add(Arrays.asList(c1, c2));
                    }
                }
            }
        } else if (col > row) {
            // Suited - 4 combos
            for (int s = 0; s < 4; s++) {
                Card c1 = new Card(rank1, s);
                Card c2 = new Card(rank2, s);
                if (!blockedCards.contains(c1.getId()) && !blockedCards.contains(c2.getId())) {
                    combos.add(Arrays.asList(c1, c2));
                }
            }
        } else {
            // Offsuit - 12 combos
            for (int s1 = 0; s1 < 4; s1++) {
                for (int s2 = 0; s2 < 4; s2++) {
                    if (s1 == s2) continue;
                    Card c1 = new Card(rank1, s1);
                    Card c2 = new Card(rank2, s2);
                    if (!blockedCards.contains(c1.getId()) && !blockedCards.contains(c2.getId())) {
                        combos.add(Arrays.asList(c1, c2));
                    }
                }
            }
        }

        return combos;
    }

    private static String getMostCommonHandType(Map<Integer, Integer> handTypes) {
        return handTypes.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(e -> HandEvaluator.getHandTypeName(e.getKey() * 1_000_000))
            .orElse("Unknown");
    }

    /**
     * Shutdown the executor service (call on app exit)
     */
    public static void shutdown() {
        executor.shutdown();
    }
}
