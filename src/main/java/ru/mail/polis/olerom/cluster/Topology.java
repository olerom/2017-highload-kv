package ru.mail.polis.olerom.cluster;

import java.util.Set;

/**
 * Date: 27.10.17
 *
 * @author olerom
 */
public interface Topology {
    int getQuorum();

    int getSize();

    Set<String> getNodes();
}
