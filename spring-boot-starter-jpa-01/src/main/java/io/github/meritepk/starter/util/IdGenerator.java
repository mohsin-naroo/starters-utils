package io.github.meritepk.starter.util;

import java.net.InetAddress;
import java.security.SecureRandom;

import de.mkammerer.snowflakeid.SnowflakeIdGenerator;
import de.mkammerer.snowflakeid.options.Options;
import de.mkammerer.snowflakeid.structure.Structure;
import de.mkammerer.snowflakeid.time.MonotonicTimeSource;

public interface IdGenerator {

    int BITS_TIMESTAMP = 48;
    int BITS_GENERATOR = 5;
    int BITS_SEQUENCE = 10;

    SecureRandom RANDOM = new SecureRandom();

    SnowflakeIdGenerator GENERATOR = SnowflakeIdGenerator.createCustom(
            generatorId(BITS_GENERATOR),
            MonotonicTimeSource.createDefault(),
            new Structure(BITS_TIMESTAMP, BITS_GENERATOR, BITS_SEQUENCE),
            Options.createDefault());

    static int generatorId(int generatorBits) {
        String server = System.getProperty("server_id");
        if (server == null) {
            server = System.getenv("server_id");
        }
        int serverId;
        try {
            if (server == null) {
                server = String.valueOf(InetAddress.getLocalHost().getHostName().hashCode());
            }
            serverId = Integer.parseInt(server);
            if (serverId == 0) {
                serverId = RANDOM.nextInt();
            }
        } catch (Exception ex) {
            serverId = RANDOM.nextInt();
        }
        int maxServerId = (int) (Math.pow(2, generatorBits) - 1);
        serverId = serverId & maxServerId;
        return serverId;
    }

    static long nextId() {
        return GENERATOR.next();
    }
}
