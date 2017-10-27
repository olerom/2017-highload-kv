package ru.mail.polis.olerom.cluster;

import one.nio.http.HttpClient;
import one.nio.http.Response;
import one.nio.net.ConnectionString;
import org.jetbrains.annotations.NotNull;

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
        int deaths = 0;
        int visited = 0;
        int satisfied = 0;
        byte[] response = null;
        for (String hostAndPort : topology.getNodes()) {
            if (hostAndPort.equals(METHOD_AND_DOMAIN + port)) {
                continue;
            }

            visited++;
            final HttpClient client = new HttpClient(new ConnectionString(hostAndPort));
            try {
                Response get = client.get(INTERCONNECTION_URI + id);
                if (get.getStatus() == OK_STATUS) {
                    satisfied++;
                    response = get.getBody();
                }
            } catch (Exception e) {
                deaths++;
                e.printStackTrace();
            } finally {
                client.close();
            }
        }


        return new BodyMessage(satisfied, visited, deaths, ack, from, response);
    }

    @NotNull
    public BaseMessage handlePut(@NotNull final String id,
                                 @NotNull final byte[] value) {
        int deaths = 0;
        int visited = 0;
        int satisfied = 0;
        for (String hostAndPort : topology.getNodes()) {
            if (hostAndPort.equals(METHOD_AND_DOMAIN + port)) {
                continue;
            }

            visited++;
            HttpClient client = new HttpClient(new ConnectionString(hostAndPort));

            try {
                Response put = client.put(INTERCONNECTION_URI + id, value);
                if (put.getStatus() == OK_STATUS) {
                    satisfied++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                client.close();
            }
        }

        return new BaseMessage(satisfied, visited, deaths, ack, from);
    }

    @NotNull
    public BaseMessage handleDelete(@NotNull final String id) {
        int deleted = 0;
        for (String hostAndPort : topology.getNodes()) {
            if (hostAndPort.equals(METHOD_AND_DOMAIN + port)) {
                continue;
            }

            HttpClient client = new HttpClient(new ConnectionString(hostAndPort));
            try {
                Response delete = client.delete(INTERCONNECTION_URI + id);
                if (delete.getStatus() == OK_STATUS) {
                    deleted++;
                }
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new BaseMessage(deleted, 0, 0, ack, from);
    }
}

