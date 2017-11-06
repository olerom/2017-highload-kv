package ru.mail.polis.olerom.cluster;

import one.nio.http.Response;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Date: 27.10.17
 *
 * @author olerom
 */
public class BodyMessage extends BaseMessage {
    public final static int OK_EXCEPT_CURRENT_NULL = 4;
    public final static int OK_EXCEPT_CURRENT_EMPTY = 5;

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
            if (value == null) {
                return OK_EXCEPT_CURRENT_NULL;
            } else if (Arrays.equals(value, Response.EMPTY)) {
                return OK_EXCEPT_CURRENT_EMPTY;
            } else {
                return OK_EXCEPT_CURRENT;
            }
        } else if (succeedNodes < visitedNodes && visitedNodes - deadNodes >= ack) {
            return NOT_FOUND;
        } else {
            return NOT_ENOUGH_REPLICAS;
        }
    }
}
