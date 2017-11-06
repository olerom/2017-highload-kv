package ru.mail.polis.olerom.storage;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;

/**
 * Date: 24.09.17
 *
 * @author olerom
 */
public interface DummyDao<V, K extends Serializable> {

    void delete(@NotNull K key);

    @NotNull
    <T extends V> T save(@NotNull T value, @NotNull K key);

    boolean exists(@NotNull K key);

    @NotNull
    V get(@NotNull K key) throws IOException;

    boolean isDeleted(@NotNull K key);
}
