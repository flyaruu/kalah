package io.floodplain.kalah.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.floodplain.kalah.KalahGameState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GameStateSerializer extends StdSerializer<KalahGameState> {

    private static final Logger logger = LoggerFactory.getLogger(GameStateSerializer.class);

    public GameStateSerializer() {
        this(null);
    }
    public GameStateSerializer(Class<KalahGameState> clz) {
        super(clz);
        logger.info("Creating serializer");
    }

    @Override
    public void serialize(KalahGameState gameState, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        logger.info("Serializing game state");
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("id", gameState.id);
        jsonGenerator.writeStringField("url", gameState.gameURL.toString());
        jsonGenerator.writeObjectFieldStart("status");
        // use for loop, forEach does not work well with checked exceptions
        for (String pit: (Iterable<String>)gameState.pitSequence()::iterator) {
            jsonGenerator.writeStringField(pit, Integer.toString(gameState.valueForPit(pit)));
        }
        jsonGenerator.writeEndObject();
    }
}
