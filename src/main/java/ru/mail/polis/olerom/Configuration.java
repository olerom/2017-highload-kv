package ru.mail.polis.olerom;

import one.nio.server.ServerConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Date: 27.10.17
 *
 * @author olerom
 */
public interface Configuration {
    @NotNull
    ServerConfig getServerConfig();

    int getPort();
}
