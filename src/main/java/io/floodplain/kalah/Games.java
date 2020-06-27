package io.floodplain.kalah;

import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Path("/games")
public class Games {

    private static final Logger logger = LoggerFactory.getLogger(Games.class);
    public static final String GAMEURL = "http://localhost:8080/games/";
    // TODO move mutable state somewhere else
    private AtomicLong lastId = new AtomicLong(0);
    Map<String, KalahGameEngine> allStates = new HashMap<>();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response createGame() throws MalformedURLException {
        String nextGame = generateNewId();
        logger.info("game id: "+nextGame);
        KalahGameEngine state = new KalahGameEngine(nextGame,GAMEURL);
        allStates.put(nextGame, state);
        Map<String,String> result = new HashMap<>();
        result.put("id",nextGame);
        result.put("url",GAMEURL+nextGame);
        return Response.ok(result).status(201).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{gameId}/pits/{pitId}")
    public Response executeMove(@PathParam String gameId, @PathParam String pitId) {
        try {
            KalahGameEngine state = allStates.get(gameId);
            if(state==null) {
                return Response.status(400,"Unknown game").build();
            }
            KalahGameEngine.Player currentPlayer = state.nextPlayer();
            state.startMove(currentPlayer,pitId);
            return Response.ok(state.status()).build();
        } catch (Throwable e) {
            logger.error("Error performing move",e);
            return Response.status(400,"Invalid move").build();
        }
    }

    private String generateNewId() {
        return Long.toString(lastId.incrementAndGet());
    }
}