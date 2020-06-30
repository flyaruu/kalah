package io.floodplain.kalah;

import io.floodplain.kalah.stores.KalahFireStore;
import io.vertx.core.http.HttpServerRequest;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Path("/games")
public class KalahEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(KalahEndpoint.class);
    public static final String URL = "url";
    public static final String ID = "id";
    public static final String STATUS = "status";
//    public static final String GAMEURL = "http://localhost:8080/games/";
    // TODO move mutable state somewhere else
//    private AtomicLong lastId = new AtomicLong(0);
//    Map<String, KalahGameEngine> allStates = new HashMap<>();

    @Inject
    KalahFireStore storageBackend;


    @Context
    UriInfo info;

    @Context
    HttpServerRequest request;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response createGame() throws IOException {
        String incomingUri = info.getRequestUri().toString();
        String nextGame = generateNewId();
        logger.info("game id: " + nextGame);
        KalahGameEngine state = new KalahGameEngine(nextGame, info.getRequestUri().toString());
        Map<String, Object> gameStatus = state.status();
        Map<String, Object> result = new HashMap<>();
        result.put(ID, nextGame);
        result.put(URL, incomingUri + "/" + nextGame);
        result.put(STATUS, gameStatus);
        storageBackend.save(nextGame, result);

        // Gamestate does not need to be returned
        result.remove(STATUS);
        return Response.ok(result).status(201).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{gameId}/pits/{pitId}")
    public Response executeMove(@PathParam String gameId, @PathParam String pitId) {
        if (gameId == null) {
            return Response.status(400, "gameId required").build();
        }
        if (pitId == null) {
            return Response.status(400, "pitId required").build();
        }
        try {
            Map<String, Object> persisted = storageBackend.load(gameId);
            if (persisted == null) {
                return Response.status(400, "Unknown game").build();
            }
            String requestURI = (String) persisted.get(URL);
            Map<String, Object> gameState = (Map<String, Object>) persisted.get(STATUS);
            KalahGameEngine state = new KalahGameEngine(gameId, requestURI, gameState);
            KalahGameEngine.Player currentPlayer = state.nextPlayer();
            state.startMove(currentPlayer, pitId);
            return Response.ok(state.status()).build();
        } catch (Throwable e) {
            logger.error("Error performing move", e);
            return Response.status(400, "Invalid move").build();
        }
    }

    private String generateNewId() throws IOException {
        return storageBackend.generateNextId();
//        return Long.toString(lastId.incrementAndGet());
    }
}