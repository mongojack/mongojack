package org.mongojack.testing;

import java.io.IOException;
import java.net.UnknownHostException;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * @author Ben McCann (benmccann.com)
 */
public class DbManager {

  public static int PORT = 12345;

  private static MongodProcess mongod = null;

  public static void startDb() {
    MongodStarter starter = MongodStarter.getDefaultInstance();

    try {
        IMongodConfig mongodConfig = new MongodConfigBuilder()
            .version(Version.Main.V2_4)
            .net(new Net(PORT, Network.localhostIsIPv6()))
            .build();

        MongodExecutable mongodExecutable = starter.prepare(mongodConfig);
        mongod = mongodExecutable.start();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static void stopDb() {
    if (mongod != null) {
      mongod.stop();
    }
  }

}
