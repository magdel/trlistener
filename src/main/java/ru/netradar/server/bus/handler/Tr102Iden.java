package ru.netradar.server.bus.handler;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Created by rfk on 16.11.2017.
 */
public class Tr102Iden {
    private final String imei;

    public Tr102Iden(@Nonnull String imei) {
        this.imei = Objects.requireNonNull(imei, "imei");
    }

    @Nonnull
    public String getImei() {
        return imei;
    }

    @Override
    public String toString() {
        return "Tr102Iden{" +
                "imei='" + imei + '\'' +
                '}';
    }
}
