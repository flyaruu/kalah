package io.floodplain.kalah.stores;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import io.floodplain.kalah.KalahGameStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Singleton
public class KalahFireStore implements KalahGameStorage {

    private static final Logger logger = LoggerFactory.getLogger(KalahFireStore.class);

    private final Firestore db;
    private final KalahGameStorage fallback;

    public KalahFireStore() throws IOException {
        // TODO remove, security issue
        logger.info("Environment: " + System.getenv());
        if (System.getenv("GOOGLE_APPLICATION_CREDENTIALS") != null) {
            logger.warn("GCP environment detected, using FireStore");
            FirestoreOptions firestoreOptions =
                    FirestoreOptions.getDefaultInstance().toBuilder()
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .build();
            db = firestoreOptions.getService();
            fallback = null;
        } else {
            logger.warn("No GCP environment detected, using in-memory");
            db = null;
            fallback = new KalahInMemoryStore();
        }
    }

    @Override
    public void save(String id, Map<String, Object> contents) throws IOException {
        if (db != null) {
            DocumentReference docRef = db.collection("games").document(id);
            ApiFuture<WriteResult> result = docRef.set(contents);
            try {
                logger.info("Update time : " + result.get().getUpdateTime());
            } catch (InterruptedException | ExecutionException e) {
                throw new IOException("Error saving data", e);
            }
        } else {
            fallback.save(id, contents);
        }
    }

    @Override
    public Map<String, Object> load(String id) throws IOException {
        if (db != null) {
            DocumentReference docRef = db.collection("games").document(id);
            try {
                return docRef.get().get().getData();
            } catch (InterruptedException|ExecutionException e) {
                throw new IOException("Error querying game state",e);
            }
        } else {
            return fallback.load(id);
        }
    }

    @Override
    public String generateNextId() throws IOException {
        if (db != null) {
            DocumentReference docRef = db.collection("lastgame").document("lastid");
            Map<String, Object> newDoc = new HashMap<>();
            newDoc.put("id", FieldValue.increment(1L));
            try {
                docRef.update(newDoc).get();
                docRef = db.collection("lastgame").document("lastid");
                Long counter = (Long) docRef.get().get().get(FieldPath.of("id"));
                if(counter==null) {
                    throw new IOException("Error reading lastgame collection. Has the FireStore database been initialized properly?");
                }
                return Long.toString(counter);
            } catch (InterruptedException | ExecutionException e) {
                throw new IOException("Error creating new game id", e);
            }
        } else {
            logger.warn("Generating id locally, firestore is unavailable");
            return fallback.generateNextId();
        }
    }
}

