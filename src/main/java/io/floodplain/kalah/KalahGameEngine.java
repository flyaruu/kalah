package io.floodplain.kalah;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class KalahGameEngine {

    private static final Logger logger = LoggerFactory.getLogger(KalahGameEngine.class);
    public enum Player {
        PLAYER_ONE,PLAYER_TWO
    }
    public enum GameResult {
        PLAYER_ONE_NEXT,PLAYER_TWO_NEXT,PLAYER_ONE_WON,PLAYER_TWO_WON,DRAW
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

    // TODO could be configurable?
    public static final int PITS = 6;
    public static final int INITIAL_STONES = 6;
    public static final int TOTAL_PITS = PITS*2 + 2;

    public final String id;
    public final String gameURL;

    // Assume PLAYER_ONE goes first
    // Technically not really necessary to keep track of which player is up now (we could leave that to the clients)
    private Player nextPlayer = Player.PLAYER_ONE;
    // The pits and stones
    private final LinkedHashMap<String,Integer> state = new LinkedHashMap<>();

    // Effectively final, custom runs to use in tests will disable verifying the number of stones, allowing easier testing
    private boolean skipStoneChecks = false;

    public KalahGameEngine(String id, String gameURL, int[] playerOnePits, int[] playerTwoPits, int playerOneKalah, int playerTwoKalah) {
        this(id,gameURL);
        skipStoneChecks = true;
        int index = 1;
        for (int item: playerOnePits) {
            state.put(Integer.toString(index),item);
            index++;
        }
        index = 8;
        for (int item: playerTwoPits) {
            state.put(Integer.toString(index),item);
            index++;
        }
        state.put(PLAYER_ONE_KALAH,playerOneKalah);
        state.put(PLAYER_TWO_KALAH,playerTwoKalah);
    }

    public KalahGameEngine(String id, String gameURL) {
        this.id = id;
        this.gameURL = gameURL;
        pitSequence()
            .forEach(i->{
                // Kalah's are empty, all others get INITIAL_STONES stones
                int value = i.equals(PLAYER_ONE_KALAH) || i.equals(PLAYER_TWO_KALAH) ? 0 : INITIAL_STONES;
              state.put(i,value);
            });
    }

    public Integer valueForPit(String pit) {
        return state.get(pit);
    }

    public Stream<String> pitSequence() {
        return IntStream.rangeClosed(1,TOTAL_PITS)
                .mapToObj(e->Integer.toString(e));
    }

    public String describeState() {
        // create some sick ascii art
        // TODO maybe something prettier, don't want to add a templating dependency
        String template = "|  |b6|b5|b4|b3|b2|b1|  |\n"
                         +"|bk-------------------ak|\n"
                         +"|  |a1|a2|a3|a4|a5|a6|  |\n";
        template = template.replaceAll("bk",paddedValueForPit(PLAYER_TWO_KALAH));
        template = template.replaceAll("ak",paddedValueForPit(PLAYER_ONE_KALAH));
        template = template.replaceAll("a1",paddedValueForPit(PLAYER_ONE_1));
        template = template.replaceAll("a2",paddedValueForPit(PLAYER_ONE_2));
        template = template.replaceAll("a3",paddedValueForPit(PLAYER_ONE_3));
        template = template.replaceAll("a4",paddedValueForPit(PLAYER_ONE_4));
        template = template.replaceAll("a5",paddedValueForPit(PLAYER_ONE_5));
        template = template.replaceAll("a6",paddedValueForPit(PLAYER_ONE_6));
        template = template.replaceAll("b1",paddedValueForPit(PLAYER_TWO_1));
        template = template.replaceAll("b2",paddedValueForPit(PLAYER_TWO_2));
        template = template.replaceAll("b3",paddedValueForPit(PLAYER_TWO_3));
        template = template.replaceAll("b4",paddedValueForPit(PLAYER_TWO_4));
        template = template.replaceAll("b5",paddedValueForPit(PLAYER_TWO_5));
        template = template.replaceAll("b6",paddedValueForPit(PLAYER_TWO_6));
        return template;
   }

    private String paddedValueForPit(String pit) {
        return String.format("%2d", valueForPit(pit));
    }

    /**
     * Returns null when game is over, otherwise which player is up next
     * @param player
     * @param pit
     * @return
     * @throws IllegalMoveException
     */
    public GameResult startMove(Player player, String pit) throws IllegalMoveException {
        Integer stones = takeStonesFromPit(pit);
        if(stones==null) {
            throw new IllegalMoveException("Illegal move, no such pit: "+pit+" board state: "+describeState());
        }
        if(stones == 0) {
            // at least, I think that that's an illegal move
            throw new IllegalMoveException("Illegal move, can't move from pit: "+pit+" as there are no stones. state:"+describeState());
        }
        if(!player.equals(nextPlayer)) {
            throw new IllegalMoveException("Wrong player is moving. It is: "+nextPlayer+"'s turn");
        }
        // do you have to start on your own side? TODO, check

        state.put(pit,0);
        return distributeAfter(player,pit,stones);
    }

    Integer takeStonesFromPit(String pit) {
        int stones = state.get(pit);
        state.put(pit,0);
        return stones;
    }
    void addStonesToPit(String pit, int stones) {
        int originalStones = state.get(pit);
        state.put(pit,stones+originalStones);
    }

    private static String nextPit(Player currentPlayer, String pit) {
        System.err.println("next pit for player: "+currentPlayer+" and current pit: "+pit);
        int current = Integer.parseInt(pit);
        String next = Integer.toString((current % TOTAL_PITS) + 1);
        System.err.println("Next pit: "+next);
        // Use all pits, except the opponents kalah
        if(isKalahForOpponent(next,currentPlayer)) {
            System.err.println("" +
                    "Skipping opponents' kalah: "+next);
            return nextPit(currentPlayer,next);
        }
        return next;
    }

    private static Player opponent(Player player) {
        switch (player) {
            case PLAYER_ONE: return Player.PLAYER_TWO;
            case PLAYER_TWO: return Player.PLAYER_ONE;
        }
        throw new IllegalArgumentException("Invalid player type");
    }

    static String oppositePit(String pit) {
        if(isKalahForPlayer(pit,Player.PLAYER_ONE) || isKalahForPlayer(pit,Player.PLAYER_TWO)) {
            throw new IllegalArgumentException("Can't calculate opposite of kalah pits");
        }
        int pitNr = Integer.parseInt(pit);
        return Integer.toString(TOTAL_PITS-pitNr);
    }

    /**
     * Start a recursive call to distribute the stones
     * @param player
     * @param pit The pit we started at. This method will not distribute a stone to the current pit.
     * @param stonesToDistribute
     * @return
     */
    private GameResult distributeAfter(Player player, String pit, int stonesToDistribute) {
        int totalStones = state.values().stream().reduce(0,(a,b)->a+b);
        System.err.println("Distribute stones after pit: "+pit+" player: "+player+" remaining stones: "+stonesToDistribute+" stones on the board: "+totalStones);
        if(stonesToDistribute == 0) {
            int totalOriginal = PITS * INITIAL_STONES * 2;
            if(!skipStoneChecks && totalStones!=totalOriginal) {
                throw new IllegalStateException("Number of stones seems to have changed. I see: "+totalStones+" and it should be "+totalOriginal);
            }

            // check if you can take the opponent's stones
            if(!isKalah(pit) && valueForPit(pit)==1 && ownerOfPit(pit).equals(player)) {
                int oppositeStones = valueForPit(oppositePit(pit));
                if(oppositeStones>0 ) {
                    System.err.println("won stones: "+(oppositeStones+1));
                    int stones = takeStonesFromPit(pit) + takeStonesFromPit(oppositePit(pit));
                    addStonesToPit(kalahForPlayer(player),stones);
                }
                player.equals(ownerOfPit(pit));
                // we've just put this one in
                String opposite = oppositePit(pit);
            }
            // TODO

            // check if game over
            // TODO
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
            if(pit.equals(kalahForPlayer(player))) {
                // ended on the players kalah. Player can play again.
                nextPlayer = player;
                return (nextPlayer==Player.PLAYER_ONE ? GameResult.PLAYER_ONE_NEXT : GameResult.PLAYER_TWO_NEXT);
            } else {
                nextPlayer = opponent(player);
                return (nextPlayer==Player.PLAYER_ONE ? GameResult.PLAYER_ONE_NEXT : GameResult.PLAYER_TWO_NEXT);
            }
        }
        if(isKalahForOpponent(pit,player)) {
            return distributeAfter(player,nextPit(player,pit),stonesToDistribute);
        }
        String nextPit = nextPit(player,pit);
        addStonesToPit(nextPit,1);
//        int nextPitCount = state.get(nextPit);
//        state.put(nextPit,nextPitCount+1);
        return distributeAfter(player,nextPit,stonesToDistribute - 1);
    }

    static Player ownerOfPit(String pit) {
        if(Integer.parseInt(pit) <= 7) {
            return Player.PLAYER_ONE;
        } else {
            return Player.PLAYER_TWO;
        }
    }

    public Player nextPlayer() {
        return this.nextPlayer;
    }

    public static boolean isKalah(String pit) {
        return pit.equals(PLAYER_ONE_KALAH) || pit.equals(PLAYER_TWO_KALAH);
    }
    public static boolean isKalahForPlayer(String pit,Player player) {
        switch (player) {
            case PLAYER_ONE: return pit.equals(PLAYER_ONE_KALAH);
            case PLAYER_TWO: return pit.equals(PLAYER_TWO_KALAH);
        }
        throw new IllegalStateException("Illegal player");
    }

    private static boolean isKalahForOpponent(String pit,Player player) {
        if(player==Player.PLAYER_ONE) {
            return pit.equals(PLAYER_TWO_KALAH);
        } else if(player==Player.PLAYER_TWO) {
            return pit.equals(PLAYER_ONE_KALAH);
        }
        if(player == null) {
            throw new IllegalArgumentException("No player supplied");
        }
        throw new IllegalArgumentException("Should not happen");
    }

    GameResult winnerIsDecided() {
        int playerOneTotal = IntStream.rangeClosed(1,6).mapToObj(Integer::toString).map(this::valueForPit).reduce(0,(a,b)->a+b);
        int playerTwoTotal = IntStream.rangeClosed(8,13).mapToObj(Integer::toString).map(this::valueForPit).reduce(0,(a,b)->a+b);
        // endgame: If the 'home row' of one of the players is empty, the game is over
        if(playerOneTotal == 0 || playerTwoTotal == 0) {
            // add the totals to each players kalah
            state.compute(PLAYER_ONE_KALAH,(key,previous)->previous+playerOneTotal);
            state.compute(PLAYER_TWO_KALAH,(key,previous)->previous+playerTwoTotal);
        } else {
            return nextPlayer == Player.PLAYER_ONE? GameResult.PLAYER_ONE_NEXT : GameResult.PLAYER_TWO_NEXT;
        }
        // clear the board
        IntStream.rangeClosed(1,6).mapToObj(Integer::toString).forEach(e->state.put(e,0));
        IntStream.rangeClosed(8,13).mapToObj(Integer::toString).forEach(e->state.put(e,0));
        int playerOneTotalWithKalah = playerOneTotal + state.get(PLAYER_ONE_KALAH);
        int playerTwoTotalWithKalah = playerTwoTotal + state.get(PLAYER_TWO_KALAH);

        if(playerOneTotalWithKalah == playerTwoTotalWithKalah) {
            return GameResult.DRAW;
        }
        if(playerOneTotalWithKalah > playerTwoTotalWithKalah) {
            return GameResult.PLAYER_ONE_WON;
        } else {
            return GameResult.PLAYER_TWO_WON;
        }
    }

    public static String kalahForPlayer(Player player) {
        switch (player) {
            case PLAYER_ONE:
                return PLAYER_ONE_KALAH;
            case PLAYER_TWO:
                return PLAYER_TWO_KALAH;
        }
        throw new IllegalArgumentException("Bad player? "+player);
    }

    public Map<String,Object> status() {
        Map<String,Object> result = new HashMap<>();
        result.put("id",this.id);
        result.put("url",this.gameURL.toString()+"/"+id);
        Map<String,String> orderedHash = new LinkedHashMap<>();
        this.state
                 .entrySet()
                 .stream()
                .forEach(entry->orderedHash.put(entry.getKey(),Integer.toString(entry.getValue())));
//                 .collect(Collectors.toUnmodifiableMap(entry->entry.getKey(),
//                         entry->Integer.toString(entry.getValue())));
        result.put("status",orderedHash);
        return Collections.unmodifiableMap(result);
    }
}


