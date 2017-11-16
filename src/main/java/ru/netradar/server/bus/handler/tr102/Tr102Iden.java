package ru.netradar.server.bus.handler.tr102;

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Tr102Iden tr102Iden = (Tr102Iden) o;

        return imei.equals(tr102Iden.imei);
    }

    @Override
    public int hashCode() {
        return imei.hashCode();
    }

    @Override
    public String toString() {
        return "Tr102Iden{" +
                "imei='" + imei + '\'' +
                '}';
    }
}
