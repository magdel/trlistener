package ru.netradar.server.queue.dao;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.util.Objects;

public abstract class Task {
    private final Long id;
    private Timestamp nextSendDt;
    private int tryN;
    private Timestamp expiryDt;

    public static final long MINUTE = 60000;
    public static final long HOUR = 60 * 60000;
    public static final long SCHEDULE_INTERVAL = 2 * MINUTE;

    public Task(@Nonnull Long id, Timestamp nextSendDt, int tryN, Timestamp expiryDt) {
        this.id = Objects.requireNonNull(id, "id");
        this.nextSendDt = nextSendDt;
        this.tryN = tryN;
        this.expiryDt = expiryDt;
    }

    public Timestamp getNextSendDt() {
        return nextSendDt;
    }

    public int getTryN() {
        return tryN;
    }

    public Timestamp getExpiryDt() {
        return expiryDt;
    }

    @Nonnull
    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", nextSendDt=" + nextSendDt +
                ", tryN=" + tryN +
                ", expiryDt=" + expiryDt +
                '}';
    }
}
