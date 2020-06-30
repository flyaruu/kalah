package io.floodplain.kalah;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class KalahGameEngine {

    private static final Logger logger = LoggerFactory.getLogger(KalahGameEngine.class);

    /**
     * All players
     */
    public enum Player {
        PLAYER_ONE, PLAYER_TWO
    }

    /**
     * All possible results after a move
     */
    public enum GameResult {
        PLAYER_ONE_NEXT, PLAYER_TWO_NEXT, PLAYER_ONE_WON, PLAYER_TWO_WON, DRAW
    }

    public static final String PLAYER_ONE_1 = "1";
    public static final String PLAYER_ONE_2 = "2";
    public static final String PLAYER_ONE_3 = "3";
    public static final String PLAYER_ONE_4 = "4";
    public static final String PLAYER_ONE_5 = "5";
    public static final String PLAYER_ONE_6 = "6";
    public static final String PLAYER_ONE_KALAH = "7";
    public static final String PLAYER_TWO_1 = "8";
    public static final String PLAYER_TWO_2 = "9";
    public static final String PLAYER_TWO_3 = "10";
    public static final String PLAYER_TWO_4 = "11";
    public static final String PLAYER_TWO_5 = "12";
    public static final String PLAYER_TWO_6 = "13";
    public static final String PLAYER_TWO_KALAH = "14";

    public static final int PITS = 6;
    public static final int INITIAL_STONES = 6;
    public static final int TOTAL_PITS = PITS * 2 + 2;

    public final String id;
    public final String gameURL;

    // Assume PLAYER_ONE goes first
    // Technically not really necessary to keep track of which player is up now (we could leave that to the clients)
    private Player nextPlayer = Player.PLAYER_ONE;
    // The pits and stones. LinkedHashMap to keep ordering.
    private Map<String, Integer> state = new HashMap<>();

    // Effectively final, custom runs to use in tests will disable verifying the number of stones, allowing easier testing
    private boolean skipStoneChecks = false;

    /**
     * Test constructor, to create specific scenarios
     * This constructor will disable the check to check the total number of stones
     *
     * @param id             The game id
     * @param gameURL        The game URL
     * @param playerOnePits  an array with six integers, which model the pit's of player one
     * @param playerTwoPits  an array with six integers, which model the pit's of player two
     * @param playerOneKalah The number of stones in player one's kalah
     * @param playerTwoKalah The number of stones in player one's kalah
     */
    KalahGameEngine(String id, String gameURL, int[] playerOnePits, int[] playerTwoPits, int playerOneKalah, int playerTwoKalah) {
        this(id, gameURL);
        skipStoneChecks = true;
        int index = 1;
        for (int item : playerOnePits) {
            state.put(Integer.toString(index), item);
            index++;
        }
        index = 8;
        for (int item : playerTwoPits) {
            state.put(Integer.toString(index), item);
            index++;
        }
        state.put(PLAYER_ONE_KALAH, playerOneKalah);
        state.put(PLAYER_TWO_KALAH, playerTwoKalah);
    }

    /**
     * Construct a standard game engine, with six stones in each of the six pits of each player
     *
     * @param id
     * @param gameURL
     */
    public KalahGameEngine(String id, String gameURL) {
        this.id = id;
        this.gameURL = gameURL;
        pitSequence()
                .forEach(i -> {
                    // Kalah's are empty, all others get INITIAL_STONES stones
                    int value = i.equals(PLAYER_ONE_KALAH) || i.equals(PLAYER_TWO_KALAH) ? 0 : INITIAL_STONES;
                    state.put(i, value);
                });
    }

    /**
     * Reconstruct a previously saved game from the map
     *
     * @param id       The game id
     * @param gameURL  The game URL
     * @param contents The map modeling the game contents
     */
    public KalahGameEngine(String id, String gameURL, Map<String, Object> contents) {
        this.id = id;
        this.gameURL = gameURL;
        this.state = contents.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> Integer.parseInt((String) entry.getValue())));
    }

    Integer valueForPit(String pit) {
        return state.get(pit);
    }

    Stream<String> pitSequence() {
        return IntStream.rangeClosed(1, TOTAL_PITS)
                .mapToObj(Integer::toString);
    }

    /**
     * Describes the current game state as ascii art
     *
     * @return
     */
    public String describeState() {
        // create some sick ascii art
        // TODO maybe something prettier, don't want to add a templating dependency
        String template = "|  |b6|b5|b4|b3|b2|b1|  |\n"
                + "|bk-------------------ak|\n"
                + "|  |a1|a2|a3|a4|a5|a6|  |\n";
        template = template.replaceAll("bk", paddedValueForPit(PLAYER_TWO_KALAH));
        template = template.replaceAll("ak", paddedValueForPit(PLAYER_ONE_KALAH));
        template = template.replaceAll("a1", paddedValueForPit(PLAYER_ONE_1));
        template = template.replaceAll("a2", paddedValueForPit(PLAYER_ONE_2));
        template = template.replaceAll("a3", paddedValueForPit(PLAYER_ONE_3));
        template = template.replaceAll("a4", paddedValueForPit(PLAYER_ONE_4));
        template = template.replaceAll("a5", paddedValueForPit(PLAYER_ONE_5));
        template = template.replaceAll("a6", paddedValueForPit(PLAYER_ONE_6));
        template = template.replaceAll("b1", paddedValueForPit(PLAYER_TWO_1));
        template = template.replaceAll("b2", paddedValueForPit(PLAYER_TWO_2));
        template = template.replaceAll("b3", paddedValueForPit(PLAYER_TWO_3));
        template = template.replaceAll("b4", paddedValueForPit(PLAYER_TWO_4));
        template = template.replaceAll("b5", paddedValueForPit(PLAYER_TWO_5));
        template = template.replaceAll("b6", paddedValueForPit(PLAYER_TWO_6));
        return template;
    }

    private String paddedValueForPit(String pit) {
        return String.format("%2d", valueForPit(pit));
    }

    /**
     * This is the actual move.
     * Returns a game result. Otherwise which player is up next
     *
     * @param player The player that makes the move
     * @param pit    The pit this player take the stones from
     * @return A game result, either which player is up next, or which player has won
     * @throws IllegalMoveException When the proposed move is not allowed
     */
    public GameResult startMove(Player player, String pit) throws IllegalMoveException {
        Integer stones = takeStonesFromPit(pit);
        if (stones == null) {
            throw new IllegalMoveException("Illegal move, no such pit: " + pit + " board state: " + describeState());
        }
        if (stones == 0) {
            // at least, I think that that's an illegal move
            throw new IllegalMoveException("Illegal move, can't move from pit: " + pit + " as there are no stones. state:" + describeState());
        }
        if (!player.equals(nextPlayer)) {
            throw new IllegalMoveException("Wrong player is moving. It is: " + nextPlayer + "'s turn");
        }
        // do you have to start on your own side? Rules seem inconsistent

        state.put(pit, 0);
        return distributeAfter(player, pit, stones);
    }

    Integer takeStonesFromPit(String pit) {
        int stones = state.get(pit);
        state.put(pit, 0);
        return stones;
    }

    void addStonesToPit(String pit, int stones) {
        int originalStones = state.get(pit);
        state.put(pit, stones + originalStones);
    }

    private static String nextPit(Player currentPlayer, String pit) {
        logger.debug("next pit for player: {} and current pit: {}", currentPlayer, pit);
        int current = Integer.parseInt(pit);
        String next = Integer.toString((current % TOTAL_PITS) + 1);
        logger.debug("Next pit: {}", next);
        // Use all pits, except the opponents kalah
        if (isKalahForOpponent(next, currentPlayer)) {
            logger.debug("Skipping opponents' kalah: {}", next);
            return nextPit(currentPlayer, next);
        }
        return next;
    }

    private static Player opponent(Player player) {
        switch (player) {
            case PLAYER_ONE:
                return Player.PLAYER_TWO;
            case PLAYER_TWO:
                return Player.PLAYER_ONE;
        }
        throw new IllegalArgumentException("Invalid player type");
    }

    static String oppositePit(String pit) {
        if (isKalahForPlayer(pit, Player.PLAYER_ONE) || isKalahForPlayer(pit, Player.PLAYER_TWO)) {
            throw new IllegalArgumentException("Can't calculate opposite of kalah pits");
        }
        int pitNr = Integer.parseInt(pit);
        return Integer.toString(TOTAL_PITS - pitNr);
    }

    /**
     * Start a recursive call to distribute the stones. It will place a stone in the next pit and
     * call this method again (with one less stone
     *
     * @param player             The player doing the current move
     * @param pit                The pit we started at. This method will not distribute a stone to the current pit.
     * @param stonesToDistribute The remaining number of stones to distribute
     * @return A GameResult: Either which player is up next, or who won
     */
    private GameResult distributeAfter(Player player, String pit, int stonesToDistribute) {
        int totalStones = state.values().stream().reduce(0, Integer::sum);
        logger.debug("Distribute stones after pit: {} player: {} remaining stones: {} stones on the board: {}", pit, player, stonesToDistribute, totalStones);
        // Last stone, end-of-turn logic resides here:
        if (stonesToDistribute == 0) {
            int totalOriginal = PITS * INITIAL_STONES * 2;
            // A sanity check, to be able to diagnose issues with the algo a bit more conveniently.
            // This is disabled using skipStoneChecks for unit tests, so we can use a custom number of stones
            if (!skipStoneChecks && totalStones != totalOriginal) {
                throw new IllegalStateException("Number of stones seems to have changed. I see: " + totalStones + " and it should be " + totalOriginal);
            }

            // check if you can take the opponent's stones
            // TODO unsure if this rule also applies, I've seen different rules
            // player.equals(ownerOfPit(pit));
            if (!isKalah(pit) && valueForPit(pit) == 1 && ownerOfPit(pit).equals(player)) {
                int oppositeStones = valueForPit(oppositePit(pit));
                if (oppositeStones > 0) {
                    logger.info("won stones: {}", (oppositeStones + 1));
                    // Take all stones from the current pit and the pit opposite...
                    int stones = takeStonesFromPit(pit) + takeStonesFromPit(oppositePit(pit));
                    // .. and place them into the current player's Kalah
                    addStonesToPit(kalahForPlayer(player), stones);
                }
            }
            GameResult result = winnerIsDecided();
            switch (result) {
                case PLAYER_ONE_WON:
                    return GameResult.PLAYER_ONE_WON;
                case PLAYER_TWO_WON:
                    return GameResult.PLAYER_TWO_WON;
                case DRAW:
                    return GameResult.DRAW;
                default:
                    // just continue
            }
            if (pit.equals(kalahForPlayer(player))) {
                // ended on the players kalah. Player can play again.
                nextPlayer = player;
                return (nextPlayer == Player.PLAYER_ONE ? GameResult.PLAYER_ONE_NEXT : GameResult.PLAYER_TWO_NEXT);
            } else {
                nextPlayer = opponent(player);
                return (nextPlayer == Player.PLAYER_ONE ? GameResult.PLAYER_ONE_NEXT : GameResult.PLAYER_TWO_NEXT);
            }
        }

        //
        if (isKalahForOpponent(pit, player)) {
            return distributeAfter(player, nextPit(player, pit), stonesToDistribute);
        }
        String nextPit = nextPit(player, pit);
        addStonesToPit(nextPit, 1);
        return distributeAfter(player, nextPit, stonesToDistribute - 1);
    }

    static Player ownerOfPit(String pit) {
        if (Integer.parseInt(pit) <= 7) {
            return Player.PLAYER_ONE;
        } else {
            return Player.PLAYER_TWO;
        }
    }

    /**
     * @return Returns which player has to make the next move
     */
    public Player nextPlayer() {
        return this.nextPlayer;
    }

    private static boolean isKalah(String pit) {
        return pit.equals(PLAYER_ONE_KALAH) || pit.equals(PLAYER_TWO_KALAH);
    }

    static boolean isKalahForPlayer(String pit, Player player) {
        switch (player) {
            case PLAYER_ONE:
                return pit.equals(PLAYER_ONE_KALAH);
            case PLAYER_TWO:
                return pit.equals(PLAYER_TWO_KALAH);
        }
        throw new IllegalStateException("Illegal player");
    }

    static String kalahForPlayer(Player player) {
        switch (player) {
            case PLAYER_ONE:
                return PLAYER_ONE_KALAH;
            case PLAYER_TWO:
                return PLAYER_TWO_KALAH;
        }
        throw new IllegalArgumentException("Bad player? " + player);
    }


    private static boolean isKalahForOpponent(String pit, Player player) {
        if (player == Player.PLAYER_ONE) {
            return pit.equals(PLAYER_TWO_KALAH);
        } else if (player == Player.PLAYER_TWO) {
            return pit.equals(PLAYER_ONE_KALAH);
        }
        if (player == null) {
            throw new IllegalArgumentException("No player supplied");
        }
        throw new IllegalArgumentException("Should not happen");
    }

    /**
     * The game is over when the 'home row' of either player is empty
     *
     * @return
     */
    GameResult winnerIsDecided() {
        int playerOneTotal = IntStream.rangeClosed(1, 6).mapToObj(Integer::toString).map(this::valueForPit).reduce(0, Integer::sum);
        int playerTwoTotal = IntStream.rangeClosed(8, 13).mapToObj(Integer::toString).map(this::valueForPit).reduce(0, Integer::sum);
        // endgame: If the 'home row' of one of the players is empty, the game is over
        if (playerOneTotal == 0 || playerTwoTotal == 0) {
            // add the totals to each players kalah
            // null check to appease static checker, as values can not be null here
            state.compute(PLAYER_ONE_KALAH, (key, previous) -> (previous != null ? previous : 0) + playerOneTotal);
            state.compute(PLAYER_TWO_KALAH, (key, previous) -> (previous != null ? previous : 0) + playerTwoTotal);
        } else {
            return nextPlayer == Player.PLAYER_ONE ? GameResult.PLAYER_ONE_NEXT : GameResult.PLAYER_TWO_NEXT;
        }
        // clear the rows (but not the kalahs)
        IntStream.rangeClosed(1, 6).mapToObj(Integer::toString).forEach(e -> state.put(e, 0));
        IntStream.rangeClosed(8, 13).mapToObj(Integer::toString).forEach(e -> state.put(e, 0));
        int playerOneTotalWithKalah = playerOneTotal + state.get(PLAYER_ONE_KALAH);
        int playerTwoTotalWithKalah = playerTwoTotal + state.get(PLAYER_TWO_KALAH);

        // Determine winner:
        if (playerOneTotalWithKalah == playerTwoTotalWithKalah) {
            return GameResult.DRAW;
        }
        if (playerOneTotalWithKalah > playerTwoTotalWithKalah) {
            return GameResult.PLAYER_ONE_WON;
        } else {
            return GameResult.PLAYER_TWO_WON;
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", this.id);
        result.put("url", this.gameURL + "/" + id);
        Map<String, String> orderedHash = new LinkedHashMap<>();
        this.state
                .entrySet()
                .forEach(entry -> orderedHash.put(entry.getKey(), Integer.toString(entry.getValue())));
        result.put("status", orderedHash);
        return Collections.unmodifiableMap(result);
    }
}


