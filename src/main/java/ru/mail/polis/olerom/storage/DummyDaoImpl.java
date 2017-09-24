package ru.mail.polis.olerom.storage;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * Date: 24.09.17
 *
 * @author olerom
 */
public class DummyDaoImpl implements DummyDao<byte[], String> {

    @NotNull
    private final File data;

    public DummyDaoImpl(@NotNull File file) {
        this.data = file;
    }

    @Override
    public void delete(@NotNull String key) {
        new File(data.getAbsolutePath() + key.toString()).delete();
    }

    @NotNull
    @Override
    public byte[] save(@NotNull byte[] value, @NotNull String key) {
        final File file = new File(data.getAbsolutePath() + key.toString());
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileUtils.writeByteArrayToFile(file, value);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return value;
    }

    @Override
    public boolean exists(@NotNull String key) {
        return new File(data.getAbsolutePath() + key.toString()).exists();
    }

    @NotNull
    @Override
    public byte[] get(@NotNull String key) throws IOException {
        return FileUtils.readFileToByteArray(new File(data.getAbsolutePath() + key.toString()));
    }
}
