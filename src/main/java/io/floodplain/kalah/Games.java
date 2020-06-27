package io.floodplain.kalah;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Path("/games")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Games {

    private static final Logger logger = LoggerFactory.getLogger(Games.class);
    // TODO move mutable state somewhere else
    private AtomicLong lastId = new AtomicLong(0);
    Map<String,KalahGameState> allStates = new HashMap<>();
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGame() throws MalformedURLException {
        String nextGame = generateNewId();
        logger.info("game id: "+nextGame);
        KalahGameState state = new KalahGameState(nextGame,new URL("http://localhost:8080/games"));
        allStates.put(nextGame, state);
        return Response.ok(state).status(201).build();
    }

    private String generateNewId() {
        return Long.toString(lastId.incrementAndGet());
    }
}