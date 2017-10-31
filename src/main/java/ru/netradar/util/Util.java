package ru.netradar.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public final class Util {

    private final static Logger LOG = Logger.getLogger(Util.class);

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
            return (char) ('0' + digitValue);
        else
            // Convert value 10-15 to A-F hex char
            return (char) ('A' + (digitValue - 10));
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
                    if (nr < 0) break;
                    baos.write(pb, 0, nr);
                }
                pb = null;

            } finally {
                try {
                    c.disconnect();
                } catch (Throwable t) {
                }
                try {
                    if (is != null) is.close();
                } catch (Throwable t) {
                }
            }
        } catch (Throwable e) {
            LOG.error("getHTTPContent: " + url + " : " + e.toString());
        }
        return baos;
    }

    public final static void sendGetHTTP(String url) throws IOException {
        InputStream is = null;
        HttpURLConnection c;
        //  try {
        c = getHttpConn(url);
        try {
            is = c.getInputStream();
            byte[] pb = new byte[512];
            int nr = 0;
            while (true) {
                nr = is.read(pb, 0, pb.length);
                if (nr < 0) break;
            }
        } finally {
            try {
                c.disconnect();
            } catch (Throwable t) {
            }
            try {
                if (is != null) is.close();
            } catch (Throwable t) {
            }
        }
        //  } catch (Throwable e) {
        // LogWriteThread.printlnlogER("send GetHTTP: "+url+" : "+e.toString());

        // }
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
                if (respCode != HttpURLConnection.HTTP_MOVED_PERM)
                    break;
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

    public final static String getHTTPContentAsString(String url) {
        return Util.byteArrayToString(getHTTPContent(url).toByteArray(), true);
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
        if (c >= '0' && c <= '9')
            return c - 48;
        if (c >= 'A' && c <= 'F')
            return (c - 65) + 10;
        if (c >= 'a' && c <= 'f')
            return (c - 97) + 10;
        else
            return 0;
    }
//
//  private static String urlDecode(String s, boolean flag) {
//    char ac[] = s.toCharArray();
//    StringBuffer stringbuffer = new StringBuffer();
//    for(int i = 0; i < ac.length; i++)
//      if(ac[i] == '%') {
//      if(i >= ac.length - 2)
//        break;
//      char c = '\0';
//      for(int j = 1; j <= 2; j++)
//        c = (char)((c <<= '\004') + _aCI(ac[i + j]));
//
//      stringbuffer.append(c);
//      i += 2;
//      } else
//        if(flag && ac[i] == '+')
//          stringbuffer.append(' ');
//        else
//          stringbuffer.append(ac[i]);
//
//    return stringbuffer.toString();
//  }
//
//  public static String _bStringvString(String s, boolean flag) {
//    String s1 = urlDecode(s, flag);
//    String as[] = {
//      "amp;quot;", "amp;apos;", "amp;", "lt;", "gt;", "quot;", "apos;", "circ;", "tilde;", "nbsp;"
//    };
//    char ac[] = {
//      '"', '\'', '&', '<', '>', '"', '\'', '^', '~', ' '
//    };
//    char ac1[] = s1.toCharArray();
//    StringBuffer stringbuffer = new StringBuffer();
//    label0:
//      for(int i = 0; i < ac1.length; i++)
//        if(ac1[i] == '&') {
//        if(i >= ac1.length - 3)
//          break;
//        String s2 = s1.substring(i + 1);
//        if(ac1[i + 1] == '#') {
//          int j;
//          if((j = s2.indexOf(';')) < 0)
//            break;
//          String s3 = s2.substring(1, j);
//          try {
//            char c = (char)Integer.parseInt(s3);
//            stringbuffer.append(c);
//          } catch(NumberFormatException _ex) {
//            break;
//          }
//          i += j + 1;
//          continue;
//        }
//        for(int k = 0; k < as.length; k++) {
//          if(!s2.startsWith(as[k]))
//            continue;
//          stringbuffer.append(ac[k]);
//          i += as[k].length();
//          continue label0;
//        }
//
//        stringbuffer.append(ac1[i]);
//        } else {
//        stringbuffer.append(ac1[i]);
//        }
//
//      return stringbuffer.toString();
//  }

//	public static String toHexString(byte[] b)
//	{
//		StringBuffer sb = new StringBuffer(b.length * 2);
//		for (int i = 0; i < b.length; i++)
//		{
//			//	look up high nibble char
//			sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
//
//			//	look up low nibble char
//			sb.append(hexChar[b[i] & 0x0f]);
//			sb.append(" ");
//			if ((i != 0) && ((i % 15) == 0)) sb.append("\n");
//		}
//		return sb.toString();
//	}
//
//	//	table to convert a nibble to a hex char.
//	private static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
//
////
//	// Extracts the byte from the buffer (buf) at position off
//	public static int getByte(byte[] buf, int off)
//	{
//		int val;
//		val = ((int) buf[off]) & 0x000000FF;
//		return (val);
//	}
//
//
//	// Puts the specified byte (val) into the buffer (buf) at position off
//	public static void putByte(byte[] buf, int off, int val)
//	{
//		buf[off] = (byte) (val & 0x000000FF);
//	}
//
//
//	// Extracts the word from the buffer (buf) at position off using the specified byte ordering (bigEndian)
//	public static int getWord(byte[] buf, int off, boolean bigEndian)
//	{
//		int val;
//		if (bigEndian)
//		{
//			val = (((int) buf[off]) << 8) & 0x0000FF00;
//			val |= (((int) buf[++off])) & 0x000000FF;
//		}
//		else   // Little endian
//		{
//			val = (((int) buf[off])) & 0x000000FF;
//			val |= (((int) buf[++off]) << 8) & 0x0000FF00;
//		}
//		return (val);
//	}
//

    static public DataInputStream getDataInputStream(byte[] array, int offset) {
        return new DataInputStream(new ByteArrayInputStream(array, offset, array.length - offset));
    }

    static public DataInputStream getDataInputStream(byte[] array) {
        return getDataInputStream(array, 0);
    }

//	static public int getWord(DataInputStream stream, boolean bigEndian) throws IOException
//	{
//		return bigEndian
//				?
//					stream.readUnsignedShort()
//				:
//					((int)stream.readByte()&0x00FF) | (((int)stream.readByte()<<8)&0xFF00);
//	}
//
//	static public String readAsciiz(DataInputStream stream) throws IOException
//	{
//		int len = Util.getWord(stream, false);
//		if (len == 0) return new String();
//		byte[] buffer = new byte[len];
//		stream.readFully(buffer);
//		return Util.byteArrayToString(buffer);
//	}

//	static public void writeWord(ByteArrayOutputStream stream, int value, boolean bigEndian)
//	{
//		if (bigEndian)
//		{
//			stream.write(((value&0xFF00)>>8)&0xFF);
//			stream.write(value&0xFF);
//		}
//		else
//		{
//			stream.write(value&0xFF);
//			stream.write(((value&0xFF00)>>8)&0xFF);
//		}
//	}

//	static public void writeByteArray(ByteArrayOutputStream stream, byte[] array)
//	{
//		try
//		{
//			stream.write(array);
//		}
//		catch (Exception e)
//		{
//			System.out.println("Util.writeByteArray: "+e.toString());
//		}
//	}

//	static public void writeDWord(ByteArrayOutputStream stream, int value, boolean bigEndian)
//	{
//		if (bigEndian)
//		{
//			stream.write(((value&0xFF000000)>>24)&0xFF);
//			stream.write(((value&0xFF0000)>>16)&0xFF);
//			stream.write(((value&0xFF00)>>8)&0xFF);
//			stream.write(value&0xFF);
//		}
//		else
//		{
//			stream.write(value&0xFF);
//			stream.write(((value&0xFF00)>>8)&0xFF);
//			stream.write(((value&0xFF0000)>>16)&0xFF);
//			stream.write(((value&0xFF000000)>>24)&0xFF);
//		}
//	}

//	static public void writeByte(ByteArrayOutputStream stream, int value)
//	{
//		stream.write(value);
//	}
//
//	static public void writeLenAndString(ByteArrayOutputStream stream, String value, boolean utf8)
//	{
//		byte[] raw = Util.stringToByteArray(value, utf8);
//		writeWord(stream, raw.length, true);
//		stream.write(raw, 0, raw.length);
//	}
//
//	static public void writeAsciizTLV(int type, ByteArrayOutputStream stream, String value)
//	{
//		writeWord(stream, type, true);
//		byte[] raw = Util.stringToByteArray(value);
//		writeWord(stream, raw.length+3, false);
//		writeWord(stream, raw.length+1, false);
//		stream.write(raw, 0, raw.length);
//		stream.write(0);
//	}
//
//	static public void writeTLV(int type, ByteArrayOutputStream stream, ByteArrayOutputStream data)
//	{
//		byte[] raw = data.toByteArray();
//		writeWord(stream, type, true);
//		writeWord(stream, raw.length, false);
//		stream.write(raw, 0, raw.length);
//	}

//	// Extracts the word from the buffer (buf) at position off using big endian byte ordering
//	public static int getWord(byte[] buf, int off)
//	{
//		return (Util.getWord(buf, off, true));
//	}


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


//	// Extracts the double from the buffer (buf) at position off using the specified byte ordering (bigEndian)
//	public static long getDWord(byte[] buf, int off, boolean bigEndian)
//	{
//		long val;
//		if (bigEndian)
//		{
//			val = (((long) buf[off]) << 24) & 0xFF000000;
//			val |= (((long) buf[++off]) << 16) & 0x00FF0000;
//			val |= (((long) buf[++off]) << 8) & 0x0000FF00;
//			val |= (((long) buf[++off])) & 0x000000FF;
//		}
//		else   // Little endian
//		{
//			val = (((long) buf[off])) & 0x000000FF;
//			val |= (((long) buf[++off]) << 8) & 0x0000FF00;
//			val |= (((long) buf[++off]) << 16) & 0x00FF0000;
//			val |= (((long) buf[++off]) << 24) & 0xFF000000;
//		}
//		return (val);
//	}


//	// Extracts the double from the buffer (buf) at position off using big endian byte ordering
//	public static long getDWord(byte[] buf, int off)
//	{
//		return (Util.getDWord(buf, off, true));
//	}


//	// Puts the specified double (val) into the buffer (buf) at position off using the specified byte ordering (bigEndian)
//	public static void putDWord(byte[] buf, int off, long val, boolean bigEndian)
//	{
//		if (bigEndian)
//		{
//			buf[off] = (byte) ((val >> 24) & 0x00000000000000FF);
//			buf[++off] = (byte) ((val >> 16) & 0x00000000000000FF);
//			buf[++off] = (byte) ((val >> 8) & 0x00000000000000FF);
//			buf[++off] = (byte) ((val) & 0x00000000000000FF);
//		}
//		else   // Little endian
//		{
//			buf[off] = (byte) ((val) & 0x00000000000000FF);
//			buf[++off] = (byte) ((val >> 8) & 0x00000000000000FF);
//			buf[++off] = (byte) ((val >> 16) & 0x00000000000000FF);
//			buf[++off] = (byte) ((val >> 24) & 0x00000000000000FF);
//		}
//	}
//
//
//	// Puts the specified double (val) into the buffer (buf) at position off using big endian byte ordering
//	public static void putDWord(byte[] buf, int off, long val)
//	{
//		Util.putDWord(buf, off, val, true);
//	}


//	// getTlv(byte[] buf, int off) => byte[]
//	public static byte[] getTlv(byte[] buf, int off)
//	{
//		if (off + 4 > buf.length) return (null);   // Length check (#1)
//		int length = Util.getWord(buf, off + 2);
//		if (off + 4 + length > buf.length) return (null);   // Length check (#2)
//		byte[] value = new byte[length];
//		System.arraycopy(buf, off + 4, value, 0, length);
//		return (value);
//	}


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


//	// Extracts a string from the buffer (buf) starting at position off, ending at position off+len
//	public static String byteArrayToString1251(byte[] buf, int off, int len)
//	{
//		return (Util.byteArrayToString(buf, off, len, false));
//	}


    // Converts the specified buffer (buf) to a string

    public static String byteArrayUTFToString(byte[] buf) {
        return Util.byteArrayToString(buf, 0, buf.length, true);
    }

    public static String byteArrayToString(byte[] buf, boolean utf8) {
        return (Util.byteArrayToString(buf, 0, buf.length, utf8));
    }


//	// Converts the specified buffer (buf) to a string
//	public static String byteArrayToString(byte[] buf)
//	{
//		return (Util.byteArrayToString(buf, 0, buf.length, false));
//	}

//	// Converts the specific 4 byte max buffer to an unsigned long
//	public static long byteArrayToLong(byte[] b)
//	{
//		long l = 0;
//	    l |= b[0] & 0xFF;
//	    l <<= 8;
//	    l |= b[1] & 0xFF;
//	    l <<= 8;
//	    if (b.length > 3)
//		{
//			l |= b[2] & 0xFF;
//			l <<= 8;
//			l |= b[3] & 0xFF;
//		}
//	    return l;
//	}

//	// Converts a byte array to a hex string
//    public static String byteArrayToHexString(byte[] buf) {
//        StringBuffer hexString = new StringBuffer(buf.length);
//        String hex;
//        for (int i = 0; i < buf.length; i++) {
//            hex = Integer.toHexString(0x0100 + (buf[i] & 0x00FF)).substring(1);
//            hexString.append((hex.length() < 2 ? "0" : "") + hex);
//        }
//        return hexString.toString();
//    }

    public static byte[] stringLatinToByteArray(String val) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            for (int i = 0; i < val.length(); i++)
                dos.writeByte((byte) val.charAt(i));

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


//	// Converts the specified string (val) to a byte array
//	public static byte[] stringToByteArray(String val)
//	{
//		return (Util.stringToByteArray(val, false));
//	}
//
//
//	// Converts the specified string to UCS-2BE
//	public static byte[] stringToUcs2beByteArray(String val)
//	{
//		byte[] ucs2be = new byte[val.length() * 2];
//		for (int i = 0; i < val.length(); i++)
//		{
//			Util.putWord(ucs2be, i * 2, (int) val.charAt(i));
//		}
//		return (ucs2be);
//	}


//	// Extract a UCS-2BE string from the specified buffer (buf) starting at position off, ending at position off+len
//	public static String ucs2beByteArrayToString(byte[] buf, int off, int len)
//	{
//
//		// Length check
//		if ((off + len > buf.length) || (buf.length % 2 != 0))
//		{
//			return (null);
//		}
//
//		// Convert
//		StringBuffer sb = new StringBuffer();
//		for (int i = off; i < off+len; i += 2)
//		{
//			sb.append((char) Util.getWord(buf, i));
//		}
//		return (sb.toString());
//
//	}


//	// Extracts a UCS-2BE string from the specified buffer (buf)
//	public static String ucs2beByteArrayToString(byte[] buf)
//	{
//		return (Util.ucs2beByteArrayToString(buf, 0, buf.length));
//	}

//	public static void showBytes(byte[] data)
//	{
//		StringBuffer buffer1 = new StringBuffer(), buffer2 = new StringBuffer();
//
//		for (int i = 0; i < data.length; i++)
//		{
//			int charaster = ((int)data[i])&0xFF;
//			buffer1.append(charaster < ' ' || charaster >= 128 ? '.' : (char)charaster);
//			String hex = Integer.toHexString(((int)data[i])&0xFF);
//			buffer2.append(hex.length() == 1 ? "0"+hex : hex);
//			buffer2.append(" ");
//
//			if (((i%16) == 15) || (i == (data.length-1)))
//			{
//				while (buffer2.length() < 16*3) buffer2.append(' ');
//				System.out.print(buffer2.toString());
//				System.out.println(buffer1.toString());
//
//				buffer1.setLength(0);
//				buffer2.setLength(0);
//			}
//		}
//		System.out.println();
//	}


//	// Removes all CR occurences
//	public static String removeCr(String val)
//	{
//		StringBuffer result = new StringBuffer();
//		for (int i = 0; i < val.length(); i++)
//		{
//			char chr = val.charAt(i);
//			if ((chr == 0) || (chr == '\r')) continue;
//			result.append(chr);
//		}
//		return result.toString();
//	}

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
            if (chr == '\r') continue;
            if (chr == '\n') result.append("\r\n");
            else result.append(chr);
        }
        return result.toString();
    }
//
//	public static String removeClRfAndTabs(String val)
//	{
//		int len = val.length();
//		char[] dst = new char[len];
//		for (int i = 0; i < len; i++)
//		{
//			char chr = val.charAt(i);
//			if ((chr == '\n') || (chr == '\r') || (chr == '\t')) chr = ' ';
//			dst[i] = chr;
//		}
//		return new String(dst, 0, len);
//	}


//	// Compare to byte arrays (return true if equals, false otherwise)
//	public static boolean byteArrayEquals(byte[] buf1, int off1, byte[] buf2, int off2, int len)
//	{
//
//		// Length check
//		if ((off1 + len > buf1.length) || (off2 + len > buf2.length))
//		{
//			return (false);
//		}
//
//		// Compare bytes, stop at first mismatch
//		for (int i = 0; i < len; i++)
//		{
//			if (buf1[off1 + i] != buf2[off2 + i])
//			{
//				return (false);
//			}
//		}
//
//		// Return true if this point is reached
//		return (true);
//
//	}


//
//	//  If the numer has only one digit add a 0
//	public static String makeTwo(int number)
//	{
//		if (number < 10)
//		{
//			return ("0" + String.valueOf(number));
//		}
//		else
//		{
//			return (String.valueOf(number));
//		}
//	}
//

    // #sijapp cond.end #

//
//    // Check is data array utf-8 string
//    public static boolean isDataUTF8(byte[] array, int start, int lenght)
//    {
//        if (lenght == 0) return false;
//        if (array.length < (start + lenght)) return false;
//
//        for (int i = start, len = lenght; len > 0;)
//        {
//            int seqLen = 0;
//            byte bt = array[i++];
//            len--;
//
//            if      ((bt&0xE0) == 0xC0) seqLen = 1;
//            else if ((bt&0xF0) == 0xE0) seqLen = 2;
//            else if ((bt&0xF8) == 0xF0) seqLen = 3;
//            else if ((bt&0xFC) == 0xF8) seqLen = 4;
//            else if ((bt&0xFE) == 0xFC) seqLen = 5;
//
//            if (seqLen == 0)
//            {
//                if ((bt&0x80) == 0x80) return false;
//                else continue;
//            }
//
//            for (int j = 0; j < seqLen; j++)
//            {
//                if (len == 0) return false;
//                bt = array[i++];
//                if ((bt&0xC0) != 0x80) return false;
//                len--;
//            }
//            if (len == 0) break;
//        }
//        return true;
//    }

//	// Returns String value of cost value
//	public static String intToDecimal(int value)
//	{
//		String costString = "";
//		String afterDot = "";
//		try
//		{
//			if (value != 0) {
//				costString = Integer.toString(value / 1000) + ".";
//				afterDot = Integer.toString(value % 1000);
//				while (afterDot.length() != 3)
//				{
//					afterDot = "0" + afterDot;
//				}
//				while ((afterDot.endsWith("0")) && (afterDot.length() > 2))
//				{
//					afterDot = afterDot.substring(0, afterDot.length() - 1);
//				}
//				costString = costString + afterDot;
//				return costString;
//			}
//			else
//			{
//				return new String("0.0");
//			}
//		}
//		catch (Exception e)
//		{
//			return new String("0.0");
//		}
//	}
//
//	// Extracts the number value form String
//	public static int decimalToInt(String string)
//	{
//		int value = 0;
//		byte i = 0;
//		char c = new String(".").charAt(0);
//		try
//		{
//			for (i = 0; i < string.length(); i++)
//			{
//				if (c != string.charAt(i))
//				{
//					break;
//				}
//			}
//			if (i == string.length()-1)
//			{
//				value = Integer.parseInt(string) * 1000;
//				return (value);
//			}
//			else
//			{
//				while (c != string.charAt(i))
//				{
//					i++;
//				}
//				value = Integer.parseInt(string.substring(0, i)) * 1000;
//				string = string.substring(i + 1, string.length());
//				while (string.length() > 3)
//				{
//					string = string.substring(0, string.length() - 1);
//				}
//				while (string.length() < 3)
//				{
//					string = string + "0";
//				}
//				value = value + Integer.parseInt(string);
//				return value;
//			}
//		}
//		catch (Exception e)
//		{
//			return (0);
//		}
//	}


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
        } else return String.valueOf(i);
    }


    public static String getDateTimeString() {
        Calendar c = Calendar.getInstance();
        return "" + make2(c.get(Calendar.DAY_OF_MONTH)) + "." + make2(c.get(Calendar.MONTH) + 1) + "." + (c.get(Calendar.YEAR)) + " " + make2(c.get(Calendar.HOUR_OF_DAY)) + ":" + make2(c.get(Calendar.MINUTE)) + ":" + make2(c.get(Calendar.SECOND));

    }

    public static String getHTTPDateTimeString() {
        Calendar c = Calendar.getInstance();
        return "" + make2(c.get(Calendar.DAY_OF_MONTH)) + "." + make2(c.get(Calendar.MONTH) + 1) + "." + (c.get(Calendar.YEAR)) + " " + make2(c.get(Calendar.HOUR_OF_DAY)) + "." + make2(c.get(Calendar.MINUTE));

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


//  private static String urlDecode(String s, boolean flag) {
//    char ac[] = s.toCharArray();
//    StringBuffer stringbuffer = new StringBuffer();
//    for(int i = 0; i < ac.length; i++)
//      if(ac[i] == '%') {
//      if(i >= ac.length - 2)
//        break;
//      char c = '\0';
//      for(int j = 1; j <= 2; j++)
//        c = (char)((c <<= '\004') + _aCI(ac[i + j]));
//
//      stringbuffer.append(c);
//      i += 2;
//      } else
//        if(flag && ac[i] == '+')
//          stringbuffer.append(' ');
//        else
//          stringbuffer.append(ac[i]);
//
//    return stringbuffer.toString();
//  }
//
//  public static String _bStringvString(String s, boolean flag) {
//    String s1 = urlDecode(s, flag);
//    String as[] = {
//      "amp;quot;", "amp;apos;", "amp;", "lt;", "gt;", "quot;", "apos;", "circ;", "tilde;", "nbsp;"
//    };
//    char ac[] = {
//      '"', '\'', '&', '<', '>', '"', '\'', '^', '~', ' '
//    };
//    char ac1[] = s1.toCharArray();
//    StringBuffer stringbuffer = new StringBuffer();
//    label0:
//      for(int i = 0; i < ac1.length; i++)
//        if(ac1[i] == '&') {
//        if(i >= ac1.length - 3)
//          break;
//        String s2 = s1.substring(i + 1);
//        if(ac1[i + 1] == '#') {
//          int j;
//          if((j = s2.indexOf(';')) < 0)
//            break;
//          String s3 = s2.substring(1, j);
//          try {
//            char c = (char)Integer.parseInt(s3);
//            stringbuffer.append(c);
//          } catch(NumberFormatException _ex) {
//            break;
//          }
//          i += j + 1;
//          continue;
//        }
//        for(int k = 0; k < as.length; k++) {
//          if(!s2.startsWith(as[k]))
//            continue;
//          stringbuffer.append(ac[k]);
//          i += as[k].length();
//          continue label0;
//        }
//
//        stringbuffer.append(ac1[i]);
//        } else {
//        stringbuffer.append(ac1[i]);
//        }
//
//      return stringbuffer.toString();
//  }
//	public static String toHexString(byte[] b)
//	{
//		StringBuffer sb = new StringBuffer(b.length * 2);
//		for (int i = 0; i < b.length; i++)
//		{
//			//	look up high nibble char
//			sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
//
//			//	look up low nibble char
//			sb.append(hexChar[b[i] & 0x0f]);
//			sb.append(" ");
//			if ((i != 0) && ((i % 15) == 0)) sb.append("\n");
//		}
//		return sb.toString();
//	}
//
//	//	table to convert a nibble to a hex char.
//	private static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
//
////
//	// Extracts the byte from the buffer (buf) at position off
//	public static int getByte(byte[] buf, int off)
//	{
//		int val;
//		val = ((int) buf[off]) & 0x000000FF;
//		return (val);
//	}
//
//
//	// Puts the specified byte (val) into the buffer (buf) at position off
//	public static void putByte(byte[] buf, int off, int val)
//	{
//		buf[off] = (byte) (val & 0x000000FF);
//	}
//
//
//	// Extracts the word from the buffer (buf) at position off using the specified byte ordering (bigEndian)
//	public static int getWord(byte[] buf, int off, boolean bigEndian)
//	{
//		int val;
//		if (bigEndian)
//		{
//			val = (((int) buf[off]) << 8) & 0x0000FF00;
//			val |= (((int) buf[++off])) & 0x000000FF;
//		}
//		else   // Little endian
//		{
//			val = (((int) buf[off])) & 0x000000FF;
//			val |= (((int) buf[++off]) << 8) & 0x0000FF00;
//		}
//		return (val);
//	}
//


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
//	static public int getWord(DataInputStream stream, boolean bigEndian) throws IOException
//	{
//		return bigEndian
//				?
//					stream.readUnsignedShort()
//				:
//					((int)stream.readByte()&0x00FF) | (((int)stream.readByte()<<8)&0xFF00);
//	}
//
//	static public String readAsciiz(DataInputStream stream) throws IOException
//	{
//		int len = Util.getWord(stream, false);
//		if (len == 0) return new String();
//		byte[] buffer = new byte[len];
//		stream.readFully(buffer);
//		return Util.byteArrayToString(buffer);
//	}
//	static public void writeWord(ByteArrayOutputStream stream, int value, boolean bigEndian)
//	{
//		if (bigEndian)
//		{
//			stream.write(((value&0xFF00)>>8)&0xFF);
//			stream.write(value&0xFF);
//		}
//		else
//		{
//			stream.write(value&0xFF);
//			stream.write(((value&0xFF00)>>8)&0xFF);
//		}
//	}
//	static public void writeByteArray(ByteArrayOutputStream stream, byte[] array)
//	{
//		try
//		{
//			stream.write(array);
//		}
//		catch (Exception e)
//		{
//			System.out.println("Util.writeByteArray: "+e.toString());
//		}
//	}
//	static public void writeDWord(ByteArrayOutputStream stream, int value, boolean bigEndian)
//	{
//		if (bigEndian)
//		{
//			stream.write(((value&0xFF000000)>>24)&0xFF);
//			stream.write(((value&0xFF0000)>>16)&0xFF);
//			stream.write(((value&0xFF00)>>8)&0xFF);
//			stream.write(value&0xFF);
//		}
//		else
//		{
//			stream.write(value&0xFF);
//			stream.write(((value&0xFF00)>>8)&0xFF);
//			stream.write(((value&0xFF0000)>>16)&0xFF);
//			stream.write(((value&0xFF000000)>>24)&0xFF);
//		}
//	}
//	static public void writeByte(ByteArrayOutputStream stream, int value)
//	{
//		stream.write(value);
//	}
//
//	static public void writeLenAndString(ByteArrayOutputStream stream, String value, boolean utf8)
//	{
//		byte[] raw = Util.stringToByteArray(value, utf8);
//		writeWord(stream, raw.length, true);
//		stream.write(raw, 0, raw.length);
//	}
//
//	static public void writeAsciizTLV(int type, ByteArrayOutputStream stream, String value)
//	{
//		writeWord(stream, type, true);
//		byte[] raw = Util.stringToByteArray(value);
//		writeWord(stream, raw.length+3, false);
//		writeWord(stream, raw.length+1, false);
//		stream.write(raw, 0, raw.length);
//		stream.write(0);
//	}
//
//	static public void writeTLV(int type, ByteArrayOutputStream stream, ByteArrayOutputStream data)
//	{
//		byte[] raw = data.toByteArray();
//		writeWord(stream, type, true);
//		writeWord(stream, raw.length, false);
//		stream.write(raw, 0, raw.length);
//	}
//	// Extracts the word from the buffer (buf) at position off using big endian byte ordering
//	public static int getWord(byte[] buf, int off)
//	{
//		return (Util.getWord(buf, off, true));
//	}
    // Puts the specified word (val) into the buffer (buf) at position off using the specified byte ordering (bigEndian)

//	// Extracts the double from the buffer (buf) at position off using the specified byte ordering (bigEndian)
//	public static long getDWord(byte[] buf, int off, boolean bigEndian)
//	{
//		long val;
//		if (bigEndian)
//		{
//			val = (((long) buf[off]) << 24) & 0xFF000000;
//			val |= (((long) buf[++off]) << 16) & 0x00FF0000;
//			val |= (((long) buf[++off]) << 8) & 0x0000FF00;
//			val |= (((long) buf[++off])) & 0x000000FF;
//		}
//		else   // Little endian
//		{
//			val = (((long) buf[off])) & 0x000000FF;
//			val |= (((long) buf[++off]) << 8) & 0x0000FF00;
//			val |= (((long) buf[++off]) << 16) & 0x00FF0000;
//			val |= (((long) buf[++off]) << 24) & 0xFF000000;
//		}
//		return (val);
//	}
//	// Extracts the double from the buffer (buf) at position off using big endian byte ordering
//	public static long getDWord(byte[] buf, int off)
//	{
//		return (Util.getDWord(buf, off, true));
//	}
//	// Puts the specified double (val) into the buffer (buf) at position off using the specified byte ordering (bigEndian)
//	public static void putDWord(byte[] buf, int off, long val, boolean bigEndian)
//	{
//		if (bigEndian)
//		{
//			buf[off] = (byte) ((val >> 24) & 0x00000000000000FF);
//			buf[++off] = (byte) ((val >> 16) & 0x00000000000000FF);
//			buf[++off] = (byte) ((val >> 8) & 0x00000000000000FF);
//			buf[++off] = (byte) ((val) & 0x00000000000000FF);
//		}
//		else   // Little endian
//		{
//			buf[off] = (byte) ((val) & 0x00000000000000FF);
//			buf[++off] = (byte) ((val >> 8) & 0x00000000000000FF);
//			buf[++off] = (byte) ((val >> 16) & 0x00000000000000FF);
//			buf[++off] = (byte) ((val >> 24) & 0x00000000000000FF);
//		}
//	}
//
//
//	// Puts the specified double (val) into the buffer (buf) at position off using big endian byte ordering
//	public static void putDWord(byte[] buf, int off, long val)
//	{
//		Util.putDWord(buf, off, val, true);
//	}
//	// getTlv(byte[] buf, int off) => byte[]
//	public static byte[] getTlv(byte[] buf, int off)
//	{
//		if (off + 4 > buf.length) return (null);   // Length check (#1)
//		int length = Util.getWord(buf, off + 2);
//		if (off + 4 + length > buf.length) return (null);   // Length check (#2)
//		byte[] value = new byte[length];
//		System.arraycopy(buf, off + 4, value, 0, length);
//		return (value);
//	}
    // Extracts a string from the buffer (buf) starting at position off, ending at position off+len

//	// Extracts a string from the buffer (buf) starting at position off, ending at position off+len
//	public static String byteArrayToString1251(byte[] buf, int off, int len)
//	{
//		return (Util.byteArrayToString(buf, off, len, false));
//	}
    // Converts the specified buffer (buf) to a string

//	// Converts the specified buffer (buf) to a string
//	public static String byteArrayToString(byte[] buf)
//	{
//		return (Util.byteArrayToString(buf, 0, buf.length, false));
//	}
//	// Converts the specific 4 byte max buffer to an unsigned long
//	public static long byteArrayToLong(byte[] b)
//	{
//		long l = 0;
//	    l |= b[0] & 0xFF;
//	    l <<= 8;
//	    l |= b[1] & 0xFF;
//	    l <<= 8;
//	    if (b.length > 3)
//		{
//			l |= b[2] & 0xFF;
//			l <<= 8;
//			l |= b[3] & 0xFF;
//		}
//	    return l;
//	}
//	// Converts a byte array to a hex string
//    public static String byteArrayToHexString(byte[] buf) {
//        StringBuffer hexString = new StringBuffer(buf.length);
//        String hex;
//        for (int i = 0; i < buf.length; i++) {
//            hex = Integer.toHexString(0x0100 + (buf[i] & 0x00FF)).substring(1);
//            hexString.append((hex.length() < 2 ? "0" : "") + hex);
//        }
//        return hexString.toString();
//    }

//	// Converts the specified string (val) to a byte array
//	public static byte[] stringToByteArray(String val)
//	{
//		return (Util.stringToByteArray(val, false));
//	}
//
//
//	// Converts the specified string to UCS-2BE
//	public static byte[] stringToUcs2beByteArray(String val)
//	{
//		byte[] ucs2be = new byte[val.length() * 2];
//		for (int i = 0; i < val.length(); i++)
//		{
//			Util.putWord(ucs2be, i * 2, (int) val.charAt(i));
//		}
//		return (ucs2be);
//	}
//	// Extract a UCS-2BE string from the specified buffer (buf) starting at position off, ending at position off+len
//	public static String ucs2beByteArrayToString(byte[] buf, int off, int len)
//	{
//
//		// Length check
//		if ((off + len > buf.length) || (buf.length % 2 != 0))
//		{
//			return (null);
//		}
//
//		// Convert
//		StringBuffer sb = new StringBuffer();
//		for (int i = off; i < off+len; i += 2)
//		{
//			sb.append((char) Util.getWord(buf, i));
//		}
//		return (sb.toString());
//
//	}
//	// Extracts a UCS-2BE string from the specified buffer (buf)
//	public static String ucs2beByteArrayToString(byte[] buf)
//	{
//		return (Util.ucs2beByteArrayToString(buf, 0, buf.length));
//	}
//	public static void showBytes(byte[] data)
//	{
//		StringBuffer buffer1 = new StringBuffer(), buffer2 = new StringBuffer();
//
//		for (int i = 0; i < data.length; i++)
//		{
//			int charaster = ((int)data[i])&0xFF;
//			buffer1.append(charaster < ' ' || charaster >= 128 ? '.' : (char)charaster);
//			String hex = Integer.toHexString(((int)data[i])&0xFF);
//			buffer2.append(hex.length() == 1 ? "0"+hex : hex);
//			buffer2.append(" ");
//
//			if (((i%16) == 15) || (i == (data.length-1)))
//			{
//				while (buffer2.length() < 16*3) buffer2.append(' ');
//				System.out.print(buffer2.toString());
//				System.out.println(buffer1.toString());
//
//				buffer1.setLength(0);
//				buffer2.setLength(0);
//			}
//		}
//		System.out.println();
//	}
//	// Removes all CR occurences
//	public static String removeCr(String val)
//	{
//		StringBuffer result = new StringBuffer();
//		for (int i = 0; i < val.length(); i++)
//		{
//			char chr = val.charAt(i);
//			if ((chr == 0) || (chr == '\r')) continue;
//			result.append(chr);
//		}
//		return result.toString();
//	}
//
//
//	public static String removeClRfAndTabs(String val)
//	{
//		int len = val.length();
//		char[] dst = new char[len];
//		for (int i = 0; i < len; i++)
//		{
//			char chr = val.charAt(i);
//			if ((chr == '\n') || (chr == '\r') || (chr == '\t')) chr = ' ';
//			dst[i] = chr;
//		}
//		return new String(dst, 0, len);
//	}
//	// Compare to byte arrays (return true if equals, false otherwise)
//	public static boolean byteArrayEquals(byte[] buf1, int off1, byte[] buf2, int off2, int len)
//	{
//
//		// Length check
//		if ((off1 + len > buf1.length) || (off2 + len > buf2.length))
//		{
//			return (false);
//		}
//
//		// Compare bytes, stop at first mismatch
//		for (int i = 0; i < len; i++)
//		{
//			if (buf1[off1 + i] != buf2[off2 + i])
//			{
//				return (false);
//			}
//		}
//
//		// Return true if this point is reached
//		return (true);
//
//	}
//
//	//  If the numer has only one digit add a 0
//	public static String makeTwo(int number)
//	{
//		if (number < 10)
//		{
//			return ("0" + String.valueOf(number));
//		}
//		else
//		{
//			return (String.valueOf(number));
//		}
//	}
//
    // #sijapp cond.end #
//
//    // Check is data array utf-8 string
//    public static boolean isDataUTF8(byte[] array, int start, int lenght)
//    {
//        if (lenght == 0) return false;
//        if (array.length < (start + lenght)) return false;
//
//        for (int i = start, len = lenght; len > 0;)
//        {
//            int seqLen = 0;
//            byte bt = array[i++];
//            len--;
//
//            if      ((bt&0xE0) == 0xC0) seqLen = 1;
//            else if ((bt&0xF0) == 0xE0) seqLen = 2;
//            else if ((bt&0xF8) == 0xF0) seqLen = 3;
//            else if ((bt&0xFC) == 0xF8) seqLen = 4;
//            else if ((bt&0xFE) == 0xFC) seqLen = 5;
//
//            if (seqLen == 0)
//            {
//                if ((bt&0x80) == 0x80) return false;
//                else continue;
//            }
//
//            for (int j = 0; j < seqLen; j++)
//            {
//                if (len == 0) return false;
//                bt = array[i++];
//                if ((bt&0xC0) != 0x80) return false;
//                len--;
//            }
//            if (len == 0) break;
//        }
//        return true;
//    }
//	// Returns String value of cost value
//	public static String intToDecimal(int value)
//	{
//		String costString = "";
//		String afterDot = "";
//		try
//		{
//			if (value != 0) {
//				costString = Integer.toString(value / 1000) + ".";
//				afterDot = Integer.toString(value % 1000);
//				while (afterDot.length() != 3)
//				{
//					afterDot = "0" + afterDot;
//				}
//				while ((afterDot.endsWith("0")) && (afterDot.length() > 2))
//				{
//					afterDot = afterDot.substring(0, afterDot.length() - 1);
//				}
//				costString = costString + afterDot;
//				return costString;
//			}
//			else
//			{
//				return new String("0.0");
//			}
//		}
//		catch (Exception e)
//		{
//			return new String("0.0");
//		}
//	}
//
//	// Extracts the number value form String
//	public static int decimalToInt(String string)
//	{
//		int value = 0;
//		byte i = 0;
//		char c = new String(".").charAt(0);
//		try
//		{
//			for (i = 0; i < string.length(); i++)
//			{
//				if (c != string.charAt(i))
//				{
//					break;
//				}
//			}
//			if (i == string.length()-1)
//			{
//				value = Integer.parseInt(string) * 1000;
//				return (value);
//			}
//			else
//			{
//				while (c != string.charAt(i))
//				{
//					i++;
//				}
//				value = Integer.parseInt(string.substring(0, i)) * 1000;
//				string = string.substring(i + 1, string.length());
//				while (string.length() > 3)
//				{
//					string = string.substring(0, string.length() - 1);
//				}
//				while (string.length() < 3)
//				{
//					string = string + "0";
//				}
//				value = value + Integer.parseInt(string);
//				return value;
//			}
//		}
//		catch (Exception e)
//		{
//			return (0);
//		}
//	}
    // Converts an Unicode string into CP1251 byte array


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
        byte[] ba=Util.stringToByteArray(paramValue, true);
        StringBuffer sb=new StringBuffer(30);
        int c;
        for (int i=0; i<ba.length; i++) {
            c=ba[i];
            sb.append((char) (c));
        }
        return urlEncodeStringAll(sb.toString());
    }

    public final static String urlEncodeStringAll(String url) {
            StringBuffer stringbuffer=new StringBuffer();
            for (int i=0; i<url.length(); i++) {
                char c=url.charAt(i);
                switch (c) {
                    case 32: // ' '
                    case 38: // '&'
                    case 40: // '('
                    case 41: // ')'
                    case 58: // ':'
                    case 63: // '?'
                    case 64: // '@'
                        stringbuffer.append('%'); // Add % character
                        stringbuffer.append(toHexChar((c&0xF0)>>4));
                        stringbuffer.append(toHexChar(c&0x0F));
                        break;
                    default:
                        if ((c<0x7b)&&(c>32)){
                            stringbuffer.append(c);
                        } else {
                            stringbuffer.append('%'); // Add % character
                            stringbuffer.append(toHexChar((c&0xF0)>>4));
                            stringbuffer.append(toHexChar(c&0x0F));
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
