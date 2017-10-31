package ru.netradar.profiler;

/**
 * @author Pavel Raev
 */

class Entry {

    private final String method;

    private final Measurement[] data;
    private int idx;
    private final Measurement mediane = new Measurement();

    private long totalExecutions;

    private long min = Long.MAX_VALUE;
    private long max;
    private double last_cur_avg;

    Entry(String mtd, int serie) {
        method = mtd;
        data = new Measurement[serie];
    }

    /**
     * This method MUST be thread-safe
     *
     * @param time current measurement
     */
    void save(long time) {
        String logRecord = null;

        synchronized (this) {
            if (time < min) {
                min = time;
            }

            if (time > max) {
                max = time;
            }

            data[idx++] = new Measurement((double) time);
            totalExecutions++;

            if (idx >= data.length) {
                // recalc sequence
                double cur_avg = mediane.add(data, Profiler.trustInterval);
                last_cur_avg = cur_avg;
                // flush sequence
                idx = 0;
                // LOG
                logRecord = method + " min:" + format(min) + " max:" + format(max) +
                        " abs-avg:" + format(mediane.measurement)
                        + " cur-avg:" + format(cur_avg)
                        + " trusted executions:" + ((long) mediane.weight)
                        + " total executions:" + totalExecutions;
            }
        }

        if (logRecord != null) {
            Profiler.LOG.info(logRecord);
        }
    }

    String getInfo() {
        String logRecord;

        synchronized (this) {
            logRecord = method + " min:" + format(min) + " max:" + format(max) +
                    " abs-avg:" + format(mediane.measurement)
                    + " cur-avg:" + format(last_cur_avg)
                    + " trusted executions:" + ((long) mediane.weight)
                    + " total executions:" + totalExecutions;
        }
        return logRecord;
    }

    /**
     * nanoseconds formatting
     *
     * @param nanos nanosecs
     * @return string
     */
    private String format(final double nanos) {
        long integer = (long) (nanos / 1E4);
        return Double.toString(((double) integer) / 1E2);
    }

}

