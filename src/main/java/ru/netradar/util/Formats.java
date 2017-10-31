package ru.netradar.util;

import java.util.Calendar;

public class Formats {

    /**
     * Форматтер ISO:8601:2004 времени 
     *
     * @param time время
     * @return буфер
     */
    public static StringBuilder formatISOTime(long time) {
        StringBuilder sb = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        sb.append(calendar.get(Calendar.YEAR)).append('-');

        int i = calendar.get(Calendar.MONTH) + 1;
        if (i < 10) sb.append('0');
        sb.append(i).append('-');
        i = calendar.get(Calendar.DAY_OF_MONTH);
        if (i < 10) sb.append('0');
        sb.append(i).append('T');
        i = calendar.get(Calendar.HOUR_OF_DAY);
        if (i < 10) sb.append('0');
        sb.append(i).append(':');
        i = calendar.get(Calendar.MINUTE);
        if (i < 10) sb.append('0');
        sb.append(i).append(':');
        i = calendar.get(Calendar.SECOND);
        if (i < 10) sb.append('0');
        sb.append(i).
                append('.').append(calendar.get(Calendar.MILLISECOND));

        // calc DST offset from millis
        long zMillis = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);

        if (zMillis == 0) {
            sb.append('Z');
        } else {
            if (zMillis >= 0)
                sb.append('+');
            else {
                sb.append('-');
                zMillis = -zMillis;
            }

            long hrs = zMillis / 3600000L;
            if (hrs < 10) sb.append('0');
            sb.append(hrs).append(':');
            long mi = (zMillis - hrs * 3600000L) / 60000L;
            if (mi < 10) sb.append('0');
            sb.append(mi);
        }
        return sb;
    }

    /**
     * Форматтер периода времени для логгирвания, миллисекунды в человечемколе предстваление: X days N hours Y minutes
     *
     * @param timePeriod время
     * @return буфер
     */
    public static StringBuilder formatTimePeriod(long timePeriod) {
        StringBuilder sb = new StringBuilder();
        // initial as seconds, миллисекунды игнорируются
        long sec = timePeriod / 1000;

        long days = sec / 86400;
        if (days > 0) {
            sb.append(days).append(" days ");
            sec -= days * 86400;
        }

        long hours = sec / 3600;
        if (hours > 0) {
            sb.append(hours).append(" hours ");
            sec -= hours * 3600;
        }

        long minutes = sec / 60;
        if (minutes > 0) {
            sb.append(minutes).append(" min ");
            sec -= minutes * 60;
        }

        sb.append(sec).append(" sec");
        return sb;
    }

}
