package ru.netradar.server.port.tr102;

import ru.netradar.server.bus.domain.NRLocation;
import ru.netradar.server.bus.handler.tr102.Tr102Iden;

/**
 * Created by rfk on 16.11.2017.
 */
public class Tr102Message {
    private final Tr102Iden tr102Iden;
    private final NRLocation nrLocation;

    public Tr102Message(Tr102Iden tr102Iden, NRLocation nrLocation) {
        this.tr102Iden = tr102Iden;
        this.nrLocation = nrLocation;
    }

    public Tr102Iden getTr102Iden() {
        return tr102Iden;
    }

    public NRLocation getNrLocation() {
        return nrLocation;
    }

    @Override
    public String toString() {
        return "Tr102Message{" +
                "tr102Iden=" + tr102Iden +
                ", nrLocation=" + nrLocation +
                '}';
    }
}
