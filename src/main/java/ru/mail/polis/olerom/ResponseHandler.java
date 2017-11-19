package ru.mail.polis.olerom;

import one.nio.http.HttpSession;
import one.nio.http.Response;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.olerom.cluster.BaseMessage;
import ru.mail.polis.olerom.cluster.BodyMessage;
import ru.mail.polis.olerom.cluster.NodeManager;
import ru.mail.polis.olerom.storage.DummyDao;

import java.io.IOException;

/**
 * Date: 19.11.17
 *
 * @author olerom
 */
public class ResponseHandler {
    @NotNull
    private final NodeManager nodeManager;

    @NotNull
    private final HttpSession session;

    @NotNull
    private final DummyDao<byte[], String> dao;

    public ResponseHandler(@NotNull final NodeManager nodeManager,
                           @NotNull final HttpSession session,
                           @NotNull final DummyDao<byte[], String> dao) {
        this.nodeManager = nodeManager;
        this.session = session;
        this.dao = dao;
    }

    public void sendGetResponse(@NotNull final String id) throws IOException {

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
    }

    public void sendPutResponse(@NotNull final String id,
                                @NotNull final byte[] body) throws IOException {
        final BaseMessage handledPut = nodeManager.handlePut(id, body);
        switch (handledPut.getResult()) {
            case BaseMessage.OK:
                dao.save(body, id);
                session.sendResponse(new Response(Response.CREATED, "Created".getBytes()));
                break;
            case BaseMessage.NOT_ENOUGH_REPLICAS:
                session.sendResponse(new Response(Response.GATEWAY_TIMEOUT, "Not Enough Replicas".getBytes()));
                break;
        }
    }


    public void sendDeleteResponse(@NotNull final String id) throws IOException {
        final BaseMessage handledDelete = nodeManager.handleDelete(id);
        switch (handledDelete.getResult()) {
            case BaseMessage.OK:
                if (dao.exists(id)) {
                    this.dao.delete(id);
                }
                session.sendResponse(new Response(Response.ACCEPTED, "Method was accepted".getBytes()));
                break;

            case BaseMessage.NOT_ENOUGH_REPLICAS:
                session.sendResponse(new Response(Response.GATEWAY_TIMEOUT, "Not Enough Replicas".getBytes()));
                break;
        }
    }

    public void sendMethodNotAllowedResponse() throws IOException {
        session.sendResponse(new Response(Response.METHOD_NOT_ALLOWED, "GET, PUT and DELETE are the only one methods being supported".getBytes()));
    }
}
