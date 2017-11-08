package ru.mail.polis.olerom.cluster;

import one.nio.http.HttpClient;
import one.nio.http.Response;
import one.nio.net.ConnectionString;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Date: 20.10.17
 *
 * @author olerom
 */
public class NodeManager {
    @NotNull
    private final static String METHOD_AND_DOMAIN = "http://localhost:";
    @NotNull
    private final static String INTERCONNECTION_URI = "/v0/interconnection?id=";

    private final static int OK_STATUS = 200;

    @NotNull
    private final Topology topology;

    private final int port;
    private int ack;
    private int from;

    public NodeManager(@NotNull final Topology topology,
                       final int port) {
        this.topology = topology;
        this.port = port;
    }

    public void setAck(final int ack) {
        this.ack = ack - 1;
    }

    public void setFrom(final int from) {
        this.from = from - 1;
    }

    @NotNull
    public BodyMessage handleGet(@NotNull final String id) {
        int deadNodes = 0;
        int visitedNodes = 0;
        int respondedNodes = 0;
        byte[] responseBody = null;
        if (from == 0){
            return new BodyMessage(respondedNodes, ack, from, visitedNodes, deadNodes, responseBody);
        }
        for (String hostAndPort : topology.getNodes()) {
            if (hostAndPort.equals(METHOD_AND_DOMAIN + port)) {
                continue;
            }

            visitedNodes++;

            final HttpClient client = new HttpClient(new ConnectionString(hostAndPort));

            try {
                Response getResponse = client.get(INTERCONNECTION_URI + id);
                if (getResponse.getStatus() == OK_STATUS) {
                    respondedNodes++;
                    if (responseBody == null) {
                        responseBody = getResponse.getBody();
                    }
                    if (Arrays.equals(getResponse.getBody(), Response.EMPTY)) {
                        responseBody = getResponse.getBody();
                    }
                }
            } catch (Exception e) {
                deadNodes++;
                e.printStackTrace();
            } finally {
                client.close();
            }
        }

        return new BodyMessage(respondedNodes, ack, from, visitedNodes, deadNodes, responseBody);
    }

    @NotNull
    public BaseMessage handlePut(@NotNull final String id,
                                 @NotNull final byte[] value) {
        int putNodes = 0;
        int deadNodes = 0;
        int visitedNodes = 0;

        for (String hostAndPort : topology.getNodes()) {
            if (hostAndPort.equals(METHOD_AND_DOMAIN + port)) {
                continue;
            }

            if (visitedNodes - deadNodes >= from) {
                break;
            }

            visitedNodes++;

            final HttpClient client = new HttpClient(new ConnectionString(hostAndPort));

            try {
                Response put = client.put(INTERCONNECTION_URI + id, value);
                if (put.getStatus() == OK_STATUS) {
                    putNodes++;
                }
            } catch (Exception e) {
                deadNodes++;
                e.printStackTrace();
            } finally {
                client.close();
            }
        }

        return new BaseMessage(putNodes, ack, from);
    }

    @NotNull
    public BaseMessage handleDelete(@NotNull final String id) {
        int deletedNodes = 0;
        for (String hostAndPort : topology.getNodes()) {
            if (hostAndPort.equals(METHOD_AND_DOMAIN + port)) {
                continue;
            }

            final HttpClient client = new HttpClient(new ConnectionString(hostAndPort));

            try {
                Response delete = client.delete(INTERCONNECTION_URI + id);
                if (delete.getStatus() == OK_STATUS) {
                    deletedNodes++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                client.close();
            }
        }

        return new BaseMessage(deletedNodes, ack, from);
    }
}

