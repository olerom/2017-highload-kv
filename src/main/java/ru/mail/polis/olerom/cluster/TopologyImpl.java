package ru.mail.polis.olerom.cluster;

import java.util.Set;

/**
 * Date: 27.10.17
 *
 * @author olerom
 */
public class TopologyImpl implements Topology {

    private final Set<String> nodes;

    public TopologyImpl(Set<String> nodes) {
        this.nodes = nodes;
    }

    @Override
    public int getQuorum() {
        return this.nodes.size() / 2 + 1;
    }

    @Override
    public int getSize() {
        return this.nodes.size();
    }

    @Override
    public Set<String> getNodes() {
        return this.nodes;
    }
}
