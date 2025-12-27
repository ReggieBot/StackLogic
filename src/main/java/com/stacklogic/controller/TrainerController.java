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
