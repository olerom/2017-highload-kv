package ru.mail.polis.olerom.cluster;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Date: 20.10.17
 *
 * @author olerom
 */
public class NodeManager {
    private final int ack;
    private final int from;
    private final int port;


    @NotNull
    private final Set<String> topology;

    public NodeManager(final int ack,
                       final int from,
                       final int port,
                       @NotNull final Set<String> topology) {
        this.ack = ack;
        this.from = from;
        this.port = port;
        this.topology = topology;
    }


    public void handleGet() {

    }
}

