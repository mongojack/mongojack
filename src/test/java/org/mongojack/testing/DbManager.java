package org.mongojack.testing;

import org.testcontainers.containers.MongoDBContainer;

/**
 * @author Ben McCann (benmccann.com)
 */
public class DbManager {

    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongodb/mongodb-community-server:7.0-ubuntu2204");

    public static void startDb() {
        if (!mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
        }
    }

    public static String connectionString() {
        return mongoDBContainer.getConnectionString();
    }
}
