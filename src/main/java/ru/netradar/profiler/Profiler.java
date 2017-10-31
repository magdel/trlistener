package ru.netradar.profiler;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Pavel Raev
 */
public final class Profiler {


    static final Logger LOG = Logger.getLogger(Profiler.class);

    public static final double trustInterval = 2d;
    public static final int defaultSerie = 20;
    private static final Map<String, Entry> stats = new HashMap<String, Entry>();

    private static final ThreadLocal<Map<String, Long>> thrSamples = new ThreadLocal<Map<String, Long>>() {
        protected Map<String, Long> initialValue() {
            return new HashMap<String, Long>();
        }
    };

    public static void startSample(String key) {
        if (thrSamples.get().put(key, System.nanoTime()) != null) {
            LOG.warn("Measurement sample for key: " + key + " last abandoned detected");
        }
    }

    public static void endSample(String key) {
        endSample(key, defaultSerie);
    }

    public static void endSample(String key, int serieLength) {
        long measurement = System.nanoTime();
        Long startTime = thrSamples.get().remove(key);
        if (startTime == null) {
            LOG.warn("endSample() called without startSample(), for key: " + key);
            return;
        }

        measurement -= startTime;

        // save stats
        Entry e = stats.get(key);

        if (e == null) {
            e = new Entry(key, serieLength);
            synchronized (stats) {
                stats.put(key, e);
            }
        }

        e.save(measurement);
    }

    public static String getLogInfo(String key) {
        // save stats
        Entry e = stats.get(key);
        if (e == null) {
            return "No info";
        } else {
            return e.getInfo();
        }
    }

    public static Set<String> getKeys() {
        synchronized (stats) {
            return new HashSet<String>(stats.keySet());
        }
    }

}
