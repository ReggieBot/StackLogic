package com.stacklogic.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.*;

/**
 * Controller for the Position Trainer / Poker Quiz.
 *
 * QUESTION CATEGORIES:
 * ====================
 * 1. Position - Understanding table positions and relative positions
 * 2. Ranges - Which hands to play from which positions
 * 3. Pot Odds - Should you call based on the pot odds
 * 4. Hand Rankings - Which hand beats which
 * 5. Terminology - Poker terms and their meanings
 * 6. Bet Sizing - Optimal bet sizes in different situations
 * 7. Stack Depth - SPR, effective stacks, commitment thresholds
 *
 * Each question has:
 * - Category
 * - Question text
 * - Optional scenario/context
 * - Optional hand display
 * - 4 answer choices (shuffled)
 * - Correct answer index
 * - Explanation for feedback
 */
public class TrainerController implements Initializable {

    // Stats display
    @FXML private Label correctCountLabel;
    @FXML private Label accuracyLabel;
    @FXML private Label streakLabel;

    // Category toggles
    @FXML private FlowPane categoryPane;
    @FXML private ToggleButton categoryAll;
    @FXML private ToggleButton categoryPosition;
    @FXML private ToggleButton categoryRanges;
    @FXML private ToggleButton categoryOdds;
    @FXML private ToggleButton categoryHands;
    @FXML private ToggleButton categoryTerms;
    @FXML private ToggleButton categorySizing;
    @FXML private ToggleButton categoryStack;

    // Question card
    @FXML private VBox questionCard;
    @FXML private Label questionTypeLabel;
    @FXML private Label positionBadge;
    @FXML private Label scenarioLabel;
    @FXML private HBox handDisplay;
    @FXML private Label card1Label;
    @FXML private Label card2Label;
    @FXML private Label questionLabel;

    // Answers
    @FXML private VBox answersBox;
    @FXML private Button answer1Btn;
    @FXML private Button answer2Btn;
    @FXML private Button answer3Btn;
    @FXML private Button answer4Btn;

    // Feedback
    @FXML private VBox feedbackBox;
    @FXML private Label feedbackLabel;
    @FXML private Button nextBtn;

    // Tip
    @FXML private Label tipLabel;

    // Statistics
    private int totalQuestions = 0;
    private int correctAnswers = 0;
    private int currentStreak = 0;
    private int bestStreak = 0;

    // Question state
    private List<Question> allQuestions;
    private List<Question> filteredQuestions;
    private Question currentQuestion;
    private int correctAnswerIndex;
    private boolean answered = false;
    private Set<String> activeCategories = new HashSet<>();

    private Random random = new Random();

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        // Build the question bank
        allQuestions = buildQuestionBank();
        filteredQuestions = new ArrayList<>(allQuestions);

        // Set "All" as default
        activeCategories.add("All");
        categoryAll.setSelected(true);

        // Load first question
        loadNextQuestion();

        // Show a random tip
        shuffleTip();
    }

    /**
     * Build the comprehensive question bank with all categories.
     */
    private List<Question> buildQuestionBank() {
        List<Question> questions = new ArrayList<>();

        // ============================================
        // POSITION QUESTIONS
        // ============================================
        questions.add(new Question("Position",
            "In a 6-max game, which position acts FIRST preflop?",
            null, null, null,
            new String[]{"UTG (Under the Gun)", "Button", "Small Blind", "Big Blind"},
            0, "UTG acts first preflop. The blinds post forced bets but act last preflop."));

        questions.add(new Question("Position",
            "In a 6-max game, which position acts LAST preflop?",
            null, null, null,
            new String[]{"Big Blind", "Small Blind", "Button", "UTG"},
            0, "The Big Blind acts last preflop because they get to see everyone else's action before deciding."));

        questions.add(new Question("Position",
            "Which position is considered the BEST position at the table?",
            null, null, null,
            new String[]{"Button (BTN)", "Cutoff (CO)", "Big Blind", "UTG"},
            0, "The Button is the best position because you act LAST postflop in every betting round."));

        questions.add(new Question("Position",
            "You're on the Button. How many players will act AFTER you postflop?",
            null, null, null,
            new String[]{"0 - You're always last", "1 - Just the blinds", "2 - Both blinds", "Depends on the hand"},
            0, "On the Button, you always act last postflop. This is a huge advantage."));

        questions.add(new Question("Position",
            "What does 'being in position' mean?",
            null, null, null,
            new String[]{"Acting after your opponent", "Being on the button", "Having a strong hand", "Being first to act"},
            0, "Being 'in position' means you act after your opponent(s), giving you more information."));

        questions.add(new Question("Position",
            "In a heads-up pot, you called a raise from the CO while on the BTN. Are you in position?",
            null, null, null,
            new String[]{"Yes, you act last postflop", "No, you called so you're out of position", "Only on the flop", "Only if you hit"},
            0, "Yes! As the button, you act last in all postflop streets regardless of preflop action."));

        questions.add(new Question("Position",
            "What is the 'Cutoff' position?",
            null, null, null,
            new String[]{"One seat to the right of the Button", "One seat to the left of the Button", "The first to act preflop", "Another name for the Big Blind"},
            0, "The Cutoff (CO) is immediately to the right of the Button, making it the second-best position."));

        questions.add(new Question("Position",
            "Why is playing out of position (OOP) more difficult?",
            null, null, null,
            new String[]{"You have less information when you act", "You can't bluff", "Your cards are weaker", "You must always check"},
            0, "Acting first means you don't know what your opponent will do. This information disadvantage makes decisions harder."));

        questions.add(new Question("Position",
            "In 6-max, which positions are considered 'early position'?",
            null, null, null,
            new String[]{"UTG and UTG+1 (HJ)", "Button and Cutoff", "Small Blind and Big Blind", "Only UTG"},
            0, "In 6-max, UTG and the Hijack (UTG+1) are early position since they act first."));

        questions.add(new Question("Position",
            "You're in the Small Blind. After the flop, when do you act?",
            null, null, null,
            new String[]{"First (before the BB)", "Second (after the BB)", "Last", "Depends who raised preflop"},
            0, "The Small Blind acts first postflop, followed by the Big Blind. This is why the blinds are tough spots."));

        // ============================================
        // RANGE QUESTIONS
        // ============================================
        questions.add(new Question("Ranges",
            "From UTG in 6-max, what percentage of hands should you typically open?",
            null, null, null,
            new String[]{"Around 15-18%", "Around 25-30%", "Around 40-50%", "Around 5-10%"},
            0, "UTG opening ranges are tight, typically 15-18% of hands. You still have 5 players behind you."));

        questions.add(new Question("Ranges",
            "Which of these hands is a CLEAR fold from UTG?",
            "6-max cash game, 100bb deep", null, null,
            new String[]{"J7o", "AQo", "88", "KQs"},
            0, "J7o is way too weak for UTG. It's not even close to playable from early position."));

        questions.add(new Question("Ranges",
            "What does 'suited' add to a hand's value?",
            null, null, null,
            new String[]{"About 3-4% equity on average", "About 10-15% equity", "Nothing significant", "It doubles the hand's value"},
            0, "Suited hands gain roughly 3-4% equity from flush potential. This is why hands like A5s are playable but A5o often isn't."));

        questions.add(new Question("Ranges",
            "From the Button, how wide can you profitably open?",
            null, null, null,
            new String[]{"40-50% of hands", "15-20% of hands", "25-30% of hands", "Any two cards"},
            0, "The Button can open very wide (40-50%) because you'll always have position postflop."));

        questions.add(new Question("Ranges",
            "What is a 'linear' or 'merged' range?",
            null, null, null,
            new String[]{"A range of your best hands in order of strength", "A range with only premium pairs", "A polarized range with bluffs", "A range with only suited hands"},
            0, "A linear/merged range includes your best hands in order. No gaps - AA, KK, QQ, AKs, etc."));

        questions.add(new Question("Ranges",
            "What is a 'polarized' range?",
            null, null, null,
            new String[]{"Strong value hands + bluffs, no medium hands", "Only pocket pairs", "Linear strongest hands", "Only suited connectors"},
            0, "A polarized range has very strong hands (value) and bluffs, skipping medium-strength hands."));

        questions.add(new Question("Ranges",
            "You hold KJo on the BTN. UTG raises 3x. What should you generally do?",
            "6-max, 100bb effective", null, null,
            new String[]{"Fold - KJo doesn't play well vs UTG range", "Always 3-bet", "Always call", "Raise all-in"},
            0, "KJo is dominated by UTG's range (AK, AQ, KQ). Folding is usually best."));

        questions.add(new Question("Ranges",
            "What does '3-betting light' mean?",
            null, null, null,
            new String[]{"3-betting with hands that aren't premium but have value", "3-betting with AA only", "Making a small 3-bet size", "3-betting from the blinds"},
            0, "3-betting light means 3-betting with hands like A5s, 76s - not premium but with good playability."));

        questions.add(new Question("Ranges",
            "Suited connectors (like 76s) are best played from which position?",
            null, null, null,
            new String[]{"Button or Cutoff", "UTG", "Big Blind", "Small Blind"},
            0, "Suited connectors thrive with position. They're speculative hands that need to see flops cheaply with position."));

        questions.add(new Question("Ranges",
            "Why is AKo called 'Big Slick'?",
            null, null, null,
            new String[]{"It looks great but can slip away if you miss", "It's the biggest non-pair hand", "It always wins", "It's named after a famous player"},
            0, "AKo 'slips' away because despite being strong, it's only ace-high until you connect with the board."));

        // ============================================
        // POT ODDS QUESTIONS
        // ============================================
        questions.add(new Question("Pot Odds",
            "The pot is $100 and villain bets $50. What are your pot odds?",
            null, null, null,
            new String[]{"25% (3:1)", "33% (2:1)", "50% (1:1)", "20% (4:1)"},
            0, "You call $50 to win $150. Pot odds = 50/(100+50+50) = 50/200 = 25%, or 3:1."));

        questions.add(new Question("Pot Odds",
            "You have a flush draw (9 outs) on the flop. What's your approximate equity to hit by the river?",
            null, null, null,
            new String[]{"About 35%", "About 19%", "About 50%", "About 25%"},
            0, "9 outs × 4 (flop to river) = 36%. The precise number is about 35%."));

        questions.add(new Question("Pot Odds",
            "You need 25% equity to call. You have 30% equity. Should you call?",
            null, null, null,
            new String[]{"Yes, you have more equity than needed", "No, it's too risky", "Only if you're in position", "Only with a flush draw"},
            0, "Yes! If your equity (30%) exceeds the required equity (25%), calling is +EV."));

        questions.add(new Question("Pot Odds",
            "What is 'implied odds'?",
            null, null, null,
            new String[]{"Money you expect to win on future streets if you hit", "The odds the pot is giving you right now", "The odds of your opponent folding", "The size of the blinds"},
            0, "Implied odds account for additional money you'll win when you hit your hand."));

        questions.add(new Question("Pot Odds",
            "An open-ended straight draw (8 outs) on the flop has approximately what equity?",
            null, null, null,
            new String[]{"32%", "17%", "45%", "25%"},
            0, "8 outs × 4 = 32% to hit by the river. On a single street it's roughly 17%."));

        questions.add(new Question("Pot Odds",
            "A gutshot straight draw (4 outs) has approximately what equity on the flop?",
            null, null, null,
            new String[]{"17% (to the river)", "32%", "8%", "25%"},
            0, "4 outs × 4 = 16-17% to hit by the river. Single street is about 8-9%."));

        questions.add(new Question("Pot Odds",
            "Pot is $200. Villain bets $200 (pot-sized bet). What equity do you need to call?",
            null, null, null,
            new String[]{"33%", "25%", "50%", "40%"},
            0, "You call $200 to win $600. 200/600 = 33.3% equity needed."));

        questions.add(new Question("Pot Odds",
            "When do implied odds matter MOST?",
            null, null, null,
            new String[]{"When you have a hidden draw (like a set or straight)", "When you have top pair", "When you're bluffing", "When you're all-in"},
            0, "Implied odds matter when your hand is disguised. Sets and straights get paid off more."));

        questions.add(new Question("Pot Odds",
            "Your opponent bets 1/3 pot. What equity do you need to call?",
            null, null, null,
            new String[]{"20%", "25%", "33%", "17%"},
            0, "1/3 pot bet: You call 1 to win 4.33. 1/4.33 ≈ 23% needed. Closest is 20%."));

        questions.add(new Question("Pot Odds",
            "What is a 'combo draw'?",
            null, null, null,
            new String[]{"A flush draw + straight draw combined", "Two pair", "A pair + gutshot", "Any draw with 6+ outs"},
            0, "A combo draw combines flush + straight draws (e.g., 15 outs). These are very powerful."));

        // ============================================
        // HAND RANKING QUESTIONS
        // ============================================
        questions.add(new Question("Hand Rankings",
            "Which hand wins: A♠K♠Q♠J♠T♠ or A♥A♦A♣A♠K♥?",
            null, null, null,
            new String[]{"Royal Flush beats Quad Aces", "Quad Aces beats Royal Flush", "It's a tie", "Depends on the suits"},
            0, "A Royal Flush is the best possible hand and beats everything, including quad aces."));

        questions.add(new Question("Hand Rankings",
            "Which hand is stronger: Full House or Flush?",
            null, null, null,
            new String[]{"Full House beats Flush", "Flush beats Full House", "They're equal", "Depends on the cards"},
            0, "Full House (3 of a kind + pair) beats a Flush. Memorize the hand rankings!"));

        questions.add(new Question("Hand Rankings",
            "You have 7♠7♥. Board is 7♦K♣K♠2♥3♦. What's your hand?",
            null, null, null,
            new String[]{"Full House (7s full of Kings)", "Three of a Kind (Trip 7s)", "Two Pair (7s and Kings)", "Full House (Kings full of 7s)"},
            0, "You have 777KK - Sevens full of Kings. The three of a kind determines the 'full of'."));

        questions.add(new Question("Hand Rankings",
            "What beats a Straight?",
            null, null, null,
            new String[]{"Flush, Full House, Quads, Straight Flush, Royal Flush", "Only Full House and above", "Only Quads and above", "Nothing - Straight is the best"},
            0, "Flush beats Straight. Then Full House, Quads, Straight Flush, Royal Flush."));

        questions.add(new Question("Hand Rankings",
            "Both players have a flush. How do you determine the winner?",
            null, null, null,
            new String[]{"Highest card in the flush wins", "Most suited cards wins", "The suit determines it", "It's always a split pot"},
            0, "The player with the highest card in their flush wins. Ace-high flush beats King-high flush."));

        questions.add(new Question("Hand Rankings",
            "You: A♠A♥. Villain: K♣K♦. Board: 2♣5♠8♦J♥Q♣. Who wins?",
            null, null, null,
            new String[]{"You win with pair of Aces", "Villain wins with pair of Kings", "It's a tie - both have one pair", "Neither - the board plays"},
            0, "Pair of Aces beats Pair of Kings. Simple but fundamental!"));

        questions.add(new Question("Hand Rankings",
            "What's the lowest possible straight?",
            null, null, null,
            new String[]{"A-2-3-4-5 (Wheel)", "2-3-4-5-6", "6-7-8-9-10", "10-J-Q-K-A"},
            0, "The 'Wheel' (A-2-3-4-5) is the lowest straight. The Ace plays as a 1 here."));

        questions.add(new Question("Hand Rankings",
            "Can you use an Ace in a straight as both high AND low?",
            null, null, null,
            new String[]{"No - it's high OR low in a single hand", "Yes - you can wrap around", "Only in Omaha", "Only in tournaments"},
            0, "The Ace can be high (10-J-Q-K-A) or low (A-2-3-4-5) but NOT both. Q-K-A-2-3 is not a straight."));

        questions.add(new Question("Hand Rankings",
            "Two players both have two pair. How is the winner determined?",
            null, null, null,
            new String[]{"Compare highest pair first, then second pair, then kicker", "Add up all card values", "The one with more suited cards", "It's always a split"},
            0, "Compare the highest pair first. If tied, compare second pair. If still tied, compare kicker."));

        questions.add(new Question("Hand Rankings",
            "What is a 'set' vs 'trips'?",
            null, null, null,
            new String[]{"Set = pocket pair + 1 board card, Trips = 1 hole card + 2 board cards", "They're the same thing", "Set is better than trips", "Trips is better than set"},
            0, "Set = you have a pocket pair and one matches on board. Trips = you hold one and two are on board. Sets are more disguised!"));

        // ============================================
        // TERMINOLOGY QUESTIONS
        // ============================================
        questions.add(new Question("Terminology",
            "What does '3-bet' mean?",
            null, null, null,
            new String[]{"A re-raise over an initial raise", "Betting 3x the big blind", "Third bet on the flop", "Betting on 3rd street"},
            0, "Blinds are 'bet 1', open raise is 'bet 2', so a re-raise is 'bet 3' or 3-bet."));

        questions.add(new Question("Terminology",
            "What is a 'continuation bet' (c-bet)?",
            null, null, null,
            new String[]{"A bet by the preflop aggressor on the flop", "Any bet after the flop", "A bet to continue the pot", "A bet on the river"},
            0, "A c-bet is when the preflop raiser bets the flop, 'continuing' their aggression."));

        questions.add(new Question("Terminology",
            "What does it mean to 'float' someone?",
            null, null, null,
            new String[]{"Call with a weak hand planning to bluff later", "Fold to a bet", "Make a small raise", "Check-raise the flop"},
            0, "Floating is calling with a weak hand, planning to take the pot away on a later street."));

        questions.add(new Question("Terminology",
            "What is 'pot control'?",
            null, null, null,
            new String[]{"Keeping the pot small with medium-strength hands", "Always betting big", "Never betting", "Controlling your emotions"},
            0, "Pot control means checking or betting small to keep the pot manageable with marginal hands."));

        questions.add(new Question("Terminology",
            "What is a 'donk bet'?",
            null, null, null,
            new String[]{"Betting into the preflop aggressor out of position", "A stupid bet", "A bet on the river", "Betting with a weak hand"},
            0, "A donk bet is when you lead into the player who raised preflop. It's called 'donk' because it's often incorrect."));

        questions.add(new Question("Terminology",
            "What does 'equity' mean in poker?",
            null, null, null,
            new String[]{"Your share of the pot based on winning probability", "The money you've invested", "Your chip stack", "The rake"},
            0, "Equity is your expected share of the pot. If you have 60% equity in a $100 pot, your equity is $60."));

        questions.add(new Question("Terminology",
            "What is 'GTO' poker?",
            null, null, null,
            new String[]{"Game Theory Optimal - unexploitable strategy", "Going To Olympics", "Get The Odds", "General Table Operations"},
            0, "GTO (Game Theory Optimal) is a mathematically balanced strategy that cannot be exploited."));

        questions.add(new Question("Terminology",
            "What does 'TAG' stand for in player types?",
            null, null, null,
            new String[]{"Tight-Aggressive", "Tight-And-Gambling", "Take-All-Gains", "Total-Aggression-Game"},
            0, "TAG = Tight-Aggressive. They play few hands but bet/raise aggressively with them."));

        questions.add(new Question("Terminology",
            "What is 'range advantage'?",
            null, null, null,
            new String[]{"When the board favors your range more than villain's", "Having more chips", "Being in position", "Having a pocket pair"},
            0, "Range advantage is when the flop/turn/river connects better with your likely holdings than your opponent's."));

        questions.add(new Question("Terminology",
            "What does 'villain' mean in poker strategy discussions?",
            null, null, null,
            new String[]{"Your opponent", "A bad player", "The dealer", "Someone who bluffs a lot"},
            0, "Villain simply means your opponent. Hero = you, Villain = them."));

        // ============================================
        // BET SIZING QUESTIONS
        // ============================================
        questions.add(new Question("Bet Sizing",
            "What is a typical preflop open raise size in 6-max cash?",
            null, null, null,
            new String[]{"2.5-3x the big blind", "5x the big blind", "Min-raise (2x)", "10x the big blind"},
            0, "Standard open is 2.5-3x BB. Smaller online (2.2-2.5x), sometimes bigger live."));

        questions.add(new Question("Bet Sizing",
            "When should you typically use a LARGER c-bet size?",
            null, null, null,
            new String[]{"On dry, unconnected boards where you have range advantage", "On wet, connected boards", "When you're bluffing", "When out of position"},
            0, "Large c-bets work on dry boards (like K-7-2 rainbow) where you can credibly rep strong hands."));

        questions.add(new Question("Bet Sizing",
            "What size 3-bet is typical when in position?",
            null, null, null,
            new String[]{"3x the original raise", "2x the original raise", "5x the original raise", "Just the minimum"},
            0, "In position, 3x the original raise is standard. Out of position, use 3.5-4x."));

        questions.add(new Question("Bet Sizing",
            "What is an 'overbet'?",
            null, null, null,
            new String[]{"A bet larger than the pot", "Any bet over $100", "A bet that's too big", "A river bet"},
            0, "An overbet is any bet larger than the current pot size. Can be used for value or as a bluff."));

        questions.add(new Question("Bet Sizing",
            "Why might you bet SMALL on a wet flop?",
            null, null, null,
            new String[]{"To bet more hands at a cheaper price with range advantage", "Because you're weak", "To induce a raise", "Small bets are always better"},
            0, "Small bets on wet boards let you continue with your whole range efficiently while still gaining value."));

        questions.add(new Question("Bet Sizing",
            "When is min-raising usually correct preflop?",
            null, null, null,
            new String[]{"Rarely - usually when stealing blinds or in late-stage tournaments", "Always", "Never", "Only with premium hands"},
            0, "Min-raises are situational. Common in tournament late stages to steal blinds cheaply."));

        questions.add(new Question("Bet Sizing",
            "What's a 'pot-sized bet'?",
            null, null, null,
            new String[]{"A bet equal to the current pot", "The maximum allowed bet", "A bet of $100", "Any bet over half pot"},
            0, "A pot-sized bet equals the current pot. If pot is $50, a pot bet is $50."));

        questions.add(new Question("Bet Sizing",
            "Why use a small bet (25-33% pot) on the river as a value bet?",
            null, null, null,
            new String[]{"To get called by weaker hands that would fold to bigger bets", "Because you're uncertain", "To save money if you lose", "Small bets are always better"},
            0, "Thin value bets are small so worse hands call. Bigger bets only get called by better hands."));

        // ============================================
        // STACK DEPTH QUESTIONS
        // ============================================
        questions.add(new Question("Stack Depth",
            "What does 'SPR' stand for?",
            null, null, null,
            new String[]{"Stack-to-Pot Ratio", "Standard Poker Rating", "Stack Percentage Remaining", "Suited Pair Ratio"},
            0, "SPR = Stack-to-Pot Ratio. Effective stack ÷ pot size after a betting round."));

        questions.add(new Question("Stack Depth",
            "With a LOW SPR (1-2), what hands increase in value?",
            null, null, null,
            new String[]{"Top pair hands and overpairs", "Drawing hands like flush draws", "Small pocket pairs (set mining)", "Suited connectors"},
            0, "Low SPR means less money behind. Made hands like top pair become commit-worthy."));

        questions.add(new Question("Stack Depth",
            "What does '100bb deep' mean?",
            null, null, null,
            new String[]{"Effective stacks are 100 big blinds", "100 bets in the pot", "You have $100", "100 hands played"},
            0, "100bb deep means effective stacks are 100 times the big blind. Standard cash game depth."));

        questions.add(new Question("Stack Depth",
            "With a HIGH SPR (10+), which hands play best?",
            null, null, null,
            new String[]{"Drawing hands and small pairs (implied odds hands)", "Top pair", "Any ace", "Only premium pairs"},
            0, "High SPR means lots of chips behind. Speculative hands have great implied odds."));

        questions.add(new Question("Stack Depth",
            "What is 'effective stack'?",
            null, null, null,
            new String[]{"The smaller stack between you and your opponent", "Your total chip stack", "The average stack at the table", "The big blind amount"},
            0, "Effective stack is the smaller of the two stacks in a pot. You can only win/lose up to the effective stack."));

        questions.add(new Question("Stack Depth",
            "You have 20bb. What type of hands should you prioritize?",
            null, null, null,
            new String[]{"Broadway hands and pairs - ready to get all-in preflop", "Suited connectors", "Small suited aces", "Only AA and KK"},
            0, "With 20bb, you're in push/fold territory. High cards and pairs are best for all-in situations."));

        questions.add(new Question("Stack Depth",
            "At what stack depth do suited connectors lose most value?",
            null, null, null,
            new String[]{"Very short stacks (under 25bb)", "100bb", "200bb+", "They're always valuable"},
            0, "Suited connectors need implied odds. Short stacks can't realize these, making them weak."));

        questions.add(new Question("Stack Depth",
            "What is 'commitment threshold'?",
            null, null, null,
            new String[]{"The SPR at which you should stack off with a specific hand", "A tournament rule", "Your maximum bet size", "When you must ante"},
            0, "Commitment threshold is the SPR where a hand is strong enough to get all the chips in."));

        questions.add(new Question("Stack Depth",
            "In a tournament with 10bb, what is the correct strategy?",
            null, null, null,
            new String[]{"Push or fold - no calling or raising small", "Play normal poker", "Only play premium hands", "Fold everything and wait"},
            0, "At 10bb, you should either shove all-in or fold. There's no room for postflop play."));

        questions.add(new Question("Stack Depth",
            "Why do small pocket pairs decrease in value as you get shorter-stacked?",
            null, null, null,
            new String[]{"Less implied odds when you hit a set", "They always stay the same value", "You can't fold them", "The blinds eat you up"},
            0, "Small pairs need to hit sets (12% of the time). Without implied odds to win big pots, they're weaker."));

        // ============================================
        // ADVANCED / COMPLEX QUESTIONS
        // ============================================

        questions.add(new Question("Ranges",
            "You open BTN with A5s, BB 3-bets. What makes A5s a better 4-bet bluff than A9o?",
            "100bb deep, BTN vs BB", null, null,
            new String[]{"A5s blocks AA/AK, has nut flush potential, and doesn't dominate hands you want to fold", "A9o is too strong to fold", "Suited cards always beat offsuit", "A5s has more showdown value"},
            0, "A5s blocks premium hands (AA, AK), has backdoor nut flush equity, and doesn't block hands like AT/AJ that might fold to a 4-bet."));

        questions.add(new Question("Ranges",
            "What is 'range morphology' and why does it matter?",
            null, null, null,
            new String[]{"How your range interacts with the board texture - determines your betting strategy", "The shape of your cards", "How many combos you have", "Your position at the table"},
            0, "Range morphology describes how well your range connects with the board. If you have more nutted hands, you can bet bigger and more often."));

        questions.add(new Question("Pot Odds",
            "You have a flush draw + gutshot (12 outs). Villain bets pot on the flop. What's your play?",
            "No implied odds consideration", null, null,
            new String[]{"Call - you have ~45% equity and only need 33%", "Fold - draws are always losing", "Raise all-in always", "Check-raise small"},
            0, "12 outs × 4 = 48% equity. Pot bet requires 33%. You have way more than enough equity to call."));

        questions.add(new Question("Pot Odds",
            "Villain overbets 2x pot on the river. What equity do you need to call?",
            null, null, null,
            new String[]{"40%", "50%", "33%", "25%"},
            0, "You call 2 to win 5 (pot + bet + your call). 2/5 = 40% equity needed."));

        questions.add(new Question("Terminology",
            "What is 'leveling' or 'FPS' (Fancy Play Syndrome)?",
            null, null, null,
            new String[]{"Overthinking and making plays that are too advanced for the situation", "Playing too straightforward", "Always value betting", "Bluffing too rarely"},
            0, "Leveling/FPS is when you outthink yourself. Against weak players, simple straightforward play is usually best."));

        questions.add(new Question("Terminology",
            "What does 'capped range' mean?",
            null, null, null,
            new String[]{"A range that can't contain the strongest possible hands", "A range with only pairs", "A balanced range", "A range with no bluffs"},
            0, "A capped range lacks the nuts. For example, if you just call preflop and flop comes AAK, you're capped - you'd have raised with AA/AK."));

        questions.add(new Question("Terminology",
            "What is 'runout' in poker?",
            null, null, null,
            new String[]{"The community cards that come after the flop (turn and river)", "When you run out of chips", "A drawing dead situation", "The final hand ranking"},
            0, "Runout refers to the turn and river cards. 'Good runout' means cards that help your hand or bluffing range."));

        questions.add(new Question("Bet Sizing",
            "On a K♠7♠2♦ flop, why might the preflop raiser use a SMALL c-bet (25-33%)?",
            "BTN opens, BB calls", null, null,
            new String[]{"BTN has range advantage and can bet entire range for a small size", "BTN is weak", "Small bets are always better", "To induce bluffs"},
            0, "On dry boards where the raiser has range advantage, small bets work well - you can bet your whole range profitably."));

        questions.add(new Question("Bet Sizing",
            "On a 8♠7♠6♥ flop, why might the preflop raiser CHECK instead of c-bet?",
            "CO opens, BB calls", null, null,
            new String[]{"This wet board hits BB's calling range hard - BB has range advantage here", "Always c-bet", "Checking shows weakness", "You should min-bet instead"},
            0, "Wet, connected boards favor the caller's range (suited connectors, pairs). The raiser should check more often on boards that don't favor their range."));

        questions.add(new Question("Position",
            "In a 3-bet pot, you're IP with AA on Q♠J♠T♠ flop. Villain checks. Best play?",
            "100bb effective, you 3-bet preflop", null, null,
            new String[]{"Check back for pot control - this board hits villain's calling range", "Bet huge to protect", "Shove all-in immediately", "Bet 25% pot"},
            0, "QJT is a terrible board for AA. Villain has all the straights, two pairs, sets. Checking back for pot control is often best."));

        questions.add(new Question("Ranges",
            "What is a 'merged' 3-bet strategy vs a 'polarized' 3-bet strategy?",
            null, null, null,
            new String[]{"Merged: strong hands in order. Polarized: value hands + bluffs, skip medium hands", "They're the same thing", "Merged is for tournaments only", "Polarized means only bluffing"},
            0, "Merged 3-betting uses your best hands (linear). Polarized uses nutted hands + bluffs, folding medium hands like KQo."));

        questions.add(new Question("Hand Rankings",
            "Board: A♠K♠Q♠J♠2♥. You hold T♠9♠. Villain holds A♥A♦. Who wins?",
            null, null, null,
            new String[]{"You win - Royal Flush beats set of Aces", "Villain wins with three Aces", "Split pot - both have Ace high flush", "Villain wins - trips beat flush"},
            0, "You have a Royal Flush (A♠K♠Q♠J♠T♠). This beats everything, including villain's set of Aces."));

        questions.add(new Question("Pot Odds",
            "What are 'reverse implied odds' and when do they matter?",
            null, null, null,
            new String[]{"Money you lose when you make your hand but villain has better - matters with dominated draws", "Same as implied odds", "Odds of villain folding", "The pot odds in reverse"},
            0, "Reverse implied odds = money lost when you hit but are beaten. Non-nut flush draws have bad RIO because better flushes exist."));

        questions.add(new Question("Terminology",
            "What does 'nit' mean as a player type?",
            null, null, null,
            new String[]{"A player who only plays premium hands and folds too much", "An aggressive player", "A fish who calls too much", "A professional player"},
            0, "A nit is an extremely tight player who only enters pots with premium hands. Exploit them by stealing their blinds."));

        questions.add(new Question("Terminology",
            "What is 'protection betting'?",
            null, null, null,
            new String[]{"Betting to deny equity to hands that could improve against you", "Betting to protect your stack", "Betting to protect your image", "A small defensive bet"},
            0, "Protection betting is betting a made hand to make draws pay or fold, preventing them from realizing their equity for free."));

        questions.add(new Question("Stack Depth",
            "You have top pair, SPR is 1.5. What does this mean for your strategy?",
            "Pot is $100, you have $150 behind", null, null,
            new String[]{"With low SPR, top pair is often strong enough to stack off", "You should fold top pair here", "SPR doesn't affect top pair", "You should slowplay"},
            0, "Low SPR (under 3) means you're committed with top pair. Plan to get all the money in."));

        questions.add(new Question("Bet Sizing",
            "When should you use an overbet (more than pot) on the river?",
            null, null, null,
            new String[]{"With a polarized range - nutted hands for value or as a bluff", "When you're unsure of your hand", "Always with top pair", "Never - it's too risky"},
            0, "Overbets work with polarized ranges. You either have the nuts and want max value, or you're bluffing."));

        questions.add(new Question("Position",
            "You're in the BB. UTG raises, MP calls, CO calls, BTN calls, SB folds. What adjustments should you make?",
            "5-way pot developing", null, null,
            new String[]{"Tighten up dramatically - your hand needs to be strong to play multiway", "Call with any two cards", "3-bet bluff more often", "Play the same as heads-up"},
            0, "In multiway pots, speculative hands go down in value while made hands go up. You need stronger holdings."));

        questions.add(new Question("Ranges",
            "Why is KQo a difficult hand to play from early position?",
            null, null, null,
            new String[]{"It's dominated by AK/AQ and doesn't flop well multiway - often better to fold", "It's the best hand in poker", "You should always 3-bet it", "It only plays well in position"},
            0, "KQo is easily dominated by 3-bets (AA, KK, QQ, AK, AQ). It plays poorly multiway and is often dominated when you hit."));

        questions.add(new Question("Pot Odds",
            "You have K♠Q♠ on J♠T♠4♥. How many outs do you have?",
            null, null, null,
            new String[]{"15 outs - 9 flush + 6 straight (minus 2 overlap)", "9 outs", "8 outs", "21 outs"},
            0, "9 flush outs + 6 straight outs (any A or 9, minus A♠ and 9♠ already counted) = 15 outs. This is a monster draw!"));

        questions.add(new Question("Terminology",
            "What is 'card removal' or 'blockers' in poker?",
            null, null, null,
            new String[]{"Cards you hold affect what hands your opponent can have", "Removing cards from the deck", "A cheating technique", "The dead cards in the muck"},
            0, "If you hold A♠, villain can't have AA or A♠K♠. Blockers affect hand reading and bluff selection."));

        questions.add(new Question("Bet Sizing",
            "What's the purpose of a 'blocking bet' or 'blocker bet'?",
            null, null, null,
            new String[]{"A small bet OOP to set your own price and prevent a larger bet from villain", "A bet that blocks straights", "A bet with blocking cards", "The biggest possible bet"},
            0, "A blocking bet is a small bet out of position, often with medium-strength hands, to prevent villain from making a larger bet you'd have to fold to."));

        questions.add(new Question("Stack Depth",
            "At 200bb deep, which hands increase in value compared to 100bb?",
            null, null, null,
            new String[]{"Suited connectors, small pairs - they have more implied odds for big pots", "Only AA and KK", "No hands change value", "All hands decrease in value"},
            0, "Deep stacks mean more implied odds. Speculative hands like 65s or 22 can win massive pots when they hit."));

        questions.add(new Question("Ranges",
            "What does it mean to 'balance your range'?",
            null, null, null,
            new String[]{"Having both value hands and bluffs in your range to be unexploitable", "Folding 50% of hands", "Only playing pairs", "Betting the same amount always"},
            0, "A balanced range contains value bets and bluffs in proper proportions. Against good players, this makes you harder to exploit."));

        questions.add(new Question("Position",
            "Why is the small blind the worst position at the table?",
            null, null, null,
            new String[]{"You post money, act first postflop, and face a raise with poor odds to defend", "It's actually the best position", "You don't post as much as the BB", "You act last preflop"},
            0, "SB is worst because you post dead money, act first postflop (no information), and face bad odds to complete/defend."));

        questions.add(new Question("Terminology",
            "What does 'run it twice' mean?",
            null, null, null,
            new String[]{"Dealing remaining board cards twice when all-in to reduce variance", "Playing two hands at once", "A tournament rule", "Betting twice in a row"},
            0, "Running it twice means dealing the remaining cards twice, splitting the pot based on both runouts. Reduces variance."));

        // ============================================
        // ADVANCED QUESTIONS - 500 MORE
        // ============================================

        // --- ADVANCED PREFLOP CONCEPTS ---
        questions.add(new Question("Ranges",
            "You're in the CO with A♠J♠. UTG opens, MP calls. What's your best play?",
            "100bb deep, tough regulars", null, null,
            new String[]{"Fold - AJs plays poorly multiway against an EP open", "3-bet for value", "Call and play postflop", "4-bet bluff"},
            0, "AJs is dominated by UTG's range (AK, AQ). Multiway it loses value. Folding is often correct against good players."));

        questions.add(new Question("Ranges",
            "What is a 'squeeze play'?",
            null, null, null,
            new String[]{"3-betting after a raise and one or more callers", "Folding under pressure", "A small 4-bet", "Betting into multiple opponents"},
            0, "A squeeze is 3-betting when there's a raise and caller(s). The caller is 'squeezed' between you and the raiser."));

        questions.add(new Question("Ranges",
            "Why is squeezing effective against a raiser + caller?",
            null, null, null,
            new String[]{"The caller showed weakness by not raising, and raiser must worry about both of you", "It's not effective", "The pot is bigger", "You always have the best hand"},
            0, "The caller's flat indicates a capped, medium-strength range. The raiser can't continue light with someone behind."));

        questions.add(new Question("Ranges",
            "What hands make good squeeze candidates from the blinds?",
            "BTN opens, CO calls, you're in BB", null, null,
            new String[]{"Ax suited, broadway hands, medium pairs - hands with blockers and playability", "Only AA and KK", "Any two cards", "Only suited connectors"},
            0, "Good squeezers block premium hands (Ax), have equity when called (suited), and can flop well."));

        questions.add(new Question("Ranges",
            "What is 'cold calling'?",
            null, null, null,
            new String[]{"Calling a raise when you haven't put money in the pot yet", "Calling with a weak hand", "Calling on the river", "Calling an all-in"},
            0, "Cold calling means calling a raise without having invested. Example: UTG raises, you call from CO (no blind posted)."));

        questions.add(new Question("Ranges",
            "Why is cold calling generally discouraged in many spots?",
            null, null, null,
            new String[]{"It caps your range, gives blinds great odds, and you're often out of position", "It's always fine", "It saves money", "It's only bad in tournaments"},
            0, "Cold callers have capped ranges (would have 3-bet premiums), invite squeezes, and often play OOP."));

        questions.add(new Question("Ranges",
            "When IS cold calling acceptable?",
            "MP opens, you're on BTN with 7♠7♥", null, null,
            new String[]{"With position, speculative hands, and passive blinds behind", "Never", "Always with pairs", "Only in tournaments"},
            0, "Cold calling works with position, implied odds hands (small pairs, suited connectors), when squeezes are unlikely."));

        questions.add(new Question("Ranges",
            "What is a '4-bet bluff' and what hands work best?",
            null, null, null,
            new String[]{"4-betting with non-premium hands; A5s/A4s are ideal (block AA/AK, have equity)", "4-betting AA", "Any 4-bet", "4-betting with 72o"},
            0, "4-bet bluffs use blockers (Ax blocks AA/AK) and have equity if called. A5s is perfect - blocks, suited, can make nuts."));

        questions.add(new Question("Ranges",
            "What does it mean when someone's 3-bet range is 'linear' vs 'polarized'?",
            null, null, null,
            new String[]{"Linear = value hands in order (QQ+, AK, AQ). Polarized = premiums + bluffs, no medium hands", "They're identical", "Linear means straight draws", "Polarized means only pairs"},
            0, "Linear 3-bets for value with all strong hands. Polarized 3-bets nutted hands + selected bluffs, flatting medium hands."));

        questions.add(new Question("Ranges",
            "Against which player type should you use a linear 3-bet strategy?",
            null, null, null,
            new String[]{"Against players who call 3-bets too often", "Against tight nits", "Against aggressive 4-bettors", "Against anyone"},
            0, "Linear 3-betting punishes call-happy players. They call with dominated hands, giving you value."));

        questions.add(new Question("Ranges",
            "Against which player type should you use a polarized 3-bet strategy?",
            null, null, null,
            new String[]{"Against players who fold too much or 4-bet aggressively", "Against calling stations", "Against passive fish", "Against everyone"},
            0, "Polarized works against folders (bluffs profit) and 4-bettors (premiums can 5-bet, bluffs fold cheaply)."));

        questions.add(new Question("Ranges",
            "You open BTN, BB 3-bets to 10bb. Your stack is 100bb. What's the minimum 4-bet size?",
            null, null, null,
            new String[]{"Around 22-25bb (2.2-2.5x the 3-bet)", "15bb", "All-in", "12bb"},
            0, "4-bets are typically 2.2-2.5x the 3-bet when in position. This allows fold equity while risking less."));

        questions.add(new Question("Ranges",
            "What is 'ICM pressure' in tournaments?",
            null, null, null,
            new String[]{"The pressure of pay jumps affecting optimal strategy - chips are worth more than their face value", "Playing for first only", "Ignoring stack sizes", "Always calling all-ins"},
            0, "ICM (Independent Chip Model) values tournament chips based on payout structure. Near bubbles/pay jumps, survival matters more."));

        questions.add(new Question("Ranges",
            "On the bubble of a tournament, a short stack shoves. You have a medium stack with AQo. What adjustment should you make?",
            "Bubble situation, chip leader at table", null, null,
            new String[]{"Fold more often - busting hurts more than doubling helps due to ICM", "Call wider", "Always call with AQ", "Reshove"},
            0, "ICM pressure means medium stacks should avoid confrontations. Busting costs more EV than winning gains."));

        questions.add(new Question("Ranges",
            "What is a 'limp-raise' and when might it be used?",
            null, null, null,
            new String[]{"Limping then raising when someone isolates - a trap play with strong hands", "Limping then folding", "Min-raising", "Limp-calling"},
            0, "Limp-raising is a trapping line. Limp with AA, hope someone raises, then re-raise. Works against aggressive players."));

        questions.add(new Question("Ranges",
            "Why is open-limping generally considered a weak play?",
            null, null, null,
            new String[]{"It doesn't build the pot with strong hands and invites multiway pots with speculative hands", "It's actually the best play", "It saves money", "Pros do it all the time"},
            0, "Limping lets others see cheap flops, doesn't define ranges, and loses value with premium hands."));

        questions.add(new Question("Ranges",
            "What is your 'continuing range' in a 3-bet pot?",
            null, null, null,
            new String[]{"The hands you proceed with after facing a 3-bet - either by calling or 4-betting", "Only AA and KK", "All hands you opened", "Hands you fold"},
            0, "Your continuing range includes hands you call the 3-bet with and hands you 4-bet. The rest you fold."));

        questions.add(new Question("Bet Sizing",
            "Why might you use a smaller 3-bet size when in position?",
            "You're on BTN, CO opened", null, null,
            new String[]{"You have position postflop so need less fold equity and can play more pots", "To look weak", "Smaller is always better", "To confuse opponents"},
            0, "In position, you can outplay opponents postflop. Smaller 3-bets let you play more hands profitably."));

        questions.add(new Question("Bet Sizing",
            "Why use a larger 3-bet size when out of position?",
            "You're in BB, BTN opened", null, null,
            new String[]{"You need more fold equity since you'll be OOP postflop", "To look strong", "Bigger is always better", "To build the pot faster"},
            0, "OOP is tough to play. Larger 3-bets generate more folds, and when called, there's more dead money in the pot."));

        // --- ADVANCED POSTFLOP CONCEPTS ---
        questions.add(new Question("Bet Sizing",
            "What is 'geometric bet sizing' and when is it used?",
            null, null, null,
            new String[]{"Sizing bets so you can get all-in by the river in equal increments", "Random bet sizes", "Always betting pot", "Small bets only"},
            0, "Geometric sizing plans your stack-off. With 100bb, betting 33% pot each street gets you all-in by river."));

        questions.add(new Question("Bet Sizing",
            "You have the nuts on the river. Pot is $100, villain has $150 behind. What sizing maximizes value?",
            null, null, null,
            new String[]{"It depends on villain's calling range - often all-in to maximize against inelastic calls", "$50 to get called more", "$20 for thin value", "Check to induce"},
            0, "With the nuts, consider villain's calling range. Against calling stations, overbet/shove. Against nits, smaller might get calls."));

        questions.add(new Question("Bet Sizing",
            "What is a 'probe bet'?",
            null, null, null,
            new String[]{"Betting into the preflop aggressor when they checked the previous street", "A small flop bet", "A river value bet", "Betting out of turn"},
            0, "A probe bet is leading into someone who checked back. It exploits their capped range (they'd have bet with strong hands)."));

        questions.add(new Question("Bet Sizing",
            "When is a probe bet on the turn most effective?",
            "You're OOP, villain c-bet flop, you called, villain checked turn", null, null,
            new String[]{"When the turn card is bad for villain's range and they showed weakness", "Always", "Never probe", "Only with the nuts"},
            0, "Probe when the turn hurts villain's range (e.g., completing draws) and their check suggests weakness."));

        questions.add(new Question("Bet Sizing",
            "What is a 'delayed c-bet'?",
            null, null, null,
            new String[]{"Checking flop as preflop aggressor, then betting the turn", "Slow c-betting", "A small c-bet", "Betting after someone else bets"},
            0, "Delayed c-betting means checking back flop and betting turn. Works on wet flops that miss your range then brick turns."));

        questions.add(new Question("Ranges",
            "What is 'nut advantage' vs 'range advantage'?",
            null, null, null,
            new String[]{"Nut advantage = more nutted combos. Range advantage = more overall equity", "They're the same", "Nut advantage means holding an Ace", "Range advantage means more hands"},
            0, "Range advantage is having more equity overall. Nut advantage is having more of the very best hands. Both affect strategy."));

        questions.add(new Question("Ranges",
            "On A♠K♥7♦ flop, who typically has nut advantage: preflop raiser or caller?",
            "BTN raised, BB called", null, null,
            new String[]{"The raiser has nut advantage with more AA, KK, AK combos", "The caller", "Neither", "It's equal"},
            0, "The raiser's range has AA, KK, AK much more often than the caller's. This is strong nut advantage."));

        questions.add(new Question("Ranges",
            "On 8♠7♦6♣ flop, who typically has nut advantage?",
            "BTN raised, BB called", null, null,
            new String[]{"The caller often has more straights, sets, and two pairs", "The raiser always", "Neither", "Whoever has the button"},
            0, "Wet, connected boards favor the caller who has more suited connectors, small pairs for sets."));

        questions.add(new Question("Ranges",
            "What does it mean when a range is 'uncapped'?",
            null, null, null,
            new String[]{"The range can contain the strongest possible hands", "The range has no pairs", "The range is unlimited", "The range is weak"},
            0, "Uncapped means you can have the nuts. If you 3-bet pre, your range on AA-high boards is uncapped."));

        questions.add(new Question("Ranges",
            "How does playing against a capped range affect your strategy?",
            null, null, null,
            new String[]{"You can bluff more and value bet thinner since they can't have the nuts", "Play more passively", "Never bluff", "Always check"},
            0, "Against capped ranges, apply pressure. They can't have the nuts so they'll fold or call with medium hands."));

        questions.add(new Question("Pot Odds",
            "What is 'minimum defense frequency' (MDF)?",
            null, null, null,
            new String[]{"The minimum % of your range you must continue with to not be exploited by bluffs", "How often you should fold", "The minimum bet size", "How often to 3-bet"},
            0, "MDF = 1 - (bet size / (pot + bet)). Against a pot-sized bet, MDF is 50% - you must continue with half your range."));

        questions.add(new Question("Pot Odds",
            "Villain bets 75% pot on the river. What's the MDF?",
            null, null, null,
            new String[]{"About 57% (you should defend 57% of your range)", "25%", "75%", "100%"},
            0, "MDF = 1 - (0.75/1.75) = 57%. If you fold more than 43%, villain profits by bluffing any two cards."));

        questions.add(new Question("Pot Odds",
            "Villain overbets 150% pot. What's the MDF?",
            null, null, null,
            new String[]{"40% - you can fold more against overbets", "60%", "75%", "50%"},
            0, "MDF = 1 - (1.5/2.5) = 40%. Overbets allow you to fold more of your range without being exploited."));

        questions.add(new Question("Pot Odds",
            "What is the 'bluff-to-value ratio' for a pot-sized bet?",
            null, null, null,
            new String[]{"1:1 - one bluff for every value bet", "2:1", "1:2", "3:1"},
            0, "A pot bet risks 1 pot to win 1 pot. Optimal bluffing ratio is 1 bluff per 1 value bet (50% bluffs)."));

        questions.add(new Question("Pot Odds",
            "What bluff-to-value ratio should you use for a 33% pot bet?",
            null, null, null,
            new String[]{"1:2 - one bluff for every two value bets", "1:1", "2:1", "1:3"},
            0, "Smaller bets need fewer bluffs. 33% pot bet: risk 1 to win 3, so 1 bluff for every 2 value bets."));

        questions.add(new Question("Terminology",
            "What is 'ranging' your opponent?",
            null, null, null,
            new String[]{"Putting them on a range of hands based on their actions", "Checking their chip stack", "Knowing their exact hand", "Counting cards"},
            0, "Ranging is deducing what hands opponent could have based on position, action, sizing, and tendencies."));

        questions.add(new Question("Terminology",
            "What does 'Villain's range is face up' mean?",
            null, null, null,
            new String[]{"Their actions make their hand range extremely obvious/narrow", "They showed their cards", "They're bluffing", "They have aces"},
            0, "A 'face up' range is transparent. Example: cold call pre, check-call flop and turn, then lead river = obvious value."));

        questions.add(new Question("Terminology",
            "What is 'hand reading'?",
            null, null, null,
            new String[]{"Using all available information to narrow down opponent's holdings", "Looking at their cards", "Reading physical tells", "Guessing randomly"},
            0, "Hand reading combines position, action, sizing, board texture, and player tendencies to deduce their range."));

        questions.add(new Question("Terminology",
            "What is a 'node' in poker decision trees?",
            null, null, null,
            new String[]{"A specific decision point in a hand where you have multiple options", "A type of bet", "A card rank", "A tournament structure"},
            0, "A node is any point where you make a decision: bet/check, call/fold/raise. Solvers analyze optimal play at each node."));

        questions.add(new Question("Terminology",
            "What does 'Villain is weighted towards' mean?",
            null, null, null,
            new String[]{"Their range contains more of certain hand types than others", "They're heavy", "They bet big", "They fold a lot"},
            0, "Weighted means skewed. 'Weighted towards value' means their range has more strong hands than bluffs."));

        questions.add(new Question("Position",
            "In a 3-bet pot IP, the flop checks through. What does this tell you?",
            "You 3-bet BTN, BB called, flop checked through", null, null,
            new String[]{"Both ranges are likely capped - nutted hands would bet/raise", "Nothing", "Villain is strong", "You should fold turn"},
            0, "When aggression disappears, both players likely have medium-strength hands. The pot is ripe for turn aggression."));

        questions.add(new Question("Position",
            "What is the value of 'relative position'?",
            null, null, null,
            new String[]{"Acting after the aggressor lets you see their action before deciding", "Being on the button", "Having more chips", "Being first to act"},
            0, "Relative position means where you are relative to the betting lead. Acting after the aggressor gives information."));

        questions.add(new Question("Position",
            "In a 3-way pot, why is the 'sandwich' position bad?",
            "BTN opens, you're in CO, BB also called", null, null,
            new String[]{"You act between two opponents and can be squeezed from both sides", "It's actually good", "You have more outs", "You can bluff more"},
            0, "Sandwich position means players on both sides. You can't know if BTN will raise after your action."));

        questions.add(new Question("Position",
            "Why should you c-bet less in multiway pots?",
            null, null, null,
            new String[]{"More opponents means higher chance someone has a strong hand", "You should c-bet more", "Pot odds are better", "Your hand is stronger"},
            0, "In multiway pots, someone usually connects. C-bet only with strong hands or on boards that miss everyone."));

        questions.add(new Question("Position",
            "What adjustment should you make when the pot goes multiway?",
            null, null, null,
            new String[]{"Play more straightforward - bluff less, value bet more", "Bluff more often", "Always check", "Bet bigger"},
            0, "Multiway = more likely someone has a hand. Reduce bluffs, increase value betting frequency."));

        questions.add(new Question("Bet Sizing",
            "What is 'polarizing the river'?",
            null, null, null,
            new String[]{"Having a range of only very strong hands or bluffs when betting river", "Betting small", "Checking back", "Folding the river"},
            0, "River betting ranges are naturally polarized. You either have it (value) or you don't (bluff). Medium hands check."));

        questions.add(new Question("Bet Sizing",
            "When should you 'merge' your river betting range?",
            null, null, null,
            new String[]{"When villain will call with many worse hands - bet thin for value", "Never", "Always on the river", "When you're bluffing"},
            0, "Merge when villain is a calling station. Bet medium-strength hands for thin value since they'll call with worse."));

        questions.add(new Question("Bet Sizing",
            "What does 'thin value' mean?",
            null, null, null,
            new String[]{"Value betting a hand that's only slightly ahead of opponent's calling range", "A small bet", "Bluffing", "Betting with nothing"},
            0, "Thin value is betting a marginal hand expecting to be called by slightly worse. Risky but profitable against calling stations."));

        questions.add(new Question("Bet Sizing",
            "You have second pair on the river. When is thin value betting correct?",
            null, null, null,
            new String[]{"When villain will call with third pair, ace-high, or missed draws", "Never bet second pair", "Always bet", "Only heads up"},
            0, "If villain calls with worse often enough to offset times you're beat, thin value betting is +EV."));

        questions.add(new Question("Terminology",
            "What is 'showdown value'?",
            null, null, null,
            new String[]{"A hand strong enough to win at showdown without betting", "A hand that always wins", "Betting for value", "The final hand ranking"},
            0, "Showdown value means your hand might be best if checked to showdown. No need to bluff, but might check for pot control."));

        questions.add(new Question("Terminology",
            "What hands typically have 'showdown value' on the river?",
            null, null, null,
            new String[]{"Medium pairs, weak top pairs, ace-high", "Only the nuts", "Any made hand", "Bluffs only"},
            0, "Medium-strength hands have showdown value. They beat busted draws but lose to value bets."));

        questions.add(new Question("Terminology",
            "When should you NOT value bet with showdown value?",
            null, null, null,
            new String[]{"When you'll only get called by better hands", "Never", "Always bet for value", "When you have position"},
            0, "If your value bet only gets called by better and folds out worse, you're 'turning your hand into a bluff' - bad."));

        questions.add(new Question("Terminology",
            "What is 'way ahead/way behind' (WA/WB)?",
            null, null, null,
            new String[]{"Spots where you're either crushing or crushed, with little in between", "A betting pattern", "Position concept", "Chip stack situation"},
            0, "WA/WB means you either have the best hand by a lot or are drawing nearly dead. Pot control is key."));

        questions.add(new Question("Terminology",
            "Give an example of a WA/WB situation",
            null, null, null,
            new String[]{"You have AA on K72 rainbow vs a tight player's 3-bet calling range", "Any river situation", "When you flop a set", "Preflop all-in"},
            0, "Your AA crushes their pairs/AK but is way behind if they have KK. Middle ground barely exists."));

        questions.add(new Question("Ranges",
            "What is 'auto-profit' in bluffing?",
            null, null, null,
            new String[]{"When villain folds often enough that any bluff is profitable regardless of your cards", "Always winning", "Guaranteed profit", "A type of bet"},
            0, "If villain folds >50% to a pot-sized bet, bluffing any two cards profits. The specific holding doesn't matter."));

        questions.add(new Question("Ranges",
            "You bet pot on river and need 50% folds to break even. Villain folds 60%. What's true?",
            null, null, null,
            new String[]{"Bluffing any hand is +EV - villain over-folds", "You should never bluff", "Bet smaller", "Only bluff with blockers"},
            0, "At 60% fold rate vs 50% required, any bluff profits. You're printing money - bluff your entire range."));

        questions.add(new Question("Ranges",
            "What are 'removal effects' (blockers) and why do they matter for bluffing?",
            null, null, null,
            new String[]{"Holding cards that reduce combinations of hands villain can have", "Removing cards from deck", "Bluff blocking", "Physical tells"},
            0, "If you hold A♠, villain can't have AA or A♠x. Bluffing with the A♠ is better - you block their value hands."));

        questions.add(new Question("Ranges",
            "You're bluffing the river. Which hand is better to bluff with: A♠5♠ or 7♠6♠?",
            "Board: K♠Q♠8♦4♣2♥, missed flush draw", null, null,
            new String[]{"A♠5♠ - you block the nut flush, making villain's value range weaker", "7♠6♠ always", "They're equal", "Neither should bluff"},
            0, "A♠5♠ blocks A♠x flushes and AA. This makes villain less likely to have the nuts, increasing fold frequency."));

        questions.add(new Question("Ranges",
            "What is 'unblocking' and why is it important?",
            null, null, null,
            new String[]{"NOT holding cards you want villain to have (like busted draws when you value bet)", "Always blocking", "Checking back", "Slow playing"},
            0, "When value betting, unblock villain's calling range. You want them to have busted draws - don't hold those cards."));

        questions.add(new Question("Ranges",
            "For value betting river, would you prefer to hold the A♦ or 7♦?",
            "Board: K♠Q♠8♦4♦2♣, you have top set", null, null,
            new String[]{"7♦ - you don't block villain's busted flush draws that might call", "A♦ for blocker", "Doesn't matter", "Neither"},
            0, "Holding A♦ blocks villain's missed nut flush draws. You want them to have those hands to call. Prefer 7♦."));

        questions.add(new Question("Pot Odds",
            "What is your pot odds equity requirement facing an all-in for twice the pot?",
            "Pot is $100, villain shoves $200", null, null,
            new String[]{"40% equity needed to call", "33%", "50%", "25%"},
            0, "You call $200 to win $500 (pot + villain's bet + your call). 200/500 = 40% equity needed."));

        questions.add(new Question("Pot Odds",
            "You have 40% equity and need 33% to call. Calling is:",
            null, null, null,
            new String[]{"+EV - you have more equity than required", "-EV", "Break even", "Depends on position"},
            0, "Having more equity than required means positive expected value. Always call in this spot."));

        questions.add(new Question("Pot Odds",
            "What is 'fold equity'?",
            null, null, null,
            new String[]{"The value gained when your bet/raise makes opponent fold", "Equity when folding", "Pot odds", "Implied odds"},
            0, "Fold equity is the EV from making opponents fold. Essential for semi-bluffs where you have equity + fold equity."));

        questions.add(new Question("Pot Odds",
            "Why are semi-bluffs more profitable than pure bluffs?",
            null, null, null,
            new String[]{"You can win by villain folding OR by improving to best hand", "They're not", "Pure bluffs are safer", "Semi-bluffs always work"},
            0, "Semi-bluffs have two ways to win: immediate fold equity + equity to improve. Pure bluffs only win if villain folds."));

        questions.add(new Question("Pot Odds",
            "What is 'total equity' in a semi-bluff calculation?",
            null, null, null,
            new String[]{"Fold equity + (1 - fold%) × hand equity", "Just hand equity", "Just fold equity", "Pot odds"},
            0, "Total equity combines both winning methods: sometimes they fold (fold eq), sometimes they call and you can still win (hand eq)."));

        questions.add(new Question("Stack Depth",
            "At what SPR should you commit with an overpair?",
            null, null, null,
            new String[]{"SPR of 4 or less - overpairs are strong enough to stack off", "Any SPR", "Only SPR < 1", "Never commit"},
            0, "Low SPR means shallow stacks relative to pot. At SPR 4 or less, overpairs are commit-worthy."));

        questions.add(new Question("Stack Depth",
            "At what SPR should you commit with top pair top kicker?",
            null, null, null,
            new String[]{"SPR of 3 or less", "SPR of 10", "Any SPR", "Never commit TPTK"},
            0, "TPTK needs lower SPR to commit than overpairs. At SPR 3 or less, you can comfortably get all-in."));

        questions.add(new Question("Stack Depth",
            "At what SPR should you commit with a set?",
            null, null, null,
            new String[]{"Almost any SPR - sets are extremely strong", "Only SPR < 5", "SPR < 1", "Never"},
            0, "Sets are monster hands. You should get stacks in at almost any SPR unless the board is very scary."));

        questions.add(new Question("Stack Depth",
            "How does SPR affect your preflop decisions?",
            null, null, null,
            new String[]{"Lower SPR means speculative hands lose value, made hands gain value", "It doesn't matter", "Higher SPR = tighter", "Always play the same"},
            0, "Short stacks = low SPR. Top pair hands are more valuable. Suited connectors need deep stacks for implied odds."));

        questions.add(new Question("Stack Depth",
            "What is 'effective stack depth' in a 3-way pot?",
            null, null, null,
            new String[]{"The smallest stack of the three players", "Average of all stacks", "Your stack", "The biggest stack"},
            0, "You can only win the smallest stack in full. Effective stack in any pot is the smallest stack involved."));

        questions.add(new Question("Terminology",
            "What is a 'cooler' in poker?",
            null, null, null,
            new String[]{"An unavoidable situation where both players have very strong hands", "A lucky card", "A bad beat", "A bluff gone wrong"},
            0, "Coolers are set-over-set, AA vs KK situations. Neither player made a mistake - just unlucky distribution."));

        questions.add(new Question("Terminology",
            "What is a 'setup hand'?",
            null, null, null,
            new String[]{"A hand where you're destined to lose a lot due to hand vs hand matchup", "A trap", "A bluff", "Opening hand"},
            0, "Setup hands are coolers - you flopped a set, they flopped a bigger set. Big losses are inevitable."));

        questions.add(new Question("Terminology",
            "What does 'realizing equity' mean?",
            null, null, null,
            new String[]{"Actually capturing the equity your hand has by getting to showdown or making villain fold", "Knowing your equity", "Calculating odds", "Having outs"},
            0, "Equity is theoretical; realizing it means you actually get to keep that value. Position helps realize equity."));

        questions.add(new Question("Terminology",
            "Why do suited connectors 'realize equity poorly' out of position?",
            null, null, null,
            new String[]{"They often have to fold to aggression before seeing all cards", "They're weak hands", "They don't have equity", "They always realize equity"},
            0, "OOP faces tough decisions. You might fold a flush draw facing aggression, never seeing the river."));

        questions.add(new Question("Position",
            "Why does having position help you realize equity better?",
            null, null, null,
            new String[]{"You can control pot size, face less aggression, and see more cards cheaply", "It doesn't help", "Cards change", "Luck factor"},
            0, "In position: check behind when weak, bet when strong. You face less pressure and see more showdowns."));

        questions.add(new Question("Ranges",
            "What is 'equity denial'?",
            null, null, null,
            new String[]{"Betting to make opponents fold their equity share", "Denying insurance", "Folding", "Checking back"},
            0, "Equity denial is betting to make draws fold. If they call, you win the pot; if they fold, they don't realize their equity."));

        questions.add(new Question("Ranges",
            "When is equity denial most important?",
            null, null, null,
            new String[]{"When many draws are possible and your hand is vulnerable", "When you have the nuts", "When you're bluffing", "On the river"},
            0, "With a vulnerable hand like top pair on a wet board, bet to deny draws their free equity."));

        questions.add(new Question("Ranges",
            "Should you ever check the nuts on the flop?",
            null, null, null,
            new String[]{"Yes - sometimes to trap or let draws catch up for bigger bets later", "Never check the nuts", "Always check", "Only multiway"},
            0, "Occasionally slowplaying the nuts lets opponents catch up. On very dry boards, checking can induce bluffs."));

        questions.add(new Question("Ranges",
            "When is slowplaying the nuts a mistake?",
            null, null, null,
            new String[]{"On wet boards where draws are present and you risk getting outdrawn", "On dry boards", "Heads up", "In position"},
            0, "On wet boards, slowplaying lets draws see free cards. Bet to deny equity and build the pot."));

        questions.add(new Question("Terminology",
            "What is 'inducing'?",
            null, null, null,
            new String[]{"Making plays to encourage opponent to take specific actions (bluff, call, etc.)", "Bluffing", "Value betting", "Folding"},
            0, "Inducing bluffs = checking to make opponent bluff. Inducing calls = sizing to get looked up."));

        questions.add(new Question("Terminology",
            "When might you 'induce a bluff'?",
            null, null, null,
            new String[]{"When you have a strong hand and villain is aggressive - check to let them bluff", "Never", "With weak hands", "When you want to fold"},
            0, "Check strong hands against aggressive opponents. They'll bet bluffs and worse value, paying you off."));

        questions.add(new Question("Bet Sizing",
            "What is a 'protection bet'?",
            null, null, null,
            new String[]{"Betting a vulnerable made hand to charge draws and deny equity", "A bluff", "A small bet", "Checking"},
            0, "Protection bets are made with hands that can be outdrawn. Force draws to pay or fold."));

        questions.add(new Question("Bet Sizing",
            "When should you NOT protection bet?",
            null, null, null,
            new String[]{"When few draws exist and checking induces bluffs from air", "Always protection bet", "With strong hands", "On wet boards"},
            0, "On dry boards, checking lets worse hands bluff or catch up slightly. Protection betting only gets folds."));

        questions.add(new Question("Terminology",
            "What is 'x/r' or 'check-raise'?",
            null, null, null,
            new String[]{"Checking and then raising when opponent bets", "Checking then raising preflop", "Extra raise", "Raising out of turn"},
            0, "Check-raise: check to opponent, they bet, you raise. Powerful for trapping or bluffing."));

        questions.add(new Question("Terminology",
            "When should you check-raise for value?",
            null, null, null,
            new String[]{"With very strong hands against aggressive opponents who will bet", "With weak hands", "Never", "Only on the river"},
            0, "Check-raising for value traps aggressive bettors. Have a strong hand and expect opponent to bet."));

        questions.add(new Question("Terminology",
            "When should you check-raise as a bluff?",
            null, null, null,
            new String[]{"With draws that have equity if called and can make opponent fold better hands", "With nothing", "Never bluff check-raise", "Always"},
            0, "Check-raise bluffs need fold equity and hand equity. Draws like flush+gutshot are ideal."));

        questions.add(new Question("Terminology",
            "What is a 'donk lead' strategy in modern poker?",
            null, null, null,
            new String[]{"OOP player betting into aggressor - can be strategic on boards favoring their range", "Always wrong", "Only with weak hands", "Only in tournaments"},
            0, "Modern donk leads are strategic on boards favoring the caller (low, connected). It's no longer 'donkey' play."));

        questions.add(new Question("Bet Sizing",
            "When might a donk lead be correct?",
            "BB called BTN open, flop is 6♠5♠4♣", null, null,
            new String[]{"This board favors BB's range (more 78, 65s, sets) - donking can be +EV", "Never donk", "Only with AA", "Only as bluff"},
            0, "BB has more straights, two pairs, and sets on this board. Donk-betting exploits range advantage."));

        questions.add(new Question("Ranges",
            "What is your 'betting range' vs 'checking range'?",
            null, null, null,
            new String[]{"Betting range = hands you bet; checking range = hands you check with", "The same thing", "Only premiums", "Random hands"},
            0, "You split your range into betting (value + bluffs) and checking (showdown value + traps)."));

        questions.add(new Question("Ranges",
            "What hands typically go in your 'checking range'?",
            null, null, null,
            new String[]{"Medium-strength hands with showdown value, traps, and some weak hands", "Only the nuts", "Only bluffs", "All hands"},
            0, "Check hands that want to get to showdown (medium strength) and occasional traps with strong hands."));

        questions.add(new Question("Bet Sizing",
            "What is 'betting frequency'?",
            null, null, null,
            new String[]{"How often you bet in a particular spot", "Bet amount", "Number of bets", "Betting speed"},
            0, "Betting frequency is % of range you bet. Some spots call for high frequency (small bets), others low."));

        questions.add(new Question("Bet Sizing",
            "On dry boards, should betting frequency be high or low?",
            "Example: K♦7♠2♣", null, null,
            new String[]{"High - you can bet your whole range for a small size", "Low", "Medium", "No bets"},
            0, "On dry boards like K72, bet frequently with a small size. Few draws exist to worry about."));

        questions.add(new Question("Bet Sizing",
            "On wet boards, should betting frequency be high or low?",
            "Example: J♠T♠8♦", null, null,
            new String[]{"Lower - only bet strong hands and draws, check medium hands", "Very high", "Bet everything", "Always check"},
            0, "Wet boards have many draws. Be more selective - bet value hands and semi-bluffs, check marginal holdings."));

        questions.add(new Question("Ranges",
            "What is a 'double barrel'?",
            null, null, null,
            new String[]{"Betting the flop and turn as the aggressor", "Two pair", "Shooting term", "Betting twice the pot"},
            0, "Double barrel = c-bet flop and continue betting turn. Shows persistent aggression."));

        questions.add(new Question("Ranges",
            "What factors make a good double barrel candidate?",
            null, null, null,
            new String[]{"Turn card improves your range, creates new draws, or is a scare card for villain", "Any turn card", "Only if you hit", "Never double barrel"},
            0, "Good double barrel turns: complete draws (you rep), scare cards (A, K), or cards that help your range."));

        questions.add(new Question("Ranges",
            "What is a 'triple barrel'?",
            null, null, null,
            new String[]{"Betting flop, turn, and river as the aggressor", "Three of a kind", "Three bets total", "Folding three times"},
            0, "Triple barrel = c-bet all three streets. Usually polarized - you have the nuts or a bluff."));

        questions.add(new Question("Ranges",
            "When should you triple barrel bluff?",
            null, null, null,
            new String[]{"When the runout favors your range and villain has shown weakness", "Always", "Never", "Only with the nuts"},
            0, "Triple barrel bluffs need good stories. The board should support your rep of a strong hand."));

        questions.add(new Question("Terminology",
            "What is 'barreling'?",
            null, null, null,
            new String[]{"Betting multiple streets as the aggressor", "Folding", "Calling", "Check-raising"},
            0, "Barreling = firing bets on consecutive streets. Single barrel, double barrel, triple barrel."));

        questions.add(new Question("Terminology",
            "What is 'firing the third barrel'?",
            null, null, null,
            new String[]{"Betting the river after betting flop and turn", "Making three bets", "Bluffing three times", "A tournament phase"},
            0, "Third barrel = river bet after flop and turn bets. The final shot - usually for value or as a bluff."));

        questions.add(new Question("Ranges",
            "What is a 'give up' or 'give-up frequency'?",
            null, null, null,
            new String[]{"How often you stop betting (check) with your bluffs", "Quitting poker", "Folding always", "Losing money"},
            0, "Give-up frequency = how often you stop bluffing. On later streets, give up weaker bluffs."));

        questions.add(new Question("Ranges",
            "What determines which bluffs to 'give up' on the turn?",
            null, null, null,
            new String[]{"Hands with less equity and no blockers - worst bluff candidates", "Random selection", "All bluffs continue", "Fold all bluffs"},
            0, "Give up lowest equity bluffs with no blockers. Keep betting draws and hands that block villain's continues."));

        questions.add(new Question("Bet Sizing",
            "What is 'range betting'?",
            null, null, null,
            new String[]{"Betting your entire range at one size (usually small) on boards that favor you", "Random betting", "Big bets only", "Checking range"},
            0, "Range betting = betting 100% of hands at a small size. Works on boards with high range advantage."));

        questions.add(new Question("Bet Sizing",
            "When is range betting appropriate?",
            "BTN vs BB on A♠7♦2♣", null, null,
            new String[]{"On boards with huge range advantage - bet small with everything", "On all boards", "Never", "Only with strong hands"},
            0, "Dry boards with range advantage (like A72 for preflop raiser) allow small, frequent bets with entire range."));

        questions.add(new Question("Bet Sizing",
            "What is a 'split betting strategy'?",
            null, null, null,
            new String[]{"Using multiple bet sizes based on hand categories (value, bluffs, marginal)", "Splitting the pot", "Even money bets", "Alternating sizes"},
            0, "Split strategies use big bets with polar range and small bets with merged/marginal range."));

        questions.add(new Question("Terminology",
            "What is 'solver' in poker context?",
            null, null, null,
            new String[]{"Software that calculates game theory optimal strategies", "A player who solves problems", "A math equation", "Chip counting tool"},
            0, "Solvers (PioSolver, GTO+) compute Nash equilibrium strategies for any poker spot."));

        questions.add(new Question("Terminology",
            "What does it mean when a play is 'solver-approved'?",
            null, null, null,
            new String[]{"GTO solvers include this play in optimal strategy at some frequency", "Always correct", "Never correct", "Theoretical only"},
            0, "Solver-approved plays appear in GTO solutions. It doesn't mean always do it, but it's within optimal range."));

        questions.add(new Question("Terminology",
            "What is 'population tendency'?",
            null, null, null,
            new String[]{"How most players play a given spot (often suboptimally)", "Demographics", "Player count", "Location bias"},
            0, "Population tendencies reveal common leaks. Example: population over-folds river - exploit with more bluffs."));

        questions.add(new Question("Terminology",
            "What is an 'exploitative' strategy?",
            null, null, null,
            new String[]{"Deviating from GTO to take advantage of opponent's mistakes", "Cheating", "GTO", "Playing tight"},
            0, "Exploitative play targets specific leaks. If villain folds too much, bluff more. If they call too much, value bet thin."));

        questions.add(new Question("Terminology",
            "When should you use an exploitative strategy vs GTO?",
            null, null, null,
            new String[]{"Against weak players with clear leaks - GTO leaves money on table", "Never exploit", "Against pros only", "Always GTO"},
            0, "Exploit against weak players with clear imbalances. GTO is for tough opponents who can counter-exploit."));

        questions.add(new Question("Terminology",
            "What is 'leveling war'?",
            null, null, null,
            new String[]{"Both players trying to out-think each other to increasingly complex levels", "Fighting", "Chip war", "Position battle"},
            0, "Leveling: 'I think he thinks I have X, so I'll do Y'. Dangerous - often better to play straightforward."));

        questions.add(new Question("Terminology",
            "What's the danger of 'playing too many levels'?",
            null, null, null,
            new String[]{"You outthink yourself, making plays your opponent isn't even considering", "Playing too tight", "Losing chips", "Taking too long"},
            0, "Level 4 thinking vs Level 1 opponent = you're folding JJ to their 'bluff' that's actually QQ."));

        questions.add(new Question("Ranges",
            "What is 'range construction'?",
            null, null, null,
            new String[]{"Building ranges with proper value-to-bluff ratios and hand selection", "Random hands", "Only pairs", "Always strong hands"},
            0, "Range construction = selecting hands for each action that create balanced, unexploitable frequencies."));

        questions.add(new Question("Ranges",
            "Why is it important to have 'some strong hands' in your checking range?",
            null, null, null,
            new String[]{"So opponents can't bet freely when you check - you might check-raise with nuts", "It's not important", "Always bet strong hands", "Trapping is bad"},
            0, "Without strong hands in checking range, villain can attack your checks with impunity. Traps keep them honest."));

        questions.add(new Question("Ranges",
            "What is 'balancing' in poker?",
            null, null, null,
            new String[]{"Playing each spot with a mix of value and bluffs at proper ratios", "Playing every hand", "Never bluffing", "Always bluffing"},
            0, "Balance means your bet/check/raise ranges contain both value and bluffs in unexploitable proportions."));

        questions.add(new Question("Ranges",
            "When is balance less important?",
            null, null, null,
            new String[]{"Against weak players who don't adjust to your strategy", "Always important", "In cash games", "Online only"},
            0, "Balance prevents exploitation. If villain doesn't exploit, imbalanced (exploitative) play is more profitable."));

        questions.add(new Question("Pot Odds",
            "What is 'EV' (expected value)?",
            null, null, null,
            new String[]{"The average outcome of a decision over many iterations", "Exact value", "Chip count", "Pot size"},
            0, "EV is average profit/loss from a play. +EV = profitable long-term. -EV = losing play."));

        questions.add(new Question("Pot Odds",
            "How do you calculate the EV of a call?",
            null, null, null,
            new String[]{"(Pot × Equity) - (Call × (1 - Equity))", "Pot size only", "Call size only", "Card count"},
            0, "EV of call = (What you win × Win%) - (What you lose × Lose%). Positive = good call."));

        questions.add(new Question("Pot Odds",
            "Pot is $100, you call $50 with 40% equity. What's the EV?",
            null, null, null,
            new String[]{"+$10: ($150 × 0.4) - ($50 × 0.6) = $60 - $30 = +$10 (profitable call)", "-$10", "$0", "+$50"},
            0, "Win $150 × 40% = $60 gained. Lose $50 × 60% = $30 lost. Net = +$10 per call."));

        questions.add(new Question("Pot Odds",
            "What does 'zero EV' mean?",
            null, null, null,
            new String[]{"Break-even play - neither winning nor losing money long-term", "No expected value", "Winning nothing", "Folding"},
            0, "Zero EV = break even. You don't win or lose money on average. The 'break even' point for calls."));

        questions.add(new Question("Pot Odds",
            "If a play is -EV, when might you still make it?",
            null, null, null,
            new String[]{"Almost never in cash games; possibly in tournaments for ICM reasons", "Always make it", "Never", "When tilted"},
            0, "In tournaments, chip value isn't linear. Sometimes slightly -EV plays protect your stack or tournament life."));

        questions.add(new Question("Stack Depth",
            "What changes at 40bb effective vs 100bb?",
            null, null, null,
            new String[]{"Less postflop play, more all-in preflop, less speculative hands", "Nothing changes", "More drawing hands", "Bigger bluffs"},
            0, "Shorter stacks mean less room to maneuver. High card hands rise in value; speculative hands fall."));

        questions.add(new Question("Stack Depth",
            "At what stack depth does 'push/fold' begin to dominate?",
            null, null, null,
            new String[]{"Around 10-15 big blinds", "50bb", "100bb", "5bb only"},
            0, "At 10-15bb, shoving or folding is optimal. There's no room for postflop play."));

        questions.add(new Question("Stack Depth",
            "Why do pocket pairs go down in value at very short stacks?",
            null, null, null,
            new String[]{"Not enough implied odds to set-mine - you're often racing or dominated", "They're still great", "No one has them", "Rules change"},
            0, "Small pairs need 15-20x stack to profitably set-mine. At 10bb, 22 is just 50/50 vs overcards."));

        questions.add(new Question("Stack Depth",
            "Which hands increase in value at short stacks?",
            null, null, null,
            new String[]{"High card hands (A9o, KTo) - equity vs calling ranges is higher", "Suited connectors", "Small pairs", "Weak aces"},
            0, "Hands that do well all-in (broadway, aces) increase. No need for implied odds when getting stacks in preflop."));

        questions.add(new Question("Stack Depth",
            "What is 'Nash equilibrium' shove/fold charts?",
            null, null, null,
            new String[]{"Mathematically optimal ranges for shoving and calling at various stack depths", "Random charts", "Fictional strategy", "Live tells"},
            0, "Nash charts give unexploitable shove/fold strategies. At 10bb, knowing these ranges is essential."));

        questions.add(new Question("Position",
            "You're on the BTN with 15bb. UTG folds, MP folds. Should you open-shove or min-raise?",
            "Blinds are good regs", null, null,
            new String[]{"Often shove - min-raise leaves you pot-committed anyway", "Always min-raise", "Always fold", "Limp"},
            0, "At 15bb, min-raise + call 3-bet = committed. Might as well shove and maximize fold equity."));

        questions.add(new Question("Position",
            "At 30bb, what preflop sizing creates commitment if 3-bet?",
            null, null, null,
            new String[]{"2.2-2.5x open - if 3-bet to 8-9bb, you're pot-committed", "Min-raise", "5x open", "Limp"},
            0, "At 30bb, opening 2.5x and facing a 3-bet to 8bb puts significant chips in. Plan for this."));

        questions.add(new Question("Ranges",
            "What is '3-bet jamming' and when is it used?",
            null, null, null,
            new String[]{"3-betting all-in with medium stacks (typically 25-40bb) for max fold equity", "Small 3-bet", "Flat calling", "Folding"},
            0, "3-bet jamming at 25-40bb maximizes fold equity and gets all-in before the flop. Effective with pairs/Ax."));

        questions.add(new Question("Ranges",
            "Should your 3-bet jam range be tighter IP or OOP?",
            null, null, null,
            new String[]{"Tighter OOP - you'll have positional disadvantage if called", "Same range", "Wider OOP", "Only jam IP"},
            0, "OOP jams need stronger hands since you'll be disadvantaged if called. IP can jam slightly wider."));

        questions.add(new Question("Terminology",
            "What is 'chip EV' vs 'dollar EV' in tournaments?",
            null, null, null,
            new String[]{"Chip EV = chips won/lost. Dollar EV = actual prize money affected (ICM)", "Same thing", "Cash game terms", "Only for pros"},
            0, "Chip EV ignores payouts. Dollar EV considers ICM - your chip won could mean less in $ than chip risked."));

        questions.add(new Question("Terminology",
            "What is 'risk premium' in tournament poker?",
            null, null, null,
            new String[]{"Extra equity you need to call due to ICM - busting is worse than doubling is good", "Entry fee", "Insurance cost", "Rebuy price"},
            0, "Risk premium = additional equity required because tournament chips have diminishing value (ICM)."));

        questions.add(new Question("Terminology",
            "Why might you fold AK preflop in a tournament?",
            "Final table bubble, chip leader shoves, you're 2nd in chips", null, null,
            new String[]{"ICM pressure - letting short stacks bust is more valuable than flipping for your stack", "AK is always call", "Never fold AK", "Only if suited"},
            0, "ICM means survival value. Folding and letting others bust can be worth more than chip doubling."));

        questions.add(new Question("Terminology",
            "What is 'future game' consideration?",
            null, null, null,
            new String[]{"How your current actions affect how opponents play against you later", "Next tournament", "Future hands today", "Tomorrow's session"},
            0, "Future game = meta-game. A weird play now might pay off by altering villain's strategy later."));

        questions.add(new Question("Bet Sizing",
            "What is 'blocky' texture?",
            null, null, null,
            new String[]{"A board that blocks many hands - paired boards or very connected", "Brick runout", "Straight board", "Flush board"},
            0, "Blocky textures reduce combos: paired boards block sets/trips, connected boards reduce straight combos."));

        questions.add(new Question("Bet Sizing",
            "On K♠K♦7♣ flop, why might you check more often?",
            "You raised preflop, villain called", null, null,
            new String[]{"Board blocks Kx hands - fewer hands to get value from", "Always bet", "Board is dry", "Villain is weak"},
            0, "Paired boards are blocky. Villain rarely has Kx (you have blockers too). Check more to induce."));

        questions.add(new Question("Ranges",
            "What is a 'blocker bet'?",
            null, null, null,
            new String[]{"A small OOP bet to set the price and prevent larger bets from opponent", "A big bet", "Bluff", "Value bet"},
            0, "Blocker bet = small bet OOP with showdown value. You pay $20 instead of facing a $50 bet."));

        questions.add(new Question("Ranges",
            "When is a blocker bet typically used?",
            null, null, null,
            new String[]{"On the river OOP with medium-strength hands - sets a cheap price to showdown", "Flop always", "With the nuts", "As a bluff"},
            0, "River with showdown value, worried about bigger bet. Bet small to potentially see showdown cheaply."));

        questions.add(new Question("Ranges",
            "What's the problem with blocker betting too often?",
            null, null, null,
            new String[]{"Good opponents will raise you, making it more expensive than checking", "No problem", "You win more", "It's always good"},
            0, "Blocker bets can backfire. Aggressive opponents raise, turning your cheap showdown into expensive fold."));

        questions.add(new Question("Bet Sizing",
            "What is 'value-cutting' yourself?",
            null, null, null,
            new String[]{"Betting a size where you only get called by better hands", "Good value bet", "Thin value", "Bluffing"},
            0, "Value-cutting = betting and only getting called when behind. Better to check for showdown."));

        questions.add(new Question("Bet Sizing",
            "How do you avoid value-cutting yourself?",
            null, null, null,
            new String[]{"Consider what hands call your bet - if only better, check instead", "Always bet", "Bet bigger", "Bluff more"},
            0, "Think about villain's calling range. If they only call with better, you're not value betting - you're paying off."));

        questions.add(new Question("Terminology",
            "What does it mean to 'turn your hand into a bluff'?",
            null, null, null,
            new String[]{"Betting a hand that has showdown value but only gets called by better", "Converting cards", "Bluff-raising", "Folding winners"},
            0, "If you bet second pair and only get called by better, you 'turned it into a bluff' - might as well bluff with air."));

        questions.add(new Question("Pot Odds",
            "What are 'direct odds' vs 'implied odds'?",
            null, null, null,
            new String[]{"Direct = current pot odds. Implied = including future bets when you hit", "Same thing", "Reverse concepts", "Tournament terms"},
            0, "Direct odds = what's in pot now. Implied = what you expect to win on later streets."));

        questions.add(new Question("Pot Odds",
            "When are implied odds minimal?",
            null, null, null,
            new String[]{"When draws are obvious, stacks are shallow, or villain is tight", "Always high", "With position", "Deep stacked"},
            0, "If your draw is obvious (4-flush board), villain won't pay off. Shallow stacks limit future bets too."));

        questions.add(new Question("Pot Odds",
            "What are 'effective odds'?",
            null, null, null,
            new String[]{"Combined direct and implied odds for decision-making", "Exact odds", "Pot odds only", "Stack odds"},
            0, "Effective odds = direct odds improved by implied odds. Your true expectation including future betting."));

        questions.add(new Question("Terminology",
            "What is 'the gap concept'?",
            null, null, null,
            new String[]{"You need a stronger hand to call a raise than to open-raise yourself", "Chip gaps", "Position gaps", "Skill differences"},
            0, "The gap: opening 87s is fine, but calling a raise with 87s needs better conditions. Callers need stronger hands."));

        questions.add(new Question("Terminology",
            "Why does the gap concept exist?",
            null, null, null,
            new String[]{"Opener has fold equity and initiative; caller needs hand strength to compensate", "It doesn't", "Random rule", "Old strategy"},
            0, "When you open, you might win immediately. When calling, you're facing a range and lose that fold equity."));

        questions.add(new Question("Ranges",
            "What is 'overcalling'?",
            null, null, null,
            new String[]{"Calling after someone else has already called a bet", "Calling too much", "Big calls", "Wrong calls"},
            0, "Overcalling = calling behind another caller. Your hand needs to beat multiple ranges now."));

        questions.add(new Question("Ranges",
            "Why should you tighten your range when overcalling?",
            null, null, null,
            new String[]{"You need to beat the bettor AND the first caller - two ranges to overcome", "You don't", "Always wide", "Position matters more"},
            0, "Overcalling means facing two ranges. If either has you beat, you lose. Tighter ranges are necessary."));

        questions.add(new Question("Position",
            "What is 'closing the action'?",
            null, null, null,
            new String[]{"Being last to act preflop (in BB when no raise) or ending a betting round", "Folding", "Going all-in", "Checking back"},
            0, "Closing action = acting last. In BB with limps, you close action preflop. On river IP, your call closes the hand."));

        questions.add(new Question("Position",
            "Why is closing the action valuable?",
            null, null, null,
            new String[]{"You have maximum information before committing chips", "It's not valuable", "You act first", "Bigger pots"},
            0, "Closing action means no one acts after you. All information is available before your decision."));

        questions.add(new Question("Terminology",
            "What is 'texture' in poker?",
            null, null, null,
            new String[]{"The connectedness and suit distribution of board cards", "Card feel", "Chip texture", "Table surface"},
            0, "Board texture = wet (many draws) vs dry (few draws). Affects betting strategy significantly."));

        questions.add(new Question("Terminology",
            "What is a 'wet' board texture?",
            null, null, null,
            new String[]{"Many draws possible: flush draws, straight draws, two pair combos", "A board with water cards", "Low cards", "Paired board"},
            0, "Wet boards like J♠T♠8♦ have many draws. Action is often intense."));

        questions.add(new Question("Terminology",
            "What is a 'dry' board texture?",
            null, null, null,
            new String[]{"Few draws possible: disconnected, rainbow cards", "Desert cards", "High cards", "Suited board"},
            0, "Dry boards like K♦7♠2♣ have minimal draws. Usually checks around or small bets."));

        questions.add(new Question("Terminology",
            "What is a 'dynamic' board?",
            null, null, null,
            new String[]{"A wet board where turn/river cards can dramatically change equity", "Stable board", "Paired board", "Dry board"},
            0, "Dynamic boards have many draws that could complete. Equities shift wildly on later streets."));

        questions.add(new Question("Terminology",
            "What is a 'static' board?",
            null, null, null,
            new String[]{"A dry board where equities remain relatively unchanged through streets", "Moving board", "Wet board", "Connected board"},
            0, "Static boards don't change much. Whoever's ahead on flop likely stays ahead. K♦7♠2♣ is static."));

        questions.add(new Question("Bet Sizing",
            "How does board texture affect bet sizing?",
            null, null, null,
            new String[]{"Wet boards = polarized/larger bets. Dry boards = smaller/more frequent bets", "No effect", "Always same size", "Bigger on dry"},
            0, "Wet boards need bigger bets to protect/get value. Dry boards allow small bets with entire range."));

        questions.add(new Question("Ranges",
            "What hands 'smash' a flop like A♠K♥K♣?",
            null, null, null,
            new String[]{"AA, KK, AK - sets, full houses, and top two pair", "Any pair", "Flush draws", "Low cards"},
            0, "Big hands crush paired broadway boards. AA, KK, AK are the monsters here."));

        questions.add(new Question("Ranges",
            "What hands 'smash' a flop like 7♠6♠5♣?",
            null, null, null,
            new String[]{"98, 84, 43, 77, 66, 55 - straights and sets", "AK", "High pairs", "Only premium hands"},
            0, "Low connected boards favor suited connectors and small pairs. 98 has the nut straight."));

        questions.add(new Question("Terminology",
            "What is a 'rainbow' board?",
            null, null, null,
            new String[]{"A board with all three cards in different suits - no flush draws possible", "Colorful cards", "All same suit", "Mixed colors"},
            0, "Rainbow = all different suits. K♠7♥2♦ is rainbow - no flush draw is possible yet."));

        questions.add(new Question("Terminology",
            "What is a 'monotone' board?",
            null, null, null,
            new String[]{"All three flop cards are the same suit - flush draw and made flush possible", "Same rank", "Rainbow", "Paired board"},
            0, "Monotone = all same suit. K♠7♠2♠ means anyone with two spades has flush/flush draw."));

        questions.add(new Question("Terminology",
            "What is a 'two-tone' board?",
            null, null, null,
            new String[]{"Two cards of one suit, one of another - flush draw possible", "Two pair board", "Paired board", "Rainbow"},
            0, "Two-tone = two suited, one different. K♠7♠2♦ has a spade flush draw possible."));

        questions.add(new Question("Ranges",
            "Why does a monotone board require more caution?",
            null, null, null,
            new String[]{"Made flushes possible, and any suited hand has a draw", "It doesn't", "Easier to play", "Less drawing"},
            0, "Monotone boards mean many hands have flush equity. Even top pair isn't safe against the field."));

        questions.add(new Question("Bet Sizing",
            "On a monotone board without the flush, should you c-bet?",
            "You have A♦A♥ on K♠7♠2♠", null, null,
            new String[]{"Often check - you don't have a spade and many hands have equity", "Always bet", "Bet big", "Fold"},
            0, "Without a flush draw yourself, you're vulnerable. Villain has flush/flush draws often. Check or bet small."));

        questions.add(new Question("Ranges",
            "What is 'card removal' on flush boards?",
            null, null, null,
            new String[]{"If you hold flush cards, fewer combinations of flushes exist for villain", "Removing cards physically", "Mucking", "Folding"},
            0, "Holding A♠ on K♠7♠2♠ means villain can't have A♠x suited. You block the nut flush."));

        questions.add(new Question("Bet Sizing",
            "When facing aggression on a monotone board, what question should you ask?",
            null, null, null,
            new String[]{"Do they have the flush? Am I drawing dead or do I have outs?", "What's for lunch?", "Should I bluff?", "Is my pair good?"},
            0, "Monotone board aggression often means flush. Evaluate if you have outs or should fold."));

        questions.add(new Question("Terminology",
            "What is a 'brick' or 'blank'?",
            null, null, null,
            new String[]{"A turn/river card that doesn't complete any draws or change much", "A red card", "A winning card", "A losing card"},
            0, "Brick = changes nothing. On J♠T♥8♦, a 2♣ turn is a brick - no straight, no flush helped."));

        questions.add(new Question("Terminology",
            "Why is a 'brick' turn sometimes good for the preflop aggressor?",
            null, null, null,
            new String[]{"Aggressor's range is less affected; they can continue representing strong hands", "It's bad", "No effect", "Always helps caller"},
            0, "Brick turns don't help the caller's speculative hands. Aggressor can double barrel confidently."));

        questions.add(new Question("Ranges",
            "What is 'card coverage'?",
            null, null, null,
            new String[]{"Having hands in your range that connect with various board runouts", "Insurance", "Side bets", "Card protector"},
            0, "Card coverage means your range isn't dead on any board. Balanced ranges 'cover' many runouts."));

        questions.add(new Question("Ranges",
            "Why is card coverage important in your 3-bet range?",
            null, null, null,
            new String[]{"So you're not exploitably weak on low/connected boards", "It's not important", "Only for pros", "Cash only"},
            0, "If you only 3-bet AA-QQ, you're crushed on 765 boards. Include suited connectors for coverage."));

        questions.add(new Question("Terminology",
            "What is a 'board lock'?",
            null, null, null,
            new String[]{"A hand that cannot lose regardless of remaining cards", "A tied board", "A blocked hand", "A straight"},
            0, "Board lock = the nuts with no redraw possible. Royal flush is always a board lock."));

        questions.add(new Question("Pot Odds",
            "You have the nut straight. Board has a flush draw. Are you 'board locked'?",
            "Board: A♠K♣Q♦J♠, you have T9", null, null,
            new String[]{"No - a fourth spade makes a flush possible", "Yes, always", "Straights are locks", "Depends on your suits"},
            0, "You have the nuts NOW but aren't board locked. Flush or full house could come."));

        questions.add(new Question("Pot Odds",
            "What is 'free-rolling'?",
            null, null, null,
            new String[]{"Having the same hand as opponent but with extra outs to improve", "Free tournament", "Rolling dice", "Limping"},
            0, "Free-roll: you have K♠Q♠, opponent has K♦Q♥ on KQx board. You chop unless spades come, then you win."));

        questions.add(new Question("Pot Odds",
            "You're free-rolling with a flush draw. Should you try to get money in?",
            null, null, null,
            new String[]{"Yes - at worst you chop, at best you scoop", "No, play passive", "Fold", "Check always"},
            0, "Free-rolling is +EV. You can't lose; you can only chop or win. Get the money in if possible."));

        questions.add(new Question("Terminology",
            "What is 'dead money' in a pot?",
            null, null, null,
            new String[]{"Money in pot from players who have folded - already contributes to your equity", "Lost chips", "Bad bets", "Blinds only"},
            0, "Dead money = chips from players no longer in hand. When you win, you get their contributions too."));

        questions.add(new Question("Terminology",
            "Why does dead money affect squeeze plays?",
            null, null, null,
            new String[]{"More dead money = more profitable squeeze since pot is bigger relative to risk", "It doesn't", "Less squeezing", "Only for value"},
            0, "Dead money improves squeeze EV. If 3 players see a flop with antes, squeezing wins more immediately."));

        questions.add(new Question("Terminology",
            "What is 'burning a card' in live poker?",
            null, null, null,
            new String[]{"Discarding the top card before dealing community cards to prevent cheating", "Setting cards on fire", "A bad beat", "Folding"},
            0, "Burn = discard face-down before flop/turn/river. Prevents marked card manipulation."));

        questions.add(new Question("Terminology",
            "What is 'splashing the pot'?",
            null, null, null,
            new String[]{"Tossing chips directly into the pot instead of placing them in front - against etiquette", "Good chip handling", "A celebration", "Winning big"},
            0, "Splashing makes it hard to count the bet. Put chips in front of you, then dealer moves them."));

        questions.add(new Question("Terminology",
            "What is 'string betting'?",
            null, null, null,
            new String[]{"Putting chips in over multiple motions or announcing and changing bet - not allowed", "A long bet", "Multi-pot bet", "Correct betting"},
            0, "String bet = 'I'll call...and raise!' This is illegal. State your action or put chips in ONE motion."));

        questions.add(new Question("Terminology",
            "What is 'angle shooting'?",
            null, null, null,
            new String[]{"Unethical but technically legal actions to gain unfair advantage", "A poker angle", "Trick shots", "Bluffing"},
            0, "Angle shots: fake folding, misleading chip counts, acting out of turn intentionally. Frowned upon heavily."));

        questions.add(new Question("Position",
            "What is 'position awareness'?",
            null, null, null,
            new String[]{"Understanding how position affects strategy and adjusting accordingly", "Knowing your seat", "Being alert", "Standing at table"},
            0, "Position awareness = constantly thinking about relative position and adjusting ranges and bet sizes."));

        questions.add(new Question("Position",
            "How does being 'first in' (limps folded to you) affect your range?",
            "Everyone folds to you on BTN", null, null,
            new String[]{"You can open very wide since only blinds remain", "Play tighter", "Same as always", "Only premium hands"},
            0, "First-in on BTN/CO = only blinds to beat. Open wide and use position advantage."));

        questions.add(new Question("Ranges",
            "What is 'opening range by position'?",
            null, null, null,
            new String[]{"Your preflop raising range adjusted based on which position you're in", "Same range everywhere", "Only pairs", "Random hands"},
            0, "Open tighter from EP (more players behind), wider from LP (fewer players, better position)."));

        questions.add(new Question("Ranges",
            "How much wider is BTN opening range vs UTG typically?",
            null, null, null,
            new String[]{"About 2-3x wider (45-50% BTN vs 15-18% UTG)", "Same range", "Slightly wider", "10x wider"},
            0, "BTN opens ~45% vs UTG ~16%. Huge difference due to position and fewer opponents."));

        questions.add(new Question("Terminology",
            "What does 'defending the big blind' mean?",
            null, null, null,
            new String[]{"Calling or raising from BB when facing a raise - already have money invested", "Protecting chips", "Folding BB", "Betting big"},
            0, "BB has 1bb in; getting favorable odds to defend wider than you'd cold call."));

        questions.add(new Question("Pot Odds",
            "How do BB pot odds affect defending range?",
            "BTN opens 2.5bb, SB folds, you're BB", null, null,
            new String[]{"You get ~36% pot odds, allowing wide calling range (50%+ of hands)", "Fold everything", "Only call premiums", "3-bet only"},
            0, "Call 1.5bb to win 4bb = 27.5% odds needed. You can defend very widely with any playable hand."));

        questions.add(new Question("Terminology",
            "What is 'flat-calling' vs 3-betting?",
            null, null, null,
            new String[]{"Calling rather than re-raising with hands that could go either way", "Calling with anything", "Raising always", "Folding"},
            0, "Flat = just call with hands like JJ, AQs that are good but may not want to 3-bet always."));

        questions.add(new Question("Ranges",
            "What hands are good 'flatting' candidates vs an EP open?",
            null, null, null,
            new String[]{"Medium pairs (77-TT), suited broadways (KQs, QJs), suited connectors in position", "AA only", "Random hands", "All aces"},
            0, "Flat with hands that play well postflop: pairs (set mine), suited broadway (strong draws), position helps."));

        questions.add(new Question("Terminology",
            "What is a 'probe opportunity'?",
            null, null, null,
            new String[]{"When villain checks after previous street aggression, signaling weakness", "Opening a pot", "First bet", "Preflop raise"},
            0, "Probe opportunity arises when aggressor checks. Their range is capped - attack with bets."));

        questions.add(new Question("Bet Sizing",
            "What size should probe bets typically be?",
            null, null, null,
            new String[]{"Small to medium (25-50% pot) - you're attacking weakness, not building massive pot", "Pot-sized", "All-in", "Min bet"},
            0, "Probes are attacks on weakness. Small sizes work since villain's range is capped."));

        questions.add(new Question("Ranges",
            "What is 'stabbing' at a pot?",
            null, null, null,
            new String[]{"Making a bet when it checks around to you, attacking dead money", "Aggressive fold", "Big bluff", "All-in move"},
            0, "Stabbing = betting when no one shows interest. Pick up dead money with any two cards."));

        questions.add(new Question("Ranges",
            "In a limped pot, flop checks around. What should you do on BTN?",
            null, null, null,
            new String[]{"Bet frequently - everyone showed weakness, steal with most hands", "Always check", "Only bet nuts", "Fold"},
            0, "Limped pots with checks = no one has anything. Stab with high frequency."));

        questions.add(new Question("Terminology",
            "What is 'floating'?",
            null, null, null,
            new String[]{"Calling a bet with a weak hand, planning to take pot away on later street", "Folding weak hands", "Betting small", "Check-raising"},
            0, "Float = call flop with weak hand, planning to bluff turn/river if villain gives up."));

        questions.add(new Question("Ranges",
            "What makes a good floating situation?",
            null, null, null,
            new String[]{"Position, villain likely c-betting wide, turn cards that help your bluff story", "Any situation", "OOP floating", "With the nuts"},
            0, "Float IP when villain c-bets too much and gives up on turns. Position is essential for floating."));

        questions.add(new Question("Ranges",
            "Why is floating dangerous OOP?",
            null, null, null,
            new String[]{"You act first on turn - harder to bluff without position", "It's not dangerous", "Better OOP", "OOP is fine"},
            0, "OOP floating = you call flop, then check turn blind. Villain can check behind for showdown or bet again."));

        questions.add(new Question("Terminology",
            "What is 'delayed c-bet'?",
            null, null, null,
            new String[]{"Checking flop as preflop aggressor, then betting turn", "Slow c-bet", "Late bet", "Third barrel"},
            0, "Delay c-bet = check flop, bet turn. Works on wet flops that brick out on turn."));

        questions.add(new Question("Bet Sizing",
            "When is delayed c-betting effective?",
            null, null, null,
            new String[]{"Wet flops where you'd face raises, then brick turns where villain checks to you", "Always", "Never", "Dry boards only"},
            0, "On 987 flop, checking avoids raises. On 2♣ turn, villain likely checks; now you can bluff effectively."));

        questions.add(new Question("Ranges",
            "What is 'delayed aggression'?",
            null, null, null,
            new String[]{"Waiting for a later street to show aggression rather than betting immediately", "Slow play", "Passive", "Immediate betting"},
            0, "Delayed aggression = letting opponent bet first, then raising or betting bigger on later street."));

        questions.add(new Question("Terminology",
            "What is 'pot manipulation'?",
            null, null, null,
            new String[]{"Controlling pot size to match your hand strength - small pot with medium hands", "Cheating", "Always betting big", "Folding"},
            0, "Pot manipulation = keep pots small with marginal hands, big with monsters. Critical skill."));

        questions.add(new Question("Terminology",
            "How do you keep the pot small with a medium-strength hand?",
            null, null, null,
            new String[]{"Check, call small bets, or bet small yourself - avoid big bets and raises", "Bet huge", "Raise always", "Go all-in"},
            0, "With marginal holdings, don't inflate the pot. You want to get to showdown cheaply."));

        questions.add(new Question("Bet Sizing",
            "What is 'pot geometry'?",
            null, null, null,
            new String[]{"How bet sizes across streets affect final pot size and remaining stacks", "Pot shape", "Table layout", "Card arrangement"},
            0, "Pot geometry = if you bet X on flop, Y on turn, you'll have Z behind on river. Plan all streets."));

        questions.add(new Question("Bet Sizing",
            "Why should you plan bet sizing across all streets?",
            null, null, null,
            new String[]{"To ensure you can get all-in or leave correct amounts for river decisions", "Doesn't matter", "Only think about current street", "Random sizing works"},
            0, "Planning prevents awkward stack sizes. Don't bet flop/turn then have weird amounts left on river."));

        questions.add(new Question("Pot Odds",
            "What is 'pot manipulation' EV?",
            null, null, null,
            new String[]{"Value gained or lost by controlling pot size relative to hand strength", "A calculation", "Chip counting", "Profit margin"},
            0, "Big pot with big hand = +EV. Big pot with weak hand = -EV. Match pot size to hand strength."));

        questions.add(new Question("Terminology",
            "What is 'playing fast' vs 'playing slow'?",
            null, null, null,
            new String[]{"Fast = betting/raising aggressively. Slow = checking/calling, trapping", "Game speed", "Time bank", "Player pace"},
            0, "Fast play builds pots quickly. Slow play traps. Choose based on hand strength and board."));

        questions.add(new Question("Terminology",
            "When should you 'play fast' with a strong hand?",
            null, null, null,
            new String[]{"On wet boards with many draws where you need to protect", "On dry boards", "Never", "Always slow play"},
            0, "On wet boards, betting protects against draws. Don't let them see free cards."));

        questions.add(new Question("Terminology",
            "When can you 'slow play' safely?",
            null, null, null,
            new String[]{"On dry boards with few draws where you have a monster hand", "On wet boards", "With weak hands", "Never slow play"},
            0, "Slow play on K♦7♠2♣ with a set. Few draws exist; let opponent catch up or bluff."));

        questions.add(new Question("Ranges",
            "What is 'equity retention'?",
            null, null, null,
            new String[]{"How much of your equity you can keep by seeing additional cards", "Keeping chips", "Retaining position", "Saving cards"},
            0, "Equity retention = realizing your theoretical equity. Draws that see river retain more equity."));

        questions.add(new Question("Ranges",
            "Why do drawing hands have 'equity retention' issues OOP?",
            null, null, null,
            new String[]{"You might fold to aggression before seeing all cards", "They don't", "OOP is fine", "Draws always work"},
            0, "OOP with a draw = might fold turn facing bet, never seeing river. Can't realize full equity."));

        questions.add(new Question("Pot Odds",
            "What is 'equity realization' percentage?",
            null, null, null,
            new String[]{"The percentage of raw equity you actually capture based on playability", "Always 100%", "Fixed number", "Card counting"},
            0, "A hand with 50% equity might only realize 40% if OOP or facing aggression. Playability matters."));

        questions.add(new Question("Terminology",
            "What affects equity realization?",
            null, null, null,
            new String[]{"Position, stack depth, hand playability, opponent skill", "Only the cards", "Luck", "Time of day"},
            0, "Position is biggest factor. Also: draws vs made hands, deep stacks for implied odds."));

        questions.add(new Question("Pot Odds",
            "Hands like A5o have 'poor equity realization' because:",
            null, null, null,
            new String[]{"They don't flop strong often and are hard to continue with postflop", "They realize 100%", "Always win", "Great playability"},
            0, "A5o flops weak pairs or nothing. Hard to know where you stand; often fold to aggression."));

        questions.add(new Question("Pot Odds",
            "Hands like 76s have 'better equity realization' because:",
            null, null, null,
            new String[]{"They flop strong draws and made hands that are easy to play", "They don't", "High card value", "Always win"},
            0, "76s flops pairs, draws, and monsters. You know when you're strong and can apply pressure."));

        questions.add(new Question("Terminology",
            "What is 'playing scared money'?",
            null, null, null,
            new String[]{"Playing too timidly because losing the money would hurt - risk-averse decisions", "Playing scared", "Bluffing less", "Tight play"},
            0, "Scared money = when stakes are too high, you play timidly and make -EV folds."));

        questions.add(new Question("Terminology",
            "Why is scared money problematic?",
            null, null, null,
            new String[]{"You can't make optimal decisions when afraid of losing", "It's not", "You win more", "Safer play is better"},
            0, "Optimal poker requires risking chips. Scared money leads to folding too much."));

        questions.add(new Question("Terminology",
            "What is 'bankroll management' (BRM)?",
            null, null, null,
            new String[]{"Playing stakes appropriate for your bankroll to minimize risk of ruin", "Counting money", "Saving chips", "Buying in max"},
            0, "BRM = playing stakes where losing streaks won't bust you. Usually 20-30 buy-ins for cash."));

        questions.add(new Question("Ranges",
            "What is 'GTO defense frequency' against a 2/3 pot bet?",
            null, null, null,
            new String[]{"About 60% - continue with 60% of your range", "100%", "25%", "50%"},
            0, "MDF = 1 - (bet/pot+bet) = 1 - (0.67/1.67) = 60%. Defend 60% to prevent auto-profit bluffs."));

        questions.add(new Question("Ranges",
            "What hands should you prioritize when meeting MDF?",
            null, null, null,
            new String[]{"Hands with best equity: strong made hands and good draws", "Random hands", "Worst hands", "Only pairs"},
            0, "To meet MDF, continue with hands that have the best expected value against villain's range."));

        questions.add(new Question("Terminology",
            "What is 'over-defending' vs 'under-defending'?",
            null, null, null,
            new String[]{"Over = calling too much. Under = folding too much vs optimal", "Both are bad", "Neither matters", "Only in tournaments"},
            0, "Over-defending vs over-folding. Both are leaks. Find the balance based on opponent tendencies."));

        questions.add(new Question("Terminology",
            "What does 'villain is over-folding' mean for your strategy?",
            null, null, null,
            new String[]{"Bluff more - your bluffs profit since they fold too often", "Bluff less", "Value bet more", "Play tighter"},
            0, "Over-folders = bluff more. Your bluffs work more often than they should mathematically."));

        questions.add(new Question("Terminology",
            "What does 'villain is over-calling' mean for your strategy?",
            null, null, null,
            new String[]{"Value bet thinner, bluff less - they call too much with weak hands", "Bluff more", "Play tighter", "Fold more"},
            0, "Against calling stations, bet thin value (they call with worse) and reduce bluffs (they don't fold)."));

        questions.add(new Question("Pot Odds",
            "What is the 'rule of 4 and 2' for calculating equity?",
            null, null, null,
            new String[]{"Outs × 4 on flop = river equity; Outs × 2 on turn = river equity", "Random numbers", "Pot odds rule", "Stack calculation"},
            0, "Quick equity estimate: flop = outs×4 (two cards to come), turn = outs×2 (one card)."));

        questions.add(new Question("Pot Odds",
            "You have 9 outs on the flop. Quick estimate of river equity?",
            null, null, null,
            new String[]{"About 36% (9 × 4 = 36)", "18%", "45%", "9%"},
            0, "9 outs × 4 = 36%. Actual is about 35%, so the rule is close enough for quick math."));

        questions.add(new Question("Pot Odds",
            "You have 9 outs on the turn. Quick estimate of river equity?",
            null, null, null,
            new String[]{"About 18% (9 × 2 = 18)", "36%", "9%", "27%"},
            0, "Turn: 9 outs × 2 = 18%. Only one card to come, so roughly half the flop-to-river equity."));

        questions.add(new Question("Pot Odds",
            "How many outs is a flush draw?",
            null, null, null,
            new String[]{"9 outs - 13 cards of suit minus 4 you can see", "13", "4", "12"},
            0, "13 cards in suit - 4 visible (2 in hand, 2 on board) = 9 unseen cards that complete your flush."));

        questions.add(new Question("Pot Odds",
            "How many outs is an open-ended straight draw?",
            null, null, null,
            new String[]{"8 outs - 4 cards on each end of the straight", "4", "12", "6"},
            0, "OESD can complete with 4 of one rank + 4 of another = 8 outs. Example: 67 on 58X can hit 4 or 9."));

        questions.add(new Question("Pot Odds",
            "How many outs is a gutshot straight draw?",
            null, null, null,
            new String[]{"4 outs - only one rank completes it", "8", "2", "6"},
            0, "Gutshot needs one specific card. 4 of that rank exist = 4 outs. Example: 67 on 59X needs 8."));

        questions.add(new Question("Pot Odds",
            "What is a 'double gutshot'?",
            null, null, null,
            new String[]{"A straight draw with 8 outs hitting two different inside cards", "Two pair", "Set draw", "Flush draw"},
            0, "Double gutshot = 8 outs like OESD. Example: 79 on 8TQ makes straight with J OR 6."));

        questions.add(new Question("Pot Odds",
            "Why is a double gutshot sometimes better than OESD?",
            null, null, null,
            new String[]{"It's more disguised - completing cards look like random runouts", "It's not", "More outs", "Better odds"},
            0, "Double gutshots hide your hand. OESD completes with obvious 4-straight boards."));

        questions.add(new Question("Ranges",
            "What are 'backdoor' draws?",
            null, null, null,
            new String[]{"Draws needing two cards to complete (runner-runner)", "Secondary draws", "Second best draws", "Weak draws"},
            0, "Backdoor = need turn AND river. Backdoor flush needs two suited cards to fall."));

        questions.add(new Question("Ranges",
            "How many 'outs' does a backdoor flush draw add?",
            null, null, null,
            new String[]{"About 1-1.5 outs of additional equity", "9 outs", "4 outs", "0 outs"},
            0, "Backdoor flush adds roughly 1-1.5 outs (4% equity). Marginal but helps."));

        questions.add(new Question("Ranges",
            "Why do backdoor draws affect hand selection?",
            null, null, null,
            new String[]{"They add equity and flexibility for semi-bluffing", "They don't matter", "Only flush draws count", "Ignore backdoors"},
            0, "Hands with backdoor potential (suited, connected) have more equity to realize on future streets."));

        questions.add(new Question("Terminology",
            "What is 'nut' in poker?",
            null, null, null,
            new String[]{"The best possible hand given the board", "A crazy play", "A bad hand", "The worst hand"},
            0, "Nuts = best possible hand. On K♠Q♠J♠T♠2♦, the nut hand is A♠x (royal flush)."));

        questions.add(new Question("Terminology",
            "What is the 'second nuts'?",
            null, null, null,
            new String[]{"The second-best possible hand", "Two pairs", "Nothing", "A losing hand"},
            0, "Second nuts = 2nd best. On AKQ72, AK is the nuts, AQ is second nuts."));

        questions.add(new Question("Terminology",
            "What is a 'nut advantage' on a flop?",
            null, null, null,
            new String[]{"Having more combinations of the very best hands than opponent", "Any advantage", "Having all nuts", "Position"},
            0, "Nut advantage = more nutted combos. On AKx, preflop raiser has more AA, KK, AK."));

        questions.add(new Question("Ranges",
            "Who has nut advantage on a 7♠6♠5♣ flop: BTN or BB?",
            "BTN raised, BB called", null, null,
            new String[]{"BB often has more straights (98, 43) and sets (77, 66, 55)", "BTN always", "Neither", "It's equal"},
            0, "Low connected boards favor the caller's range. BB has suited connectors and small pairs more often."));

        questions.add(new Question("Ranges",
            "Who typically has nut advantage on high card boards like A♠K♦Q♣?",
            "BTN raised, BB called", null, null,
            new String[]{"BTN - has more AA, KK, QQ, AK combinations", "BB always", "Neither", "Equal ranges"},
            0, "Broadway boards favor raisers who have more premium hands. BTN has huge nut advantage here."));

        questions.add(new Question("Bet Sizing",
            "How does nut advantage affect betting strategy?",
            null, null, null,
            new String[]{"With nut advantage, you can bet larger and more frequently", "No effect", "Bet smaller", "Always check"},
            0, "Nut advantage = villain can't have nuts often. Bigger bets are less risky and more profitable."));

        questions.add(new Question("Bet Sizing",
            "Without nut advantage, what adjustment should you make?",
            null, null, null,
            new String[]{"Check more, use smaller bet sizes when betting", "Bet huge", "Always bluff", "Go all-in"},
            0, "Without nut advantage, you're at risk of facing big raises. Be more cautious with bet sizes."));

        questions.add(new Question("Ranges",
            "What is 'range asymmetry'?",
            null, null, null,
            new String[]{"When players' ranges differ significantly due to prior actions", "Equal ranges", "Perfect balance", "Random hands"},
            0, "Range asymmetry = one player's range looks very different from another's. Creates strategic differences."));

        questions.add(new Question("Ranges",
            "3-bet pots typically have more range asymmetry because:",
            null, null, null,
            new String[]{"Both players showed significant strength preflop, narrowing ranges", "Less asymmetry", "Wider ranges", "No change"},
            0, "3-bet pots = both showed strength. Ranges are narrow and often favoring the 3-bettor."));

        questions.add(new Question("Terminology",
            "What is 'postflop game'?",
            null, null, null,
            new String[]{"All strategy and betting after the flop is dealt", "After showdown", "Preflop only", "Endgame"},
            0, "Postflop = flop, turn, river streets. Where most of the poker skill comes into play."));

        questions.add(new Question("Terminology",
            "What is 'multiway' pot?",
            null, null, null,
            new String[]{"A pot with three or more players", "Two players", "Heads up", "Final table"},
            0, "Multiway = 3+ players. Strategy changes significantly from heads-up pots."));

        questions.add(new Question("Ranges",
            "How do multiway pots affect bluffing?",
            null, null, null,
            new String[]{"Bluff less - at least one player likely has something", "Bluff more", "No change", "Always bluff"},
            0, "More players = someone likely has a hand. Reduce bluff frequency in multiway pots."));

        questions.add(new Question("Ranges",
            "How do multiway pots affect value betting?",
            null, null, null,
            new String[]{"Bet for value more - someone is likely to call with second-best hand", "Value bet less", "No change", "Never value bet"},
            0, "More players = more likely someone calls. Value bet your strong hands confidently."));

        questions.add(new Question("Terminology",
            "What is 'reverse floating'?",
            null, null, null,
            new String[]{"Calling OOP planning to check-raise or lead later streets", "Normal float", "Folding", "Value betting"},
            0, "Reverse float = call OOP with plan to take pot on later street. Risky but can work."));

        questions.add(new Question("Terminology",
            "What is a 'hero call'?",
            null, null, null,
            new String[]{"Calling a big bet with a marginal hand believing opponent is bluffing", "A big bet", "Folding", "A raise"},
            0, "Hero call = calling all-in with 3rd pair because you read a bluff. Requires strong reads."));

        questions.add(new Question("Terminology",
            "When are hero calls appropriate?",
            null, null, null,
            new String[]{"When you have strong reads that villain is bluffing", "Always", "Never", "With the nuts"},
            0, "Hero calls need evidence: timing tells, sizing tells, or historical patterns suggesting a bluff."));

        questions.add(new Question("Terminology",
            "What's the risk of hero calling too much?",
            null, null, null,
            new String[]{"You'll pay off value hands, massively harming your win rate", "No risk", "You'll win more", "Good for image"},
            0, "Hero calling too often = paying off their value. Balance hero calls with appropriate folds."));

        questions.add(new Question("Terminology",
            "What is 'blockers' in bluff catching?",
            null, null, null,
            new String[]{"Cards you hold that reduce combinations of villain's value hands", "Bluff blockers", "Fold blockers", "Call blockers"},
            0, "When calling, you want to block villain's value hands and unblock their bluffs."));

        questions.add(new Question("Ranges",
            "For bluff-catching, should you prefer blocking villain's value or bluffs?",
            null, null, null,
            new String[]{"Block their VALUE hands - this increases the chance they're bluffing", "Block their bluffs", "Doesn't matter", "Block nothing"},
            0, "Blocking value (like holding Ax on an Ace-high board) means they less likely have top pair."));

        questions.add(new Question("Ranges",
            "For bluffing, should you prefer blocking villain's calls or folds?",
            null, null, null,
            new String[]{"Block their CALLING hands - this increases fold frequency", "Block their folds", "Doesn't matter", "Block everything"},
            0, "Bluffing with A♠ blocks AA, AK that would call. You want to block hands that continue vs your bluff."));

        questions.add(new Question("Terminology",
            "What is 'range morphing'?",
            null, null, null,
            new String[]{"How ranges transform based on actions throughout a hand", "Changing cards", "New ranges", "Different strategy"},
            0, "Range morphing = preflop range → flop continue range → turn range → river range. Each action filters."));

        questions.add(new Question("Terminology",
            "How does a check on the flop 'morph' the aggressor's range?",
            null, null, null,
            new String[]{"It caps their range - strong hands would have bet", "No effect", "Makes it stronger", "Random change"},
            0, "Checking caps range. If they bet with all strong hands, their check = medium or weak holdings."));

        questions.add(new Question("Ranges",
            "After flop check → turn check, what's villain's range like?",
            "Villain was preflop aggressor", null, null,
            new String[]{"Very capped - strong hands would have bet at least once", "Uncapped", "Stronger than ever", "Random"},
            0, "Two checks = two chances to bet missed. Range is super capped, attack with bets."));

        return questions;
    }

    /**
     * Filter questions based on selected categories.
     */
    private void filterQuestions() {
        if (activeCategories.contains("All")) {
            filteredQuestions = new ArrayList<>(allQuestions);
        } else {
            filteredQuestions = new ArrayList<>();
            for (Question q : allQuestions) {
                if (activeCategories.contains(q.category)) {
                    filteredQuestions.add(q);
                }
            }
        }

        if (filteredQuestions.isEmpty()) {
            filteredQuestions = new ArrayList<>(allQuestions);
        }
    }

    /**
     * Load the next random question.
     */
    private void loadNextQuestion() {
        answered = false;

        // Reset button states
        feedbackBox.setVisible(false);
        feedbackBox.setManaged(false);
        nextBtn.setVisible(false);
        nextBtn.setManaged(false);

        answer1Btn.setDisable(false);
        answer2Btn.setDisable(false);
        answer3Btn.setDisable(false);
        answer4Btn.setDisable(false);
        answer1Btn.getStyleClass().removeAll("correct", "incorrect", "was-correct");
        answer2Btn.getStyleClass().removeAll("correct", "incorrect", "was-correct");
        answer3Btn.getStyleClass().removeAll("correct", "incorrect", "was-correct");
        answer4Btn.getStyleClass().removeAll("correct", "incorrect", "was-correct");

        // Pick random question
        filterQuestions();
        currentQuestion = filteredQuestions.get(random.nextInt(filteredQuestions.size()));

        // Set category label
        questionTypeLabel.setText(currentQuestion.category.toUpperCase());

        // Show/hide scenario
        if (currentQuestion.scenario != null && !currentQuestion.scenario.isEmpty()) {
            scenarioLabel.setText(currentQuestion.scenario);
            scenarioLabel.setVisible(true);
            scenarioLabel.setManaged(true);
        } else {
            scenarioLabel.setVisible(false);
            scenarioLabel.setManaged(false);
        }

        // Show/hide hand display
        if (currentQuestion.card1 != null && currentQuestion.card2 != null) {
            card1Label.setText(currentQuestion.card1);
            card2Label.setText(currentQuestion.card2);
            handDisplay.setVisible(true);
            handDisplay.setManaged(true);
        } else {
            handDisplay.setVisible(false);
            handDisplay.setManaged(false);
        }

        // Show/hide position badge
        if (currentQuestion.position != null) {
            positionBadge.setText(currentQuestion.position);
            positionBadge.setVisible(true);
            positionBadge.setManaged(true);
        } else {
            positionBadge.setVisible(false);
            positionBadge.setManaged(false);
        }

        // Set question
        questionLabel.setText(currentQuestion.question);

        // Shuffle answers
        List<Integer> indices = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(indices);

        Button[] buttons = {answer1Btn, answer2Btn, answer3Btn, answer4Btn};
        for (int i = 0; i < 4; i++) {
            int originalIndex = indices.get(i);
            buttons[i].setText(currentQuestion.answers[originalIndex]);
            buttons[i].setUserData(originalIndex);

            if (originalIndex == currentQuestion.correctIndex) {
                correctAnswerIndex = i;
            }
        }
    }

    /**
     * Handle answer button click.
     */
    @FXML
    private void handleAnswer(ActionEvent event) {
        if (answered) return;
        answered = true;
        totalQuestions++;

        Button clicked = (Button) event.getSource();
        int selectedOriginalIndex = (int) clicked.getUserData();
        boolean isCorrect = selectedOriginalIndex == currentQuestion.correctIndex;

        // Disable all buttons
        answer1Btn.setDisable(true);
        answer2Btn.setDisable(true);
        answer3Btn.setDisable(true);
        answer4Btn.setDisable(true);

        // Mark correct/incorrect
        Button[] buttons = {answer1Btn, answer2Btn, answer3Btn, answer4Btn};
        for (Button btn : buttons) {
            int idx = (int) btn.getUserData();
            if (idx == currentQuestion.correctIndex) {
                if (btn == clicked) {
                    btn.getStyleClass().add("correct");
                } else {
                    btn.getStyleClass().add("was-correct");
                }
            } else if (btn == clicked) {
                btn.getStyleClass().add("incorrect");
            }
        }

        // Update stats
        if (isCorrect) {
            correctAnswers++;
            currentStreak++;
            if (currentStreak > bestStreak) {
                bestStreak = currentStreak;
            }
            feedbackBox.getStyleClass().removeAll("feedback-correct", "feedback-incorrect");
            feedbackBox.getStyleClass().add("feedback-correct");
            feedbackLabel.setText("Correct! " + currentQuestion.explanation);
        } else {
            currentStreak = 0;
            feedbackBox.getStyleClass().removeAll("feedback-correct", "feedback-incorrect");
            feedbackBox.getStyleClass().add("feedback-incorrect");
            feedbackLabel.setText("Incorrect. " + currentQuestion.explanation);
        }

        // Update display
        correctCountLabel.setText(String.valueOf(correctAnswers));
        int accuracy = totalQuestions > 0 ? (correctAnswers * 100) / totalQuestions : 0;
        accuracyLabel.setText(accuracy + "%");
        streakLabel.setText(String.valueOf(currentStreak));

        // Show feedback
        feedbackBox.setVisible(true);
        feedbackBox.setManaged(true);
        nextBtn.setVisible(true);
        nextBtn.setManaged(true);
    }

    /**
     * Move to next question.
     */
    @FXML
    private void handleNextQuestion() {
        loadNextQuestion();
    }

    /**
     * Handle category toggle.
     */
    @FXML
    private void handleCategoryChange(ActionEvent event) {
        ToggleButton clicked = (ToggleButton) event.getSource();

        if (clicked == categoryAll) {
            if (clicked.isSelected()) {
                // Deselect all others
                categoryPosition.setSelected(false);
                categoryRanges.setSelected(false);
                categoryOdds.setSelected(false);
                categoryHands.setSelected(false);
                categoryTerms.setSelected(false);
                categorySizing.setSelected(false);
                categoryStack.setSelected(false);
                activeCategories.clear();
                activeCategories.add("All");
            } else {
                // Must have at least one selected
                clicked.setSelected(true);
            }
        } else {
            // Deselect "All" if any specific category is selected
            if (clicked.isSelected()) {
                categoryAll.setSelected(false);
                activeCategories.remove("All");

                // Add this category
                String category = clicked.getText();
                if (category.equals("Pot Odds")) category = "Pot Odds";
                else if (category.equals("Hand Rankings")) category = "Hand Rankings";
                else if (category.equals("Bet Sizing")) category = "Bet Sizing";
                else if (category.equals("Stack Depth")) category = "Stack Depth";

                activeCategories.add(category);
            } else {
                // Remove this category
                String category = clicked.getText();
                if (category.equals("Pot Odds")) category = "Pot Odds";
                else if (category.equals("Hand Rankings")) category = "Hand Rankings";
                else if (category.equals("Bet Sizing")) category = "Bet Sizing";
                else if (category.equals("Stack Depth")) category = "Stack Depth";

                activeCategories.remove(category);

                // If none selected, select "All"
                if (activeCategories.isEmpty()) {
                    categoryAll.setSelected(true);
                    activeCategories.add("All");
                }
            }
        }
    }

    /**
     * Reset statistics.
     */
    @FXML
    private void handleResetStats() {
        totalQuestions = 0;
        correctAnswers = 0;
        currentStreak = 0;
        bestStreak = 0;

        correctCountLabel.setText("0");
        accuracyLabel.setText("0%");
        streakLabel.setText("0");
    }

    /**
     * Show a random tip.
     */
    @FXML
    private void handleShuffleTip() {
        shuffleTip();
    }

    private void shuffleTip() {
        String[] tips = {
            "Position is power in poker. Acting last gives you more information about your opponents' hands.",
            "Play tighter from early position and looser from late position.",
            "Pot odds tell you the minimum equity you need to call profitably.",
            "A continuation bet should be made for a reason - don't just autopilot.",
            "Suited connectors play best from position with deep stacks.",
            "3-betting helps you build pots with your best hands and define villain's range.",
            "With a low SPR, you should be more willing to commit with top pair.",
            "The Button is the most profitable position because you always act last postflop.",
            "Implied odds matter more when your hand is disguised.",
            "A polarized range contains very strong hands and bluffs, but not medium hands.",
            "In short-stacked play, push/fold strategy becomes optimal around 10-15bb.",
            "Your c-bet frequency should vary based on board texture and position.",
            "Pay attention to bet sizing tells - they often reveal hand strength.",
            "Fold equity is the chance your opponent folds when you bet or raise.",
            "Playing too many hands from the blinds is a major leak for beginners.",
            "Set-mining is only profitable with sufficient implied odds (usually 20:1).",
            "Blockers matter more in polarized spots like river all-ins.",
            "Thin value betting separates good players from great players.",
            "Don't slowplay strong hands with lots of draws on the board.",
            "Your table image affects how opponents play against you."
        };

        tipLabel.setText(tips[random.nextInt(tips.length)]);
    }

    /**
     * Question data class.
     */
    private static class Question {
        String category;
        String question;
        String scenario;  // Optional context
        String card1, card2;  // Optional hand display
        String position;  // Optional position badge
        String[] answers;
        int correctIndex;
        String explanation;

        Question(String category, String question, String scenario, String card1, String card2,
                 String[] answers, int correctIndex, String explanation) {
            this.category = category;
            this.question = question;
            this.scenario = scenario;
            this.card1 = card1;
            this.card2 = card2;
            this.answers = answers;
            this.correctIndex = correctIndex;
            this.explanation = explanation;
        }
    }
}
