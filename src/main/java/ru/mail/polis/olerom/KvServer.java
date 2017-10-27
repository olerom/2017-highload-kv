package ru.mail.polis.olerom;

import one.nio.config.ConfigParser;
import one.nio.http.*;
import one.nio.net.ConnectionString;
import one.nio.pool.PoolException;
import one.nio.server.ServerConfig;
import one.nio.util.Utf8;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.olerom.cluster.NodeManager;
import ru.mail.polis.olerom.cluster.Topology;
import ru.mail.polis.olerom.storage.DummyDao;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * Date: 27.09.17
 *
 * @author olerom
 */
public class KvServer extends HttpServer {
    @NotNull
    private final static String CONF = "\n" +
            "acceptors:\n" +
            " - port: {PORT}\n";

    @NotNull
    private final static String ID_PARAMETER = "id=";
    @NotNull
    private final static String REPLICAS_PARAMETER = "replicas=";
    @NotNull
    private final static String DELIMETER = "/";
    @NotNull
    private final static String EMPTY = "";

    @NotNull
    private final DummyDao<byte[], String> dao;

    @NotNull
    private final Topology topology;

    public KvServer(@NotNull final Configuration configuration,
                    @NotNull final DummyDao<byte[], String> dao,
                    @NotNull final Topology topology) throws IOException {
        super(configuration.getServerConfig());
        this.dao = dao;
        this.topology = topology;
    }

    @Path("/v0/status")
    public Response status() {
        return Response.ok("200");
    }

    @Path("/v0/entity")
    public void entity(Request request, HttpSession session) throws IOException {
        final String id = request.getParameter(ID_PARAMETER);

        if (id.equals(EMPTY)) {
            session.sendResponse(new Response(Response.BAD_REQUEST, "ID parameter is necessary".getBytes()));
            return;
        }

        final int ack;
        final int from;

        if (request.getParameter(REPLICAS_PARAMETER) == null) {
            ack = topology.getQuorum();
            from = topology.getSize();
        } else {
            final String[] tokens = request.getParameter(REPLICAS_PARAMETER).split(DELIMETER);
            ack = Integer.valueOf(tokens[0]);
            from = Integer.valueOf(tokens[1]);
        }

        if (ack == 0 || ack > from) {
            session.sendResponse(new Response(Response.BAD_REQUEST, "Ack should be positive and less than from".getBytes()));
            return;
        }

//        final NodeManager nodeManager = new NodeManager(ack, from, port, topology);

        int satisfied = 0;
        int visited = 0;
        switch (request.getMethod()) {
            case Request.METHOD_GET:
                byte[] resp = null;
                int deaths = 0;
                for (String hostAndPort : topology.getNodes()) {
                    if (hostAndPort.equals("http://localhost:" + port)) {
                        continue;
                    }

                    visited++;
                    HttpClient client = new HttpClient(new ConnectionString(hostAndPort));

                    try {
                        Response get = client.get("/v0/interconnection" + "?id=" + id);
                        if (get.getStatus() == 200) {
                            satisfied++;
                            if (resp == null) {
                                resp = get.getBody();
                            }
                        }
                    } catch (Exception e) {
                        deaths++;
                        e.printStackTrace();
                    } finally {
                        client.close();
                    }
                }

                visited++;
                if (this.dao.exists(id)) {
                    resp = dao.get(id);
                    satisfied++;
                } else if (resp != null) {
                    dao.save(resp, id);
                    satisfied++;
                }

                System.out.println("GET, visited = " + visited + ", deaths = " + deaths + ", satisfied = " + satisfied
                        + ", ack = " + ack + ", from = " + from);

                if (visited - deaths < ack) {
                    session.sendResponse(new Response(Response.GATEWAY_TIMEOUT, "Not Enough Replicas".getBytes()));
                } else if (satisfied >= ack) {
                    session.sendResponse(new Response(Response.OK, resp));
                } else if (satisfied < ack) {
                    session.sendResponse(new Response(Response.NOT_FOUND, "Data is not found".getBytes()));
                } else {
                    session.sendResponse(new Response(Response.GATEWAY_TIMEOUT, "Not Enough Replicas".getBytes()));
                }
                break;

            case Request.METHOD_PUT:

                for (String hostAndPort : topology.getNodes()) {
                    if (hostAndPort.equals("http://localhost:" + port)) {
                        continue;
                    }

                    System.out.println("Look at topo: " + hostAndPort);

                    visited++;
                    HttpClient client = new HttpClient(new ConnectionString(hostAndPort));

                    try {
                        Response put = client.put("/v0/interconnection" + "?id=" + id, request.getBody());
                        if (put.getStatus() == 200) {
                            satisfied++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        client.close();
                    }
                }

                satisfied++;
                visited++;
                dao.save(request.getBody(), id);

                System.out.println("PUT, visited = " + visited + ", satisfied = " + satisfied);
                if (satisfied >= ack) {
                    session.sendResponse(new Response(Response.CREATED, "Created".getBytes()));
                } else {
                    session.sendResponse(new Response(Response.GATEWAY_TIMEOUT, "Not Enough Replicas".getBytes()));
                }

                break;
            case Request.METHOD_DELETE:

                for (String hostAndPort : topology.getNodes()) {
                    if (hostAndPort.equals("http://localhost:" + port)) {
                        continue;
                    }
                    if (visited >= from - 1) {
                        break;
                    }
                    visited++;

                    HttpClient client = new HttpClient(new ConnectionString(hostAndPort));

                    try {
                        Response delete = client.delete("/v0/interconnection" + "?id=" + id);
                        if (delete.getStatus() == 200) {
                            satisfied++;
                        }
                        client.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                visited++;
                satisfied++;
                if (dao.exists(id)) {
                    this.dao.delete(id);
                }

                System.out.println("DELETE, visited = " + visited + ", satisfied = " + satisfied);
                if (satisfied >= ack) {
                    session.sendResponse(new Response(Response.ACCEPTED, "Method was accepted".getBytes()));
                } else {
                    session.sendResponse(new Response(Response.GATEWAY_TIMEOUT, "Not Enough Replicas".getBytes()));
                }
                break;

            default:
                session.sendResponse(new Response(Response.METHOD_NOT_ALLOWED, "GET, PUT and DELETE are the only one methods being supported".getBytes()));
                break;
        }
    }

    @Path("/v0/interconnection")
    public void interconnection(Request request, HttpSession session) throws IOException {

        final String id = request.getParameter(ID_PARAMETER);
        switch (request.getMethod()) {
            case Request.METHOD_DELETE:
                if (dao.exists(id)) {
                    dao.delete(id);
                    session.sendResponse(new Response(Response.OK, new byte[0]));
                }
                session.sendResponse(new Response(Response.OK, new byte[0]));
                break;
            case Request.METHOD_PUT:
                dao.save(request.getBody(), id);
                session.sendResponse(new Response(Response.OK, new byte[0]));
                break;
            case Request.METHOD_GET:
                if (dao.exists(id)) {
                    session.sendResponse(new Response(Response.OK, dao.get(id)));
                } else {
                    session.sendResponse(new Response(Response.NO_CONTENT, new byte[0]));
                }
                break;
        }
    }
}
