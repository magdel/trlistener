package ru.netradar.server.port.tr102;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.netradar.server.acceptor.sockets.connect.TRServerProtocol;
import ru.netradar.server.bus.domain.NRLocation;
import ru.netradar.server.bus.handler.tr102.Tr102Iden;
import ru.netradar.util.Util;
import ru.netradar.utils.IdGenerator;

import java.util.Optional;
import java.util.function.Function;

/**
 * Created by rfk on 18.11.2016.
 */
public class StringToNrLocationRecordMapper implements Function<String, Optional<Tr102Message>> {
    private static final Logger log = LoggerFactory.getLogger(StringToNrLocationRecordMapper.class);

    static String sep_comma = ",";
    static String S_1 = "1";
    static String S_2 = "2";
    static String S_3 = "3";
    private final IdGenerator idGenerator;

    public StringToNrLocationRecordMapper(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public Optional<Tr102Message> apply(String trackString) {
        try {
            return convertTr102(trackString, idGenerator);
        } catch (Exception ex) {
            log.error("Convert failed: dataString={}", trackString, ex);
            return Optional.empty();
        }
    }

    private static Optional<Tr102Message> convertTr102(String trackString, IdGenerator idGenerator) {
        //STX,Id-112233,$GPRMC,112842.000,A,6000.5274,N,03021.3429,E,0.00,0.00,241214,,,A*6C,F,,imei:013226008424265,03,23.0,Battery=70%,,0,250,02,1E82,173D;36

        String s = trackString;
        try {
            //$355632000166323,1,1,040202,093633,E12129.2252,N2459.8891,00161,0.0100,147,07*37!
            float errpos = 1;
            String[] info = Util.parseString(sep_comma + s, ',');
            errpos = 2;

            String imei = info[0].substring(1);
            errpos = 3;
            if (s.length() < 500) {
                log.info("Parsing: {}", s);
            } else {
                log.info("Strange big message: {}..", s.substring(0, 490));
            }
            errpos = 3.1f;


            errpos = 14;
            if (info[1].equals(S_2) || info[1].equals(S_3) || info[2].equals(S_1)) {
                return Optional.empty();
            }

            s = info[5];
            errpos = 15;
            double LON = Integer.parseInt(s.substring(1, 4)) + Double.parseDouble(s.substring(4, 11)) / 60.0;
            errpos = 1;
            if (s.charAt(0) == 'W') {
                LON = -LON;
            }

            errpos = 16;
            s = info[6];
            errpos = 17;
            double LAT = Integer.parseInt(s.substring(1, 3)) + Double.parseDouble(s.substring(3, 10)) / 60.0;
            errpos = 18;
            if (s.charAt(0) == 'S') {
                LAT = -LAT;
            }

            errpos = 19;
            int lat = (int) (LAT * 100000);
            errpos = 20;
            int lon = (int) (LON * 100000);
            errpos = 21;
            short alt = (short) Double.parseDouble(info[7]);
            errpos = 22;
            short crs = (short) Double.parseDouble(info[9]);
            errpos = 23;
            short spd = (short) (Double.parseDouble(info[8]) * 18.52);
            errpos = 24;

            //long dt = System.currentTimeMillis();
            long dt = TRServerProtocol.decodeGPSTime(info[3], info[4]);

            //log.info("Authorized at " + (new Date()) + " with GPS date " + new Date(dt) + " delay " + (System.currentTimeMillis() - dt) + "ms");

            return Optional.of(
                    new Tr102Message(
                            new Tr102Iden(imei),
                            new NRLocation(lat, lon, alt, spd, crs, dt)
                    )
            );
        } catch (RuntimeException exc) {
            log.error("On parse: text={}", s, exc);
            return Optional.empty();
        }

    }
}
