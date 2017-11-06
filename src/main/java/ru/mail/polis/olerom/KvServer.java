package ru.mail.polis.olerom;

import one.nio.http.*;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.olerom.cluster.BaseMessage;
import ru.mail.polis.olerom.cluster.BodyMessage;
import ru.mail.polis.olerom.cluster.NodeManager;
import ru.mail.polis.olerom.cluster.Topology;
import ru.mail.polis.olerom.storage.DummyDao;

import java.io.IOException;

/**
 * Date: 27.09.17
 *
 * @author olerom
 */
public class KvServer extends HttpServer {
    @NotNull
    private final static String ID_PARAMETER = "id=";
    @NotNull
    private final static String REPLICAS_PARAMETER = "replicas=";
    @NotNull
    private final static String DELIMITER = "/";
    @NotNull
    private final static String EMPTY = "";

    @NotNull
    private final DummyDao<byte[], String> dao;

    @NotNull
    private final Topology topology;

    @NotNull
    private final NodeManager nodeManager;

    public KvServer(@NotNull final Configuration configuration,
                    @NotNull final DummyDao<byte[], String> dao,
                    @NotNull final Topology topology) throws IOException {
        super(configuration.getServerConfig());
        this.dao = dao;
        this.topology = topology;
        this.nodeManager = new NodeManager(topology, configuration.getPort());
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
            final String[] tokens = request.getParameter(REPLICAS_PARAMETER).split(DELIMITER);
            ack = Integer.valueOf(tokens[0]);
            from = Integer.valueOf(tokens[1]);
        }

        if (ack == 0 || ack > from) {
            session.sendResponse(new Response(Response.BAD_REQUEST, "Ack should be positive and less than from + 1".getBytes()));
            return;
        }

        nodeManager.setAck(ack);
        nodeManager.setFrom(from);

        switch (request.getMethod()) {
            case Request.METHOD_GET:

                final BodyMessage handledGet = nodeManager.handleGet(id);
                switch (handledGet.getResult()) {
                    case BodyMessage.OK:
                        session.sendResponse(new Response(Response.OK, handledGet.getValue()));
                        break;
                    case BodyMessage.OK_EXCEPT_CURRENT:
                        if (this.dao.exists(id) || !dao.isDeleted(id)) {
                            session.sendResponse(new Response(Response.OK, handledGet.getValue()));
                        } else {
                            session.sendResponse(new Response(Response.NOT_FOUND, "Data is not found".getBytes()));
                        }
                        break;
                    case BodyMessage.OK_EXCEPT_CURRENT_EMPTY:
                    case BodyMessage.OK_EXCEPT_CURRENT_NULL:
                        if (this.dao.exists(id)) {
                            session.sendResponse(new Response(Response.OK, dao.get(id)));
                        } else {
                            session.sendResponse(new Response(Response.NOT_FOUND, "Data is not found".getBytes()));
                        }
                        break;
                    case BodyMessage.NOT_FOUND:
                        session.sendResponse(new Response(Response.NOT_FOUND, "Data is not found".getBytes()));
                        break;
                    default:
                        session.sendResponse(new Response(Response.GATEWAY_TIMEOUT, "Not Enough Replicas".getBytes()));
                        break;
                }
                break;

            case Request.METHOD_PUT:
                final BaseMessage handledPut = nodeManager.handlePut(id, request.getBody());
                switch (handledPut.getResult()) {
                    case BaseMessage.OK:
                        dao.save(request.getBody(), id);
                        session.sendResponse(new Response(Response.CREATED, "Created".getBytes()));
                        break;
                    case BaseMessage.NOT_ENOUGH_REPLICAS:
                        session.sendResponse(new Response(Response.GATEWAY_TIMEOUT, "Not Enough Replicas".getBytes()));
                        break;
                }

                break;
            case Request.METHOD_DELETE:

                final BaseMessage handledDelete = nodeManager.handleDelete(id);
                switch (handledDelete.getResult()) {
                    case BaseMessage.OK:
                        if (dao.exists(id)) {
                            this.dao.delete(id);
                        }
                        session.sendResponse(new Response(Response.ACCEPTED, "Method was accepted".getBytes()));
                        return;

                    case BaseMessage.NOT_ENOUGH_REPLICAS:
                        session.sendResponse(new Response(Response.GATEWAY_TIMEOUT, "Not Enough Replicas".getBytes()));
                        break;
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
                }
                session.sendResponse(new Response(Response.OK, Response.EMPTY));
                break;
            case Request.METHOD_PUT:
                dao.save(request.getBody(), id);
                session.sendResponse(new Response(Response.OK, Response.EMPTY));
                break;
            case Request.METHOD_GET:
                if (dao.exists(id)) {
                    session.sendResponse(new Response(Response.OK, dao.get(id)));
                } else if (!dao.isDeleted(id)) {
                    session.sendResponse(new Response(Response.OK, Response.EMPTY));
                } else {
                    session.sendResponse(new Response(Response.NO_CONTENT, Response.EMPTY));
                }
                break;
        }
    }
}
