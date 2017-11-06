package ru.mail.polis.olerom;

import one.nio.http.HttpServer;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.KVService;
import ru.mail.polis.olerom.cluster.Topology;
import ru.mail.polis.olerom.storage.DummyDao;

import java.io.IOException;
import java.util.Set;

/**
 * Date: 24.09.17
 *
 * @author olerom
 */
public class ServiceImpl implements KVService {
    @NotNull
    private final HttpServer server;

    Configuration c;

    public ServiceImpl(@NotNull final Configuration configuration,
                       @NotNull final DummyDao<byte[], String> dao,
                       @NotNull final Topology topology) throws IOException {
        this.server = new KvServer(configuration, dao, topology);
        c = configuration;
    }

    @Override
    public void start() {
        server.start();
    }

    @Override
    public void stop() {

        System.out.println(server.getConnections());
        server.stop();
        System.out.println(server.getKeepAlive());
        System.out.println(server.getSelectorCount());
        System.out.println(server.getConnections());
//        System.out.println(server.getWorkers());
        server.stop();
//        System.out.println(server.getConnections());
//        server.getConnections();
    }


}
