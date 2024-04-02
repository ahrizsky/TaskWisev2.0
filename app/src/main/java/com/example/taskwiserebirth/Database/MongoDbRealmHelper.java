package com.example.taskwiserebirth.Database;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import org.bson.Document;

public class MongoDbRealmHelper {

    private static final String SERVICE_NAME = "mongodb-atlas";
    private static final String DATABASE_NAME = "TaskWise";
    private static final String APP_ID = "taskwise-bxyah";
    private static App app;

    public static App initializeRealmApp() {
        if (app == null) {
            app = new App(new AppConfiguration.Builder(APP_ID).build());
        }
        return app;
    }

    public static App getRealmApp() {
        if (app == null) {
            throw new IllegalStateException("Realm app is not initialized. Call initializeRealmApp() first.");
        }
        return app;
    }

    public static MongoClient getMongoClient() {
        App app = getRealmApp();
        return app.currentUser().getMongoClient(SERVICE_NAME);
    }

    public static MongoDatabase getMongoDatabase() {
        MongoClient mongoClient = getMongoClient();
        return mongoClient.getDatabase(DATABASE_NAME);
    }

    public static MongoCollection<Document> getMongoCollection(String collectionName) {
        MongoDatabase mongoDatabase = getMongoDatabase();
        return mongoDatabase.getCollection(collectionName);
    }
}
