package ru.mail.polis.olerom.storage;

import one.nio.mem.LongHashSet;
import one.nio.mem.OffheapMap;
import one.nio.mem.SharedMemoryMap;
import one.nio.mem.SharedMemoryStringMap;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Date: 24.09.17
 *
 * @author olerom
 */
public class DummyDaoImpl implements DummyDao<byte[], String> {

    @NotNull
    private final File data;


    //    @NotNull
//    private final OffheapMap<String, byte[]> cache;
    @NotNull
    private final HashMap<String, byte[]> cache;

    @NotNull
    private final HashSet<String> deletedCache;


    @NotNull
    private final ExecutorService executorService;

    public DummyDaoImpl(@NotNull final File file) {
        this.data = file;
        this.cache = new HashMap<>();
        this.deletedCache = new HashSet<>();
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void delete(@NotNull final String key) {
        cache.remove(getFilePathNameByKey(key));
        deletedCache.add(getFilePathNameDeletedByKey(key));

        executorService.submit(() -> {
            new File(getFilePathNameByKey(key)).delete();
            try {
                new File(getFilePathNameDeletedByKey(key)).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    @NotNull
    @Override
    public byte[] save(@NotNull final byte[] value,
                       @NotNull final String key) {
        final String fileName = getFilePathNameByKey(key);
        final String deletedFileName = getFilePathNameDeletedByKey(key);

        executorService.submit(() -> {
            final File file = new File(fileName);
            try {
                file.createNewFile();
                FileUtils.writeByteArrayToFile(file, value);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        cache.put(fileName, value);
        deletedCache.remove(deletedFileName);

        executorService.submit(() -> {
            final File deletedFile = new File(deletedFileName);
            deletedFile.delete();
        });

        return value;
    }

    @Override
    public boolean exists(@NotNull final String key) {
        final String fileName = getFilePathNameByKey(key);
        return cache.containsKey(fileName) || new File(fileName).exists();
    }

    @NotNull
    @Override
    public byte[] get(@NotNull final String key) throws IOException {
        final String fileName = getFilePathNameByKey(key);
        if (cache.containsKey(fileName)) {
            return cache.get(fileName);
        } else {
            final byte[] value = FileUtils.readFileToByteArray(new File(fileName));
            cache.put(key, value);
            return value;
        }
    }

    @Override
    public boolean isDeleted(@NotNull final String key) {
        final String deletedFileName = getFilePathNameDeletedByKey(key);
        if (deletedCache.contains(deletedFileName)) {
            return true;
        } else {
            return new File(deletedFileName).exists();
        }
    }

    @NotNull
    private String getFilePathNameByKey(@NotNull final String key) {
        return data.getAbsolutePath() + "/" + key;
    }

    @NotNull
    private String getFilePathNameDeletedByKey(@NotNull final String key) {
        return getFilePathNameByKey(key) + "deleted";
    }
}
