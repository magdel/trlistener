package ru.netradar.util;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public final class Util {
    private final static Logger LOG = Logger.getLogger(Util.class);

    private static final org.joda.time.format.DateTimeFormatter HTTP_DATE_TIME_FORMATTER_MSK =
            new DateTimeFormatterBuilder()
                    .appendDayOfMonth(2)
                    .appendLiteral('.')
                    .appendMonthOfYear(2)
                    .appendLiteral('.')
                    .appendYear(4, 9)
                    .appendLiteral(' ')
                    .appendHourOfDay(2)
                    .appendLiteral('.')
                    .appendMinuteOfHour(2)
                    .toFormatter()
                    .withZone(DateTimeZone.forID("Europe/Moscow"));

    public static String getHumanTime(long millis) {
        if (millis < 3000) {
            return "" + millis + " мсек";
        } else if (millis < 60000) {
            return "" + (millis / 1000) + " сек";
        } else if (millis < 120000) {
            long sec = (millis % 60000) / 1000;
            return "" + (millis / 60000) + " мин " + sec + " с";
        } else if (millis < 60000 * 90) {
            return "" + (millis / 60000) + " мин ";
        } else if (millis < 60000 * 60 * 24) {
            long min = (millis % (60 * 60000)) / 60000;
            return "" + (millis / (60 * 60000)) + " ч " + min + " мин";
        } else {
            return "" + (millis / (60 * 60000)) + " ч ";
        }
    }

    public final static String urlEncodeString(String url) {
        StringBuffer stringbuffer = new StringBuffer();
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            switch (c) {
                case '=': // ' '

                case 32: // ' '
                case 38: // '&'
                case 40: // '('
                case 41: // ')'
                case 58: // ':'
                case 63: // '?'
                case 64: // '@'
                    stringbuffer.append('%'); // Add % character
                    stringbuffer.append(toHexChar((c & 0xF0) >> 4));
                    stringbuffer.append(toHexChar(c & 0x0F));
                    break;

                default:
                    stringbuffer.append(c);
                    break;
            }
        }

        return stringbuffer.toString();
    }

//    /**
//     * Converts Hex digit to a UTF-8 "Hex" character
//     * @param digitValue digit to convert to Hex
//     * @return the converted Hex digit

    //     */

    static private char toHexChar(int digitValue) {
        if (digitValue < 10)
        // Convert value 0-9 to char 0-9 hex char
        {
            return (char) ('0' + digitValue);
        } else
        // Convert value 10-15 to A-F hex char
        {
            return (char) ('A' + (digitValue - 10));
        }
    }

    public final static ByteArrayOutputStream getHTTPContent(String url) {
        InputStream is = null;
        //OutputStream os = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[100];
        int bufPos = 0;
        HttpURLConnection c;
        try {
            c = getHttpConn(url);
            //c = (HttpConnection)Connector.open(url);
            try {
                //c.setRequestMethod(HttpConnection.GET);
                is = c.getInputStream();
                byte[] pb = new byte[1024];
                int nr = 0;
                while (true) {
                    nr = is.read(pb, 0, pb.length);
                    if (nr < 0) {
                        break;
                    }
                    baos.write(pb, 0, nr);
                }
                pb = null;

            } finally {
                try {
                    c.disconnect();
                } catch (Throwable t) {
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (Throwable t) {
                }
            }
        } catch (Throwable e) {
            LOG.error("getHTTPContent: " + url + " : " + e.toString());
        }
        return baos;
    }

    /**
     * Return connection even if redirection happens
     */
    public final static HttpURLConnection getHttpConn(String url) throws IOException {

        HttpURLConnection httpConn = null;
        int numTry = 0;
        int respCode = -1;
        while (numTry++ < 5) {
            URL u = new URL(url);
            httpConn = (HttpURLConnection) u.openConnection();
            httpConn.setRequestMethod("GET");
            respCode = httpConn.getResponseCode();

            //   if(respCode != HttpURLConnection.HTTP_TEMP_REDIRECT) {
            if (respCode != HttpURLConnection.HTTP_MOVED_TEMP) {
                if (respCode != HttpURLConnection.HTTP_MOVED_PERM) {
                    break;
                }
            }
            //  }
            url = httpConn.getHeaderField("Location");
            httpConn.disconnect();
        }
        // if(respCode == 200) {
        return httpConn;
        //  }

    }

    final public static String[] parseString(String s, char delim) throws RuntimeException {
        int i = 0;
        int pos = 0;
        int nextPos = 0;
        String[] result = new String[140];
        // check how big the array is.
        while (pos > -1) {
            pos = s.indexOf(delim, pos);
            if (pos < 0) {
                continue;
            }
            pos++;
            i++;
        }

        if (i > 500) {
            throw new RuntimeException("to big:" + i);
        }

        i = 0;
        pos = 0;

        // Start splitting the string, search for each ','
        while (pos > -1) {
            pos = s.indexOf(delim, pos);
            if (pos < 0) {
                continue;
            }

            nextPos = s.indexOf(delim, pos + 1);
            if (nextPos < 0) {
                nextPos = s.length();
            }

            result[i] = s.substring(pos + 1, nextPos).trim();
            i++;
            if (pos > -1) {
                pos++;
            }
        }

        return result;
    }

    public static double javaTime2WinTime(long ts) {
        return (double) ((double) ts / 86400000.0d + 25569.0d);
    }

    public static long winTime2JavaTime(double dt) {
        return (long) ((dt - 25569.0d) * 86400000.0d);
    }

    //** Writes string to Output Stream in utf format */
    public static final void writeStr2OS(OutputStream os, String s) throws IOException {
        os.write(Util.stringToByteArray(s, true));
    }

    public final static int hex2int(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= 'A' && c <= 'F') {
            return (c - 65) + 10;
        }
        if (c >= 'a' && c <= 'f') {
            return (c - 97) + 10;
        } else {
            return 0;
        }
    }

    static public DataInputStream getDataInputStream(byte[] array, int offset) {
        return new DataInputStream(new ByteArrayInputStream(array, offset, array.length - offset));
    }

    static public DataInputStream getDataInputStream(byte[] array) {
        return getDataInputStream(array, 0);
    }

    // Puts the specified word (val) into the buffer (buf) at position off using the specified byte ordering (bigEndian)

    public static void putWord(byte[] buf, int off, int val, boolean bigEndian) {
        if (bigEndian) {
            buf[off] = (byte) ((val >> 8) & 0x000000FF);
            buf[++off] = (byte) ((val) & 0x000000FF);
        } else   // Little endian
        {
            buf[off] = (byte) ((val) & 0x000000FF);
            buf[++off] = (byte) ((val >> 8) & 0x000000FF);
        }
    }


    // Puts the specified word (val) into the buffer (buf) at position off using big endian byte ordering
    public static void putWord(byte[] buf, int off, int val) {
        Util.putWord(buf, off, val, true);
    }


    // Extracts a string from the buffer (buf) starting at position off, ending at position off+len

    public static String byteArrayToString(byte[] buf, int off, int len, boolean utf8) {

        // Length check
        if (buf.length < off + len) {
            return (null);
        }

        // Remove \0's at the end
        while ((len > 0) && (buf[off + len - 1] == 0x00)) {
            len--;
        }

        // Read string in UTF-8 format
        if (utf8) {
            try {
                byte[] buf2 = new byte[len + 2];
                Util.putWord(buf2, 0, len);
                System.arraycopy(buf, off, buf2, 2, len);
                ByteArrayInputStream bais = new ByteArrayInputStream(buf2);
                DataInputStream dis = new DataInputStream(bais);
                return (dis.readUTF());
            } catch (Exception e) {
                // do nothing
            }
        }

        // CP1251 or default character encoding?
        //if (Options.getBoolean(Options.OPTION_CP1251_HACK))
        if (true) {
            return (byteArray1251ToString(buf, off, len));
        } else {
            return (new String(buf, off, len));
        }

    }

    // Converts the specified buffer (buf) to a string

    public static String byteArrayUTFToString(byte[] buf) {
        return Util.byteArrayToString(buf, 0, buf.length, true);
    }

    public static String byteArrayToString(byte[] buf, boolean utf8) {
        return (Util.byteArrayToString(buf, 0, buf.length, utf8));
    }

    public static byte[] stringLatinToByteArray(String val) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            for (int i = 0; i < val.length(); i++) {
                dos.writeByte((byte) val.charAt(i));
            }

            return baos.toByteArray();
        } catch (Exception e) {
            // Do nothing
        }
        return null;
    }

    // Converts the specified string (val) to a byte array
    public static byte[] stringToByteArray(String val, boolean utf8) {
        // Write string in UTF-8 format
        if (utf8) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                dos.writeUTF(val);
                byte[] raw = baos.toByteArray();
                // dos=null;baos=null;
                byte[] result = new byte[raw.length - 2];
                System.arraycopy(raw, 2, result, 0, raw.length - 2);
                return result;
            } catch (Exception e) {
                // Do nothing
            }
        }

//		// CP1251 or default character encoding?
//		//if (Options.getBoolean(Options.OPTION_CP1251_HACK))
//		if (true)
//		{
        return (stringToByteArray1251(val));
//		}
//		else
//		{
//			return (val.getBytes());
//		}

    }

    //
    // Restores CRLF sequense from LF

    public static byte[] restoreCrLfToByteArray(String val, boolean utf) {
        return stringToByteArray(restoreCrLf(val), utf);
    }

    // Restores CRLF sequense from LF
    public static String restoreCrLf(String val) {
        StringBuffer result = new StringBuffer();
        int size = val.length();
        for (int i = 0; i < size; i++) {
            char chr = val.charAt(i);
            if (chr == '\r') {
                continue;
            }
            if (chr == '\n') {
                result.append("\r\n");
            } else {
                result.append(chr);
            }
        }
        return result.toString();
    }

    // Converts an Unicode string into CP1251 byte array

    public static byte[] stringToByteArray1251(String s) {
        byte abyte0[] = s.getBytes();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case 1025:
                    abyte0[i] = -88;
                    break;
                case 1105:
                    abyte0[i] = -72;
                    break;

                /* Ukrainian CP1251 chars section */
                case 1168:
                    abyte0[i] = -91;
                    break;
                case 1028:
                    abyte0[i] = -86;
                    break;
                case 1031:
                    abyte0[i] = -81;
                    break;
                case 1030:
                    abyte0[i] = -78;
                    break;
                case 1110:
                    abyte0[i] = -77;
                    break;
                case 1169:
                    abyte0[i] = -76;
                    break;
                case 1108:
                    abyte0[i] = -70;
                    break;
                case 1111:
                    abyte0[i] = -65;
                    break;
                /* end of section */

                default:
                    char c1 = c;
                    if (c1 >= '\u0410' && c1 <= '\u044F') {
                        abyte0[i] = (byte) ((c1 - 1040) + 192);
                    }
                    break;
            }
        }
        return abyte0;
    }


    // Converts an CP1251 byte array into an Unicode string
    public static String byteArray1251ToString(byte abyte0[], int i, int j) {
        String s = new String(abyte0, i, j);
        StringBuffer stringbuffer = new StringBuffer(j);
        for (int k = 0; k < j; k++) {
            int l = abyte0[k + i] & 0xff;
            switch (l) {
                case 168:
                    stringbuffer.append('\u0401');
                    break;
                case 184:
                    stringbuffer.append('\u0451');
                    break;

                /* Ukrainian CP1251 chars section */
                case 165:
                    stringbuffer.append('\u0490');
                    break;
                case 170:
                    stringbuffer.append('\u0404');
                    break;
                case 175:
                    stringbuffer.append('\u0407');
                    break;
                case 178:
                    stringbuffer.append('\u0406');
                    break;
                case 179:
                    stringbuffer.append('\u0456');
                    break;
                case 180:
                    stringbuffer.append('\u0491');
                    break;
                case 186:
                    stringbuffer.append('\u0454');
                    break;
                case 191:
                    stringbuffer.append('\u0457');
                    break;
                /* end of section */

                default:
                    if (l >= 192 && l <= 255) {
                        stringbuffer.append((char) ((1040 + l) - 192));
                    } else {
                        stringbuffer.append(s.charAt(k));
                    }
                    break;
            }
        }
        return stringbuffer.toString();
    }


//	static public int strToIntDef(String str, int defValue)
//	{
//		if (str == null) return defValue;
//		int result = defValue;
//		try
//		{
//			result = Integer.parseInt(str);
//		}
//		catch (Exception e) {}
//		return result;
//	}
//
//
//	static public String replaceStr(String original, String from, String to)
//	{
//		int index = original.indexOf(from);
//		if (index == -1) return original;
//		return original.substring(0, index)+to+original.substring(index+from.length(), original.length());

    //	}

    final static public String make2(int i) {
        if (i < 10) {
            StringBuffer sb = new StringBuffer(3);
            sb.append('0').append(i);
            return sb.toString();
        } else {
            return String.valueOf(i);
        }
    }


    public static String getDateTimeString() {
        Calendar c = Calendar.getInstance();
        return "" + make2(c.get(Calendar.DAY_OF_MONTH)) + "." + make2(c.get(Calendar.MONTH) + 1) + "." + (c.get(Calendar.YEAR)) + " " + make2(c.get(Calendar.HOUR_OF_DAY)) + ":" + make2(c.get(Calendar.MINUTE)) + ":" + make2(c.get(Calendar.SECOND));

    }

    /**
     * @return 20.11.2017 22:30
     */
    public static String getDateTimeStringInMsk() {
        return HTTP_DATE_TIME_FORMATTER_MSK
                .print(System.currentTimeMillis());

        //Calendar c = Calendar.getInstance();
        //return "" + make2(c.get(Calendar.DAY_OF_MONTH)) + "." + make2(c.get(Calendar.MONTH) + 1) + "." + (c.get(Calendar.YEAR)) + " " + make2(c.get(Calendar.HOUR_OF_DAY)) + "." + make2(c.get(Calendar.MINUTE));

    }

    public static String getDateTimeString(long dt) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dt);
        return "" + make2(c.get(Calendar.DAY_OF_MONTH)) + "." + make2(c.get(Calendar.MONTH) + 1) + "." + (c.get(Calendar.YEAR)) + " " + make2(c.get(Calendar.HOUR_OF_DAY)) + ":" + make2(c.get(Calendar.MINUTE)) + ":" + make2(c.get(Calendar.SECOND));
    }

    public static String getDBDateTimeString(long dt) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dt);
        return "" + make2(c.get(Calendar.YEAR)) + "-" + make2(c.get(Calendar.MONTH) + 1) + "-" + (c.get(Calendar.DAY_OF_MONTH)) + " " + make2(c.get(Calendar.HOUR_OF_DAY)) + ":" + make2(c.get(Calendar.MINUTE)) + ":" + make2(c.get(Calendar.SECOND));
    }

    public static long getLocalUTCOffset() {
        Calendar calendarUTC = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendarUTC.setTime(new Date());
        Calendar calendarLoc = Calendar.getInstance(TimeZone.getDefault());

        calendarUTC.set(Calendar.HOUR_OF_DAY, 12);
        calendarUTC.set(Calendar.MINUTE, 0);
        calendarUTC.set(Calendar.SECOND, 0);
        calendarUTC.set(Calendar.DAY_OF_MONTH, 5);
        calendarUTC.set(Calendar.MONTH, 3);
        calendarUTC.set(Calendar.YEAR, 2004);
        calendarLoc.setTime(calendarUTC.getTime());

        int hour = calendarLoc.get(Calendar.HOUR_OF_DAY);
        int min = calendarLoc.get(Calendar.MINUTE);
        int sec = calendarLoc.get(Calendar.SECOND);
        return (hour - 12) * 3600000 + (min) * 60000 + (sec) * 1000;

    }



    public static final byte[] readStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(30000);
        int b;
        do {
            b = is.read();
            if (b < 0) {
                break;
            }
            baos.write(b);
        } while (true);
        return baos.toByteArray();
    }


    static public InputStream getInputStream(byte[] array) {
        return new ByteArrayInputStream(array);
    }


    static final char[] charTab =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    public static String encodeBase64(String data) {
        return encodeBase64(Util.stringToByteArray(data, true));
    }

    public static String encodeBase64(byte[] data) {
        return encodeBase64(data, 0, data.length, null).toString();
    }

    /**
     * Encodes the part of the given byte array denoted by start and
     * len to the Base64 format.  The encoded data is appended to the
     * given StringBuffer. If no StringBuffer is given, a new one is
     * created automatically. The StringBuffer is the return value of
     * this method.
     */
    public static StringBuffer encodeBase64(
            byte[] data,
            int start,
            int len,
            StringBuffer buf) {

        if (buf == null) {
            buf = new StringBuffer(data.length * 3 / 2);
        }
        int end = len - 3;
        int i = start;
        int n = 0;

        while (i <= end) {
            int d =
                    ((((int) data[i]) & 0x0ff) << 16) | ((((int) data[i + 1]) & 0x0ff) << 8) | (((int) data[i + 2]) & 0x0ff);

            buf.append(charTab[(d >> 18) & 63]);
            buf.append(charTab[(d >> 12) & 63]);
            buf.append(charTab[(d >> 6) & 63]);
            buf.append(charTab[d & 63]);

            i += 3;

            if (n++ >= 14) {
                n = 0;
                buf.append("\r\n");
            }
        }

        if (i == start + len - 2) {
            int d =
                    ((((int) data[i]) & 0x0ff) << 16) | ((((int) data[i + 1]) & 255) << 8);

            buf.append(charTab[(d >> 18) & 63]);
            buf.append(charTab[(d >> 12) & 63]);
            buf.append(charTab[(d >> 6) & 63]);
            buf.append("=");
        } else if (i == start + len - 1) {
            int d = (((int) data[i]) & 0x0ff) << 16;

            buf.append(charTab[(d >> 18) & 63]);
            buf.append(charTab[(d >> 12) & 63]);
            buf.append("==");
        }

        return buf;
    }

    private static int decode(char c) {
        if (c >= 'A' && c <= 'Z') {
            return ((int) c) - 65;
        } else if (c >= 'a' && c <= 'z') {
            return ((int) c) - 97 + 26;
        } else if (c >= '0' && c <= '9') {
            return ((int) c) - 48 + 26 + 26;
        } else {
            switch (c) {
                case '+':
                    return 62;
                case '/':
                    return 63;
                case '=':
                    return 0;
                default:
                    throw new RuntimeException(
                            "unexpected code: " + c);
            }
        }
    }

    /**
     * Decodes the given Base64 encoded String to a new byte array.
     * The byte array holding the decoded data is returned.
     */
    public static byte[] decodeBase64(String s) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            decodeBase64(s, bos);
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return bos.toByteArray();
    }

    public static void decodeBase64(String s, OutputStream os)
            throws IOException {
        int i = 0;

        int len = s.length();

        while (true) {
            while (i < len && s.charAt(i) <= ' ') {
                i++;
            }
            if (i == len) {
                break;
            }
            int tri =
                    (decode(s.charAt(i)) << 18) + (decode(s.charAt(i + 1)) << 12) + (decode(s.charAt(i + 2)) << 6) + (decode(s.charAt(i + 3)));

            os.write((tri >> 16) & 255);
            if (s.charAt(i + 2) == '=') {
                break;
            }
            os.write((tri >> 8) & 255);
            if (s.charAt(i + 3) == '=') {
                break;
            }
            os.write(tri & 255);

            i += 4;
        }
    }


    public final static String urlEncodeUnicode(String paramValue) {
        byte[] ba = Util.stringToByteArray(paramValue, true);
        StringBuffer sb = new StringBuffer(30);
        int c;
        for (int i = 0; i < ba.length; i++) {
            c = ba[i];
            sb.append((char) (c));
        }
        return urlEncodeStringAll(sb.toString());
    }

    public final static String urlEncodeStringAll(String url) {
        StringBuffer stringbuffer = new StringBuffer();
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            switch (c) {
                case 32: // ' '
                case 38: // '&'
                case 40: // '('
                case 41: // ')'
                case 58: // ':'
                case 63: // '?'
                case 64: // '@'
                    stringbuffer.append('%'); // Add % character
                    stringbuffer.append(toHexChar((c & 0xF0) >> 4));
                    stringbuffer.append(toHexChar(c & 0x0F));
                    break;
                default:
                    if ((c < 0x7b) && (c > 32)) {
                        stringbuffer.append(c);
                    } else {
                        stringbuffer.append('%'); // Add % character
                        stringbuffer.append(toHexChar((c & 0xF0) >> 4));
                        stringbuffer.append(toHexChar(c & 0x0F));
                    }
//          else {
//          stringbuffer.append('%'); // Add % character
//          stringbuffer.append(toHexChar((c & 0xF000) >> 12));
//          stringbuffer.append(toHexChar((c & 0xF00) >> 8));
//          stringbuffer.append(toHexChar((c & 0xF0) >> 4));
//          stringbuffer.append(toHexChar(c & 0x0F));
//          }
                    break;
            }
        }

        return stringbuffer.toString();
    }

}
