package com.stacklogic.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Represents a deck of 52 playing cards.
 *
 * DECK OPERATIONS:
 * ================
 * - Create a fresh 52-card deck
 * - Remove specific cards (dead cards)
 * - Deal random cards from remaining deck
 * - Reset to full deck
 *
 * Used for equity calculations where we need to:
 * 1. Remove known cards (hero hand, villain hand, board)
 * 2. Deal random remaining cards for runouts
 */
public class Deck {

    private final List<Card> cards;
    private final Set<Integer> removedCardIds;
    private final Random random;

    public Deck() {
        this.cards = new ArrayList<>(52);
        this.removedCardIds = new HashSet<>();
        this.random = new Random();
        reset();
    }

    public Deck(long seed) {
        this.cards = new ArrayList<>(52);
        this.removedCardIds = new HashSet<>();
        this.random = new Random(seed);
        reset();
    }

    /**
     * Reset deck to full 52 cards
     */
    public void reset() {
        cards.clear();
        removedCardIds.clear();
        for (int i = 0; i < 52; i++) {
            cards.add(Card.fromId(i));
        }
    }

    /**
     * Remove a specific card from the deck (mark as dead)
     */
    public void remove(Card card) {
        removedCardIds.add(card.getId());
        cards.removeIf(c -> c.getId() == card.getId());
    }

    /**
     * Remove multiple cards from the deck
     */
    public void removeAll(List<Card> cardsToRemove) {
        for (Card card : cardsToRemove) {
            remove(card);
        }
    }

    /**
     * Check if a card is still in the deck
     */
    public boolean contains(Card card) {
        return !removedCardIds.contains(card.getId());
    }

    /**
     * Get the number of cards remaining in the deck
     */
    public int size() {
        return cards.size();
    }

    /**
     * Deal one random card from the remaining deck
     */
    public Card dealOne() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("No cards left in deck");
        }
        int index = random.nextInt(cards.size());
        return cards.remove(index);
    }

    /**
     * Deal multiple random cards from the remaining deck
     */
    public List<Card> deal(int count) {
        if (count > cards.size()) {
            throw new IllegalArgumentException("Not enough cards in deck. Requested: " + count + ", available: " + cards.size());
        }
        List<Card> dealt = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            dealt.add(dealOne());
        }
        return dealt;
    }

    /**
     * Shuffle the remaining cards
     */
    public void shuffle() {
        Collections.shuffle(cards, random);
    }

    /**
     * Get a copy of all remaining cards
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * Create a copy of this deck (for running simulations)
     */
    public Deck copy() {
        Deck copy = new Deck();
        copy.cards.clear();
        copy.cards.addAll(this.cards);
        copy.removedCardIds.addAll(this.removedCardIds);
        return copy;
    }

    @Override
    public String toString() {
        return "Deck[" + cards.size() + " cards remaining]";
    }
}
