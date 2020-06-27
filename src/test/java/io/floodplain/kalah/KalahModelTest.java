package io.floodplain.kalah;

import io.floodplain.kalah.KalahGameEngine.GameResult;
import io.floodplain.kalah.KalahGameEngine.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;

import static io.floodplain.kalah.KalahGameEngine.GameResult.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KalahModelTest {

    private static final Logger logger = LoggerFactory.getLogger(KalahGameEngine.class);

    private KalahGameEngine initialState = null;
    @BeforeEach
    public void setup() throws MalformedURLException {
        initialState = new KalahGameEngine("123", "http://somehost/");
    }

    @Test
    public void testKalahPit() throws MalformedURLException {
//        KalahGameState initialState = new KalahGameState("123", new URL("http://bla"));
        assertEquals(KalahGameEngine.PLAYER_ONE_KALAH, KalahGameEngine.kalahForPlayer(Player.PLAYER_ONE));
        assertEquals(KalahGameEngine.PLAYER_TWO_KALAH, KalahGameEngine.kalahForPlayer(Player.PLAYER_TWO));
        KalahGameEngine.isKalahForPlayer(KalahGameEngine.PLAYER_ONE_KALAH,Player.PLAYER_ONE);
        KalahGameEngine.isKalahForPlayer(KalahGameEngine.PLAYER_TWO_KALAH,Player.PLAYER_TWO);
    }

    @Test
    public void testModelInitial() throws MalformedURLException {
//        KalahGameState initialState = new KalahGameState("123", new URL("http://bla"));
        assertEquals("123", initialState.id);
        assertEquals("http://somehost/", initialState.gameURL);
        initialState.pitSequence()
                .filter(pit -> !(KalahGameEngine.isKalahForPlayer(pit,Player.PLAYER_ONE)))
                .filter(pit -> !(KalahGameEngine.isKalahForPlayer(pit,Player.PLAYER_TWO)))
                .forEach(pit -> assertEquals(6, initialState.valueForPit(pit)));
        String playerOneKalah = KalahGameEngine.kalahForPlayer(Player.PLAYER_ONE);
        assertEquals(0, initialState.valueForPit(playerOneKalah));
        String playerTwoKalah = KalahGameEngine.kalahForPlayer(Player.PLAYER_ONE);
        assertEquals(0, initialState.valueForPit(playerTwoKalah));
        assertEquals(14L, initialState.pitSequence().count());
    }

    @Test
    public void testModelSimpleMove() throws IllegalMoveException {
        Player upNext = initialState.nextPlayer();
        assertEquals(Player.PLAYER_ONE, upNext);
        System.err.println (initialState.describeState());
        GameResult next = initialState.startMove(initialState.nextPlayer(),"1");
        System.err.println (initialState.describeState());
        // check if the stones have been removed from the initial pit
        assertEquals(0,initialState.valueForPit("1"));
        // did end in the kalah, so player 1 to is up again:
        assertEquals(PLAYER_ONE_NEXT,next);
        next = initialState.startMove(initialState.nextPlayer(),"2");
        System.err.println (initialState.describeState());
        // did end in the kalah, so player 2 is up now:
        assertEquals(PLAYER_TWO_NEXT,next);
        next = initialState.startMove(Player.PLAYER_TWO,"11");
        System.err.println (initialState.describeState());
        assertEquals(PLAYER_ONE_NEXT,next);
        // player two kalah should be 1
        assertEquals(1,initialState.valueForPit(KalahGameEngine.PLAYER_TWO_KALAH));
        next = initialState.startMove(Player.PLAYER_ONE,"6");
        System.err.println (initialState.describeState());
        // assert that the kalah of player two was skipped, so still one
        assertEquals(1,initialState.valueForPit(KalahGameEngine.PLAYER_TWO_KALAH));
    }

    @Test
    public void testNumberOfStones() throws IllegalMoveException {
        Player upNext = initialState.nextPlayer();
        assertEquals(Player.PLAYER_ONE, upNext);
        GameResult next = initialState.startMove(initialState.nextPlayer(), "1");
        System.err.println(initialState.describeState());
    }

    @Test
    public void testOwnerOfPit() throws IllegalMoveException {
        assertEquals(Player.PLAYER_ONE, KalahGameEngine.ownerOfPit("3"));
        assertEquals(Player.PLAYER_TWO, KalahGameEngine.ownerOfPit("12"));
        // TODO
    }

    @Test
    public void testCustomModelResult() throws MalformedURLException {
        KalahGameEngine state = new KalahGameEngine("123","http://somehost/",new int[]{0,0,0,0,0,0},new int[]{0,0,0,0,0,0},0,10);
        GameResult result = state.winnerIsDecided();
        System.err.println("Result: "+result);
        assertEquals(PLAYER_TWO_WON,result);
    }

    @Test
    public void testLastMove() throws MalformedURLException, IllegalMoveException {
        KalahGameEngine state = new KalahGameEngine("123", "http://somehost/",new int[]{0,0,0,0,0,1},new int[]{0,0,0,0,0,0},0,0);
        System.err.println(state.describeState());
        GameResult result =  state.startMove(Player.PLAYER_ONE,"6");
        System.err.println(state.describeState());
        System.err.println("Result: "+result);
        assertEquals(PLAYER_ONE_WON,result);
    }
    @Test
    public void testCustomDraw() throws MalformedURLException {
        KalahGameEngine state = new KalahGameEngine("123", "http://somehost/",new int[]{0,0,0,0,0,0},new int[]{0,0,0,0,0,0},10,10);
        GameResult result = state.winnerIsDecided();
        System.err.println("Result: "+result);
        assertEquals(DRAW,result);
    }
    @Test
    public void testInProgress() throws MalformedURLException {
        KalahGameEngine state = new KalahGameEngine("123", "http://somehost/",new int[]{0,0,3,0,0,0},new int[]{0,0,0,5,0,0},0,10);
        GameResult result = state.winnerIsDecided();
        System.err.println("Result: "+result);
        assertEquals(PLAYER_ONE_NEXT,result);
    }

    @Test
    public void testTakeOpponentsStones() throws MalformedURLException, IllegalMoveException {
        KalahGameEngine state = new KalahGameEngine("123","http://somehost/",new int[]{0,0,1,0,0,0},new int[]{0,0,5,0,0,0},0,10);
        System.err.println(state.describeState());
        state.startMove(Player.PLAYER_ONE,"3");
        System.err.println(state.describeState());
    }
    @Test
    public void testCheckOpposite() throws MalformedURLException, IllegalMoveException {
        assertEquals("1", KalahGameEngine.oppositePit("13"));
        assertEquals("8", KalahGameEngine.oppositePit("6"));
        assertEquals("13", KalahGameEngine.oppositePit("1"));
        assertEquals("6", KalahGameEngine.oppositePit("8"));
    }
}