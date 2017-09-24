package ru.mail.polis.olerom;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.KVService;
import ru.mail.polis.olerom.storage.DummyDao;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 24.09.17
 *
 * @author olerom
 */
public class ServiceImpl implements KVService {
    private final static String METHOD_GET = "GET";
    private final static String METHOD_PUT = "PUT";
    private final static String METHOD_DELETE = "DELETE";

    @NotNull
    private final HttpServer server;
    @NotNull
    private final DummyDao<byte[], String> dao;

    public ServiceImpl(final int port, @NotNull final DummyDao<byte[], String> dao) throws IOException {

        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.dao = dao;

        this.server.createContext("/v0/status", (httpExchange -> {
            if (!httpExchange.getRequestMethod().equals(METHOD_GET)) {
                sendHttpResponseAndClose(httpExchange, Response.METHOD_NOT_ALLOWED, "Use GET method");
            } else {
                sendHttpResponseAndClose(httpExchange, Response.OK, "OK");
            }
        }));

        this.server.createContext("/v0/entity", httpExchange -> {

            final Map<String, String> parametersMap = getParametersMap(httpExchange.getRequestURI().getQuery());
            final String id = parametersMap.get("id");
            if (id == null) {
                sendHttpResponseAndClose(httpExchange, Response.BAD_REQUEST, "ID is necessary");
                return;
            }

            switch (httpExchange.getRequestMethod()) {
                case METHOD_GET:
                    if (dao.exists(id)) {
                        sendHttpResponseAndClose(httpExchange, Response.OK, this.dao.get(id));
                    } else {
                        sendHttpResponseAndClose(httpExchange, Response.NOT_FOUND, "Data is not found");
                    }
                    break;
                case METHOD_PUT:
                    final int contentLength =
                            Integer.valueOf(httpExchange.getRequestHeaders().getFirst("Content-Length"));

                    final byte[] value = new byte[contentLength];
                    if (httpExchange.getRequestBody().read(value) != contentLength && contentLength != 0) {
                        throw new IOException("Can't read at one go");
                    }

                    this.dao.save(value, id);
                    sendHttpResponseAndClose(httpExchange, Response.CREATED, "Successfully ad data");
                    break;
                case METHOD_DELETE:
                    this.dao.delete(id);
                    sendHttpResponseAndClose(httpExchange, Response.ACCEPTED, "Successfully delete data");
                    break;
                default:
                    sendHttpResponseAndClose(httpExchange, Response.METHOD_NOT_ALLOWED, "GET, PUT, DELETE are the only methods being supported");
                    break;
            }
        });
    }

    @Override
    public void start() {
        server.start();
    }

    @Override
    public void stop() {
        server.stop(0);
    }


    private void sendHttpResponseAndClose(@NotNull HttpExchange httpExchange, int code, @NotNull byte[] bytes) throws IOException {
        httpExchange.sendResponseHeaders(code, bytes.length);
        httpExchange.getResponseBody().write(bytes);
        httpExchange.getResponseBody().close();
        httpExchange.close();
    }

    private void sendHttpResponseAndClose(@NotNull HttpExchange httpExchange, int code, @NotNull String message) throws IOException {
        sendHttpResponseAndClose(httpExchange, code, message.getBytes());
    }

    @NotNull
    private Map<String, String> getParametersMap(@NotNull String query) {
        final String[] arguments = query.split("&");
        final Map<String, String> parametersMap = new HashMap<>(arguments.length);

        for (String argument : arguments) {
            final String[] split = argument.split("=");
            if (split.length != 1) {
                parametersMap.put(split[0], split[1]);
            }
        }

        return parametersMap;
    }

}
