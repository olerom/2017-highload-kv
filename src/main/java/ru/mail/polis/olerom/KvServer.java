package ru.mail.polis.olerom;

import one.nio.config.ConfigParser;
import one.nio.http.*;
import one.nio.server.ServerConfig;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.olerom.storage.DummyDao;

import java.io.IOException;

/**
 * Date: 27.09.17
 *
 * @author olerom
 */
public class KvServer extends HttpServer {
    private final static String CONF = "\n" +
            "keepAlive: 120s\n" +
            "maxWorkers: 1000\n" +
            "queueTime: 50ms\n" +
            "\n" +
            "acceptors:\n" +
            " - port: {PORT}\n" +
            "   backlog: 10000\n" +
            "   deferAccept: true";

    @NotNull
    private final DummyDao<byte[], String> dao;

    public KvServer(int port, @NotNull final DummyDao<byte[], String> dao) throws IOException {
        super(ConfigParser.parse(CONF.replace("{PORT}", port + ""), ServerConfig.class));
        this.dao = dao;
    }

    @Path("/v0/status")
    public Response status() {
        return Response.ok("200");
    }

    @Path("/v0/entity")
    public void entity(Request request, HttpSession session) throws IOException {

        String id = request.getParameter("id=");

        if (id.equals("")) {
            session.sendResponse(new Response(Response.BAD_REQUEST, "ID parameter is necessary".getBytes()));
            return;
        }

        switch (request.getMethod()) {
            case Request.METHOD_GET:
                if (this.dao.exists(id)) {
                    session.sendResponse(new Response(Response.OK, dao.get(id)));
                } else {
                    session.sendResponse(new Response(Response.NOT_FOUND, "Data is not found".getBytes()));
                }
                break;
            case Request.METHOD_PUT:
                dao.save(request.getBody(), id);
                session.sendResponse(new Response(Response.CREATED, "Created".getBytes()));
                break;
            case Request.METHOD_DELETE:
                this.dao.delete(id);
                session.sendResponse(new Response(Response.ACCEPTED, "Method was accepted".getBytes()));
                break;
            default:
                session.sendResponse(new Response(Response.METHOD_NOT_ALLOWED, "GET, PUT and DELETE are the only one methods being supported".getBytes()));
                break;
        }
    }

}
