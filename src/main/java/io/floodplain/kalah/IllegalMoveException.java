package io.floodplain.kalah;

/**
 * The game engine throws this exception when you try to make an illegal move.
 */
public class IllegalMoveException extends Exception {

    public IllegalMoveException(String message) {
        super(message);
    }

    public IllegalMoveException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalMoveException(Throwable cause) {
        super(cause);
    }
}
