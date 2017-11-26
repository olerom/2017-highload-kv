package ru.mail.polis.olerom.cluster;

import one.nio.http.HttpClient;
import one.nio.http.HttpException;
import one.nio.http.Request;
import one.nio.http.Response;
import one.nio.net.ConnectionString;
import one.nio.net.Socket;
import one.nio.pool.PoolException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

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

    @NotNull
    private final HashMap<String, HttpClient> client;

    private final int port;

    public NodeManager(@NotNull final Topology topology,
                       final int port) {
        this.topology = topology;
        this.port = port;
        this.client = new HashMap<>(topology.getSize());
        topology.getNodes().forEach((node) -> {
            if (!node.equals(METHOD_AND_DOMAIN + port)) {
                final ConnectionString connectionString = new ConnectionString(node);
                this.client.put(node, new HttpClient(connectionString));
            }
        });
    }

    @NotNull
    public BodyMessage handleGet(@NotNull final String id,
                                 final int ack,
                                 final int from) {
        int deadNodes = 0;
        int visitedNodes = 0;
        int respondedNodes = 0;
        byte[] responseBody = null;
        if (from == 0) {
            return new BodyMessage(respondedNodes, ack, from, visitedNodes, deadNodes, responseBody);
        }
        for (String hostAndPort : topology.getNodes()) {
            if (hostAndPort.equals(METHOD_AND_DOMAIN + port)) {
                continue;
            }

            visitedNodes++;

            try {
                Response getResponse = client.get(hostAndPort).get(INTERCONNECTION_URI + id);
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
            }
        }

        return new BodyMessage(respondedNodes, ack, from, visitedNodes, deadNodes, responseBody);
    }

    @NotNull
    public BaseMessage handlePut(@NotNull final String id,
                                 @NotNull final byte[] value,
                                 final int ack,
                                 final int from) {
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

            try {
                Response put = client.get(hostAndPort).put(INTERCONNECTION_URI + id, value);
                if (put.getStatus() == OK_STATUS) {
                    putNodes++;
                }
            } catch (Exception e) {
                deadNodes++;
                e.printStackTrace();
            }
        }

        return new BaseMessage(putNodes, ack, from);
    }

    @NotNull
    public BaseMessage handleDelete(@NotNull final String id,
                                    final int ack,
                                    final int from) {
        int deletedNodes = 0;
        for (String hostAndPort : topology.getNodes()) {
            if (hostAndPort.equals(METHOD_AND_DOMAIN + port)) {
                continue;
            }

            try {
                Response delete = client.get(hostAndPort).delete(INTERCONNECTION_URI + id);
                if (delete.getStatus() == OK_STATUS) {
                    deletedNodes++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new BaseMessage(deletedNodes, ack, from);
    }
}

