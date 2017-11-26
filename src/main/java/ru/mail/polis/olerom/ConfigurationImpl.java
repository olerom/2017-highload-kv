package ru.mail.polis.olerom;

import one.nio.config.ConfigParser;
import one.nio.server.ServerConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Date: 27.10.17
 *
 * @author olerom
 */
public class ConfigurationImpl implements Configuration {
    @NotNull
    private final static String CONF = "\n" +
            "selectors: 4\n" +
            "acceptors:\n" +
            " - port: {PORT}\n";

    final private int port;

    public ConfigurationImpl(final int port) {
        this.port = port;
    }

    @NotNull
    @Override
    public ServerConfig getServerConfig() {
        return ConfigParser.parse(CONF.replace("{PORT}", port + ""), ServerConfig.class);
    }

    @Override
    public int getPort() {
        return this.port;
    }
}
