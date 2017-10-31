package ru.netradar.profiler       ;

/**
 * @author Pavel Raev
 */

class Measurement {

    static final double INITIAL_WEIGHT = 1.0d;
    static final double INCREMENT_WEIGHT = 1.0d;

    double measurement;
    double weight;

    Measurement(double measurement) {
        this.measurement = measurement;
        weight = INITIAL_WEIGHT;
    }

    Measurement() {
        measurement = 0;
        weight = 0;
    }

    double add(Measurement[] data, double trustInterval) {
        double average = measurement * this.weight;
        double weight = this.weight;
        int i;
        double cur_avg = 0;

        for (i = 0; i < data.length; i++) {
            Measurement m = data[i];
            average += m.measurement * m.weight;
            weight += m.weight;
            cur_avg += m.measurement;
        }

        average = average / weight;
        cur_avg = cur_avg / data.length;

        // lookup untrusted values
        boolean hasUntrusted = false;
        boolean[] trusted = new boolean[data.length];

        for (i = 0; i < data.length; i++) {
            if ((Math.abs(data[i].measurement - average) / average) > trustInterval) {
                // untrusted value detected
                trusted[i] = false;
                hasUntrusted = true;
            } else {
                trusted[i] = true;
            }
        }

        // if untrusted elements detected, recalc again
        if (hasUntrusted) {
            average = measurement * this.weight;
            weight = this.weight;
            for (i = 0; i < data.length; i++) {
                if (trusted[i]) {
                    Measurement m = data[i];
                    average += m.measurement * m.weight;
                    weight += m.weight;
                }
            }

            average = average / weight;
        }

        // assign mediane to current measurement
        measurement = average;
        this.weight = weight;

        return cur_avg;
    }

}