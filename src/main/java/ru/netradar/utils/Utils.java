package ru.netradar.utils;


import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Утилитарные функции для исключения шаблонного кода
 * Eliminate boilerplate code with moving useful pieces here!
 *
 * @author Vladimir Zhukov
 * @author Pavel Raev
 */
public class Utils {
    private static final Logger LOG = Logger.getLogger(Utils.class);

    /**
     * Количество миллисекунд в секунде
     */
    public static final long MILLISECONDS_PER_SECOND = 1000L;

    /**
     * Количество миллисекунд в часе
     */
    public static final long MILLISECONDS_PER_HOUR = 60 * 60 * MILLISECONDS_PER_SECOND;

    /**
     * Количество миллисекунд в сутках
     */
    public static final long MILLISECONDS_PER_DAY = 24 * MILLISECONDS_PER_HOUR;

    /**
     * Паттерн, описывающий sha256/hex-строки
     */
    private static final Pattern SHA256_HEX_PATTERN = Pattern.compile("[0-9a-fA-F]{64}");

    /**
     * Кодировка по-умолчанию, в которую переводим строки в приложении
     */
    public static final Charset DEFAULT_CHARSET = Charset.forName("utf8");

    /**
     * Паттерн, описывающий строку, которая считается валидным почтовым адресом
     */
    private static final Pattern EMAIL = Pattern.compile("([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,6}|[0-9]{1,3})(\\]?)$");

    /**
     * Паттерн, описывающий строку, которая считается валидным полным URI
     */
    private static final Pattern FULL_URI =
            Pattern.compile("(news|(ht|f)tp(s?))://[\\w\\-_]+(\\.?[\\w\\-_]+)+([\\w\\-\\.?,@?^=%&amp;:/~\\+#_]*[\\w\\-@?^=%&amp;/~\\+#_])?");

    private Utils() {
    }

    /**
     * Получить массив байт из потока
     * Переданный поток будет обязательно закрыт
     * Функция предназначена для чтения небольших данных (единиц-десятков килобайт),
     * например PGP ключей, XSD схем и т.п.
     * Для больших объемов данных использовать ее нельзя.
     *
     * @param is входной поток
     * @return массив байт из потока
     * @throws java.io.IOException в случае ошибки чтения потока
     */
    public static byte[] getBytes(InputStream is) throws IOException {
        if (is == null) {
            throw new IllegalArgumentException("Input stream is null");
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
            byte[] buf = new byte[4096];
            int numRead;
            while ((numRead = is.read(buf)) != -1) {
                baos.write(buf, 0, numRead);
            }
            return baos.toByteArray();
        } finally {
            is.close();
        }
    }

    /**
     * Получить массив байт указанного размера из потока
     * Будет считана указанная порция и переданный поток НЕ будет закрыт
     *
     * @param is     входной поток
     * @param length длина порции для считывания
     * @return массив байт из потока
     * @throws java.io.IOException в случае ошибки чтения потока
     */
    public static byte[] getBytes(InputStream is, int length) throws IOException {
        byte[] buf = new byte[length];
        int numRead;
        int offs = 0;
        while ((length > 0) && ((numRead = is.read(buf, offs, length)) != -1)) {
            length -= numRead;
            offs += numRead;
        }
        if (length > 0)
            throw new IOException("Stream is exhausted");
        return buf;
    }

    /**
     * Получить содержимое потока как строку
     * Переданный поток будет обязательно закрыт
     *
     * @param is      входной поток
     * @param charset название кодировки
     * @return строку из потока
     * @throws java.io.IOException в случае ошибки чтения потока
     */
    public static String getString(InputStream is, Charset charset) throws IOException {
        return new String(Utils.getBytes(is), charset);
    }

    /**
     * Получить часть содержимого потока как строку
     * Будет считана указанная часть и переданный поток НЕ будет закрыт
     *
     * @param is      входной поток
     * @param length  длина порции для считывания
     * @param charset название кодировки
     * @return строку из потока
     * @throws java.io.IOException в случае ошибки чтения потока
     */
    public static String getString(InputStream is, int length, Charset charset) throws IOException {
        return new String(Utils.getBytes(is, length), charset);
    }

    /**
     * Сохраняет переданный поток в файл
     * Переданный поток будет обязательно закрыт
     *
     * @param is       поток для сохранения
     * @param filename имя файла для сохранения
     * @throws IOException в случае ошибки доступа к файлу или чтения из переданного потока
     */
    public static void writeStreamToFile(InputStream is, String filename) throws IOException {
        final FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            try {
                is.close();
            } catch (IOException e1) {
                LOG.error(e1);
            }
            throw e;
        }
        try {
            fileOutputStream.write(getBytes(is));
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                LOG.error(e);
            }
        }
    }

    /**
     * Возвращает MD5 hash от строки
     *
     * @param src входной массив байт
     * @return MD5 hash в виде массива байт
     * @throws NoSuchAlgorithmException при отсутствии MD5 алгоритма
     */
    public static byte[] getMD5Hash(final byte[] src) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return md.digest(src);
    }

    /**
     * Получить MD5 hash в виде строки Base64 из входной строки
     *
     * @param src входной массив байт
     * @return MD5 hash в виде строки Base64
     * @throws NoSuchAlgorithmException при отсутствии MD5 алгоритма
     */

    public static String getMD5HashInBase64Str(final byte[] src) throws NoSuchAlgorithmException {
        final byte[] md5Byte = getMD5Hash(src);
        return Base64.byteArrayToBase64(md5Byte);
    }

    /**
     * Получить MD5 hash в виде строки Base64 из входного массива байт
     *
     * @param src входной массив байт
     * @return MD5 hash в виде строки Base64
     * @throws NoSuchAlgorithmException при отсутствии MD5 алгоритма
     */
    public static String getMD5HashInHexStr(final byte[] src) throws NoSuchAlgorithmException {
        final byte[] md5Byte = getMD5Hash(src);
        final StringBuilder hexStr = new StringBuilder();
        for (final byte aMd5Byte : md5Byte) {
            String byteHex = Integer.toHexString(aMd5Byte).toUpperCase();
            if (byteHex.length() > 1) {
                byteHex = byteHex.substring(byteHex.length() - 2, byteHex.length());
            } else {
                byteHex = "0" + byteHex;
            }
            hexStr.append(byteHex);
        }
        return hexStr.toString();
    }

    /**
     * Возвращает SHA-1 hash от строки
     *
     * @param src входной массив байт
     * @return SHA-1 hash в виде массива байт
     * @throws NoSuchAlgorithmException при отсутствии SHA-1 алгоритма
     */
    public static byte[] getSHA1Hash(final byte[] src) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        return sha1.digest(src);
    }

    /**
     * Получить SHA-1 hash в виде строки Base64 из входного массива байт
     *
     * @param src входной массив байт
     * @return SHA-1 hash в виде строки Base64
     * @throws NoSuchAlgorithmException при отсутствии SHA-1 алгоритма
     */
    public static String getSHA1HashInBase64Str(final byte[] src) throws NoSuchAlgorithmException {
        final byte[] md5Byte = getSHA1Hash(src);
        return Base64.byteArrayToBase64(md5Byte);
    }

    /**
     * Проверка, что строка является sha256/hex совместимой
     *
     * @param test тестируемая строка
     * @return true, если строка является sha256/hax совместимой, иначе false
     */
    public static boolean isSha256HexString(String test) {
        Matcher matcher = Utils.SHA256_HEX_PATTERN.matcher(test);
        return matcher.matches();
    }

    /**
     * Проверка, что строка является валидным почтовым адресом
     *
     * @param value проверяемая строка
     * @return true, если строка является валидным почтовым адресом, иначе false
     * @throws NullPointerException если проверяемая строка не задана
     */
    public static boolean isValidEmail(String value) {
        if (value == null)
            throw new NullPointerException("Проверяемая строка не задана");
        return EMAIL.matcher(value).matches();
    }

    /**
     * Проверка, что строка является валидным полным URI
     *
     * @param value проверяемая строка
     * @return true, если строка является валидным полным URI, иначе false
     * @throws NullPointerException если проверяемая строка не задана
     */
    public static boolean isValidFullURI(String value) {
        if (value == null)
            throw new NullPointerException("Проверяемая строка не задана");
        return FULL_URI.matcher(value).matches();
    }

    /**
     * Обрезает строку до максимальной длины.
     *
     * @param aStr      строка
     * @param maxLength максимальная длина.
     * @return обрезанная строка.
     * @throws NullPointerException если строка не определена.
     */
    public static String cutString(String aStr, int maxLength) {
        if (aStr == null)
            throw new NullPointerException("aStr == null");
        maxLength = maxLength > aStr.length() ? aStr.length() : maxLength;

        return aStr.substring(0, maxLength);
    }

    /**
     * Преобразует строку в булево значение, значение строки должно быть либо 'true', либо 'false'.
     *
     * @param value строка
     * @return булево значение
     * @throws IllegalArgumentException если строка не 'true' и не 'false'
     */
    public static boolean getBoolean(final String value) throws IllegalArgumentException {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException("Некорректное значение параметра: '" + value +
                "'. Значение должно быть либо 'true', либо 'false'.");
    }


    /**
     * Получить список имен файлов по маске
     *
     * @param dir       стартовый каталог
     * @param recursive признак того что нужно смотреть дальше чем  на 1 уровень
     * @param filter    окончаение имени файла - фактически расширение.
     * @return Список имен файлов
     */
    public static List<String> getFileList(String dir, boolean recursive, String filter) {
        if (dir == null)
            throw new NullPointerException("dir == null");

        List<String> list = new ArrayList<String>();
        File file = new File(dir);
        for (File eachFile : file.listFiles()) {
            if (recursive && eachFile.isDirectory()) {
                List<String> fileList = getFileList(eachFile.getPath(), recursive, filter);
                list.addAll(fileList);
            } else if (filter == null || eachFile.getName().toLowerCase().endsWith(filter.toLowerCase()))
                list.add(eachFile.getPath());
        }
        return list;
    }


}
