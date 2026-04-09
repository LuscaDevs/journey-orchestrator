package com.luscadevs.journeyorchestrator.config;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Test configuration for MongoDB Testcontainers. Creates a fresh MongoDB container for each test
 * run.
 */
@Testcontainers
public class MongoTestContainerConfig {

    @Container
    static MongoDBContainer mongoContainer =
            new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    static {
        mongoContainer.start();
    }

    public static String getMongoUri() {
        return mongoContainer.getReplicaSetUrl();
    }
}
