package io.floodplain.kalah.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.floodplain.kalah.KalahGameState;
import io.quarkus.jackson.ObjectMapperCustomizer;

import javax.inject.Singleton;

@Singleton
public class KalahSerializationCustomizer implements ObjectMapperCustomizer {
    @Override
    public void customize(ObjectMapper objectMapper) {
        SimpleModule mod = new SimpleModule("Kalah Module");

        // Add the custom serializer to the module
        mod.addSerializer(new GameStateSerializer(KalahGameState.class));
        objectMapper.registerModule(mod);
    }
}
