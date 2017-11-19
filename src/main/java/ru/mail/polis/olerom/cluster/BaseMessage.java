package ru.mail.polis.olerom.cluster;

/**
 * Date: 27.10.17
 *
 * @author olerom
 */
public class BaseMessage {

    public final static int OK = 0;
    public final static int OK_EXCEPT_CURRENT = 1;
    public final static int NOT_FOUND = 2;
    public final static int NOT_ENOUGH_REPLICAS = 3;

    protected final int succeedNodes;
    protected final int ack;
    protected final int from;

    public BaseMessage(final int succeedNodes,
                       final int ack,
                       final int from) {
        this.succeedNodes = succeedNodes;
        this.ack = ack;
        this.from = from;
    }

    public int getResult() {
        if (succeedNodes >= ack) {
            return OK;
        } else {
            return NOT_ENOUGH_REPLICAS;
        }
    }



}
