package ru.mail.polis.olerom.cluster;

import org.jetbrains.annotations.Nullable;

/**
 * Date: 27.10.17
 *
 * @author olerom
 */
public class BodyMessage extends BaseMessage {
    private final int visitedNodes;
    private final int deadNodes;

    @Nullable
    private final byte[] value;

    public BodyMessage(final int succeedNodes,
                       final int ack,
                       final int from,
                       final int visitedNodes,
                       final int deadNodes,
                       @Nullable final byte[] value) {
        super(succeedNodes, ack, from);
        this.visitedNodes = visitedNodes;
        this.deadNodes = deadNodes;
        this.value = value;
    }

    @Nullable
    public byte[] getValue() {
        return value;
    }

    @Override
    public int getResult() {
        if (succeedNodes >= ack + 1) {
            return OK;
        } else if (succeedNodes >= ack) {
            return OK_EXCEPT_CURRENT;
        } else if (succeedNodes < visitedNodes && visitedNodes - deadNodes >= ack) {
            return NOT_FOUND;
        } else {
            return NOT_ENOUGH_REPLICAS;
        }
    }
}
