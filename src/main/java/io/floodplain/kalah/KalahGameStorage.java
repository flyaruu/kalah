package io.floodplain.kalah;

import java.io.IOException;
import java.util.Map;

public interface KalahGameStorage {
    /**
     * Persist the current game
     *
     * @param id       The game id
     * @param contents The game context
     * @throws IOException when there is a problem persisting
     */
    void save(String id, Map<String, Object> contents) throws IOException;

    /**
     * Retrieve a saved game from storage
     *
     * @param id the id the game is known under
     * @return The map that contains all game data
     */
    Map<String, Object> load(String id) throws IOException;

    /**
     * Generate a new id for the next game
     *
     * @return A new, unused id
     * @throws IOException When something went wrong
     */
    String generateNextId() throws IOException;
}
