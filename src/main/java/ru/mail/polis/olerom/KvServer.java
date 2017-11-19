package ru.mail.polis.olerom;

import one.nio.http.*;
import one.nio.server.ServerConfig;
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
    private final Configuration configuration;

    public KvServer(@NotNull final Configuration configuration,
                    @NotNull final DummyDao<byte[], String> dao,
                    @NotNull final Topology topology) throws IOException {
        super(configuration.getServerConfig());
        this.configuration = configuration;
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
            final String[] tokens = request.getParameter(REPLICAS_PARAMETER).split(DELIMITER);
            ack = Integer.valueOf(tokens[0]);
            from = Integer.valueOf(tokens[1]);
        }

        if (ack == 0 || ack > from) {
            session.sendResponse(new Response(Response.BAD_REQUEST, "Ack should be positive and less than from + 1".getBytes()));
            return;
        }

        final NodeManager nodeManager = new NodeManager(topology, configuration.getPort(), ack, from);
        final ResponseHandler responseHandler = new ResponseHandler(nodeManager, session, dao);

        switch (request.getMethod()) {
            case Request.METHOD_GET:
                responseHandler.sendGetResponse(id);
                break;
            case Request.METHOD_PUT:
                responseHandler.sendPutResponse(id, request.getBody());
                break;
            case Request.METHOD_DELETE:
                responseHandler.sendDeleteResponse(id);
                break;
            default:
                responseHandler.sendMethodNotAllowedResponse();
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
