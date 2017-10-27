package ru.mail.polis.olerom.cluster;

import one.nio.http.Response;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Date: 27.10.17
 *
 * @author olerom
 */
public class BodyMessage extends BaseMessage {

    @Nullable
    private final byte[] value;

    public BodyMessage(final int succeedNodes,
                       final int visitedNodes,
                       final int deadNodes,
                       final int ack,
                       final int from,
                       @Nullable final byte[] value) {
        super(succeedNodes, visitedNodes, deadNodes, ack, from);
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
