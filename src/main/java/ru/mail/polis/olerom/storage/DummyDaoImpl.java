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
        new File(getFilePathNameByKey(key)).delete();
        try {
            new File(getFilePathNameDeletedByKey(key)).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public byte[] save(@NotNull byte[] value, @NotNull String key) {
        final File file = new File(getFilePathNameByKey(key));
        final File deletedFile = new File(getFilePathNameDeletedByKey(key));
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileUtils.writeByteArrayToFile(file, value);

            if (deletedFile.exists()) {
                deletedFile.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return value;
    }

    @Override
    public boolean exists(@NotNull String key) {
        return new File(getFilePathNameByKey(key)).exists();
    }

    @NotNull
    @Override
    public byte[] get(@NotNull String key) throws IOException {
        return FileUtils.readFileToByteArray(new File(getFilePathNameByKey(key)));
    }

    @Override
    public boolean isDeleted(@NotNull String key) {
        final File file = new File(getFilePathNameDeletedByKey(key));
        if (file.exists()) {
            return true;
        } else {
            return false;
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
