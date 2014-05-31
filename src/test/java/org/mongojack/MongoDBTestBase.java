/*
 * Copyright 2011 VZ Netzwerke Ltd
 * Copyright 2014 devbliss GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mongojack;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * Base class for unit tests that run against MongoDB. Will start a mongodb
 * database on localhost, and that we can do whatever we want to a database
 * called "unittest".
 */
@RunWith(MongoDBTestCaseRunner.class)
public abstract class MongoDBTestBase {
  private static final Random rand = new Random();

  private boolean forceDefaultPort = false;

  private boolean useStreamParser = true;
  private boolean useStreamSerialiser = false;

  protected Mongo mongo;
  protected DB db;
  private Set<String> collections;

  protected MongodExecutable mongoExec = null;
  protected MongodProcess mongod = null;
  protected MongoClient mongoClient = null;
  IMongodConfig mongodConfig = null;

  /**
   * 
   * Starts a mongodb instance and returns a working client to it.
   * 
   * @return the client that can connect to the working instance
   * 
   * @throws UnknownHostException
   * @throws IOException
   */
  protected MongoClient startMongo() throws UnknownHostException, IOException {

    // start embedded mongo

    IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
        .defaultsWithLogger(Command.MongoD,
            Logger.getLogger(getClass().getName())).build();

    MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);

    // NOTICE could also set the version of MonogDB to test against
    MongodConfigBuilder configBuilder = new MongodConfigBuilder()
        .version(Version.Main.PRODUCTION);

    // for testing we usually dont care what port we are goint to use but
    // just in case
    if (forceDefaultPort) {
      configBuilder.net(new Net(27017, Network.localhostIsIPv6()));
    }

    mongodConfig = configBuilder.build();

    mongoExec = runtime.prepare(mongodConfig);
    mongod = mongoExec.start();

    // setup the client for the test from the same config as used for the
    // test server
    ServerAddress addr = new ServerAddress(mongodConfig.net()
        .getServerAddress(), mongodConfig.net().getPort());
    mongoClient = new MongoClient(addr);

    return this.mongoClient;

  }

  public MongoClient getMongoClient() throws UnknownHostException {
    if (!isRuning()) {
      throw new RuntimeException("Embedded MongoDB is not running.");
    } else {
      return new MongoClient(new ServerAddress(mongodConfig.net()
          .getServerAddress(), mongodConfig.net().getPort()));
    }
  }

  protected void shutdownMongo() {

    if (mongod != null)
      mongod.stop();

    if (mongoExec != null)
      mongoExec.stop();

  }

  public boolean isRuning() {

    if (mongoExec == null || mongod == null | mongodConfig == null)
      return false;
    else
      return true;

  }

  public String getMongoHost() throws UnknownHostException {
    if (this.mongodConfig == null) {
      throw new RuntimeException("Embedded MongoDB is not configured.");
    } else {
      return this.mongodConfig.net().getServerAddress().getHostName();
    }
  }

  public int getMongoPort() {
    if (this.mongodConfig == null) {
      throw new RuntimeException("Embedded MongoDB is not configured.");
    } else {
      return this.mongodConfig.net().getPort();
    }
  }

  /**
   * @return true if the mongod process is required to listen on port 27017
   */
  public boolean isForceDefaultPort() {
    return forceDefaultPort;
  }

  /**
   * @param force
   *            is set to true the test will run on the default monogd port or
   *            die if there is already another mongo instance bound to this
   *            port
   */
  public void setForceDefaultPort(boolean force) {
    this.forceDefaultPort = force;
  }

  @Before
  public void connectToDb() throws Exception {
    mongo = startMongo();
    db = mongo.getDB("unittest");
    collections = new HashSet<String>();
  }

  @After
  public void disconnectFromDb() throws Exception {
    for (String collection : collections) {
      db.getCollection(collection).drop();
    }
    mongo.close();
    shutdownMongo();
  }

  /**
   * Get a collection with the given name, and store it, so that it will be
   * dropped in clean up
   * 
   * @param name
   *            The name of the collection
   * @return The collection
   */
  protected DBCollection getCollection(String name) {
    collections.add(name);
    return db.getCollection(name);
  }

  /**
   * Get a collection with a random name. Should grant some degree of
   * isolation from tests running in parallel.
   * 
   * @return The collection
   */
  protected DBCollection getCollection() {
    StringBuilder name = new StringBuilder();
    while (name.length() < 8) {
      char letter = (char) rand.nextInt(26);
      if (rand.nextBoolean()) {
        letter += 'a';
      } else {
        letter += 'A';
      }
      name.append(letter);
    }
    return getCollection(name.toString());
  }

  protected <T, K> JacksonDBCollection<T, K> configure(
      JacksonDBCollection<T, K> collection) {
    if (useStreamParser) {
      collection
          .enable(JacksonDBCollection.Feature.USE_STREAM_DESERIALIZATION);
    } else {
      collection
          .disable(JacksonDBCollection.Feature.USE_STREAM_DESERIALIZATION);
    }
    if (useStreamSerialiser) {
      collection
          .enable(JacksonDBCollection.Feature.USE_STREAM_SERIALIZATION);
    } else {
      collection
          .disable(JacksonDBCollection.Feature.USE_STREAM_SERIALIZATION);
    }
    return collection;
  }

  protected <T, K> JacksonDBCollection<T, K> getCollection(Class<T> type,
      Class<K> keyType) {
    return configure(JacksonDBCollection.wrap(getCollection(), type,
        keyType));
  }

  protected <T, K> JacksonDBCollection<T, K> getCollection(Class<T> type,
      Class<K> keyType, Class<?> view) {
    return configure(JacksonDBCollection.wrap(getCollection(), type,
        keyType, view));
  }

  protected <T, K> JacksonDBCollection<T, K> getCollection(Class<T> type,
      Class<K> keyType, String collectionName) {
    return configure(JacksonDBCollection.wrap(
        getCollection(collectionName), type, keyType));
  }

  protected <T, K> JacksonDBCollection<T, K> getCollection(Class<T> type,
      Class<K> keyType, ObjectMapper mapper) {
    return configure(JacksonDBCollection.wrap(getCollection(), type,
        keyType, mapper));
  }

  public void setUseStreamParser(boolean useStreamParser) {
    this.useStreamParser = useStreamParser;
  }

    public void setUseStreamSerialiser(boolean useStreamSerialiser) {
        this.useStreamSerialiser = useStreamSerialiser;
    }
}
