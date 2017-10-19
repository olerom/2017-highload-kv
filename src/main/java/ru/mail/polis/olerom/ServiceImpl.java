package ru.mail.polis.olerom;

import one.nio.http.HttpServer;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.KVService;
import ru.mail.polis.olerom.storage.DummyDao;

import java.io.IOException;

/**
 * Date: 24.09.17
 *
 * @author olerom
 */
public class ServiceImpl implements KVService {
    @NotNull
    private final HttpServer server;

    public ServiceImpl(final int port, @NotNull final DummyDao<byte[], String> dao) throws IOException {
        this.server = new KvServer(port, dao);
    }

    @Override
    public void start() {
        server.start();
    }

    @Override
    public void stop() {
        server.stop();
    }


}
