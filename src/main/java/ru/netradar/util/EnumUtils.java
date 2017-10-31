package ru.netradar.util;


import java.util.ArrayList;
import java.util.Collection;


public class EnumUtils {

    /**
     * Создаёт строку, содержащую строковое представление объектов.
     * Если коллекция равна null или пустая - возвращается пустая строка.
     * Пример результата (для коллекции чисел [2, 5, 7, 10], разделитель: [, ]): "2, 5, 7, 10".
     *
     * @param objectCollection коллекция объектов
     * @param comma            строка разделилтель, между строковыми представлениями объектов
     * @return строка, содержащаю порядковые номера элементов перечеслений, переданных в коллекции
     */
    public static String objectCollectionToCommaString(final Collection<?> objectCollection, final String comma) {
        if (objectCollection == null) {
            return "";
        }
        if (objectCollection.size() == 0) {
            return "";
        }

        if (comma == null) {
            throw new IllegalArgumentException("Parameter comma cannot be null");
        }

        final StringBuilder stringBuilder = new StringBuilder();
        for (Object obj : objectCollection) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(comma);
            }
            stringBuilder.append(obj);
        }

        return stringBuilder.toString();
    }

    /**
     * Создаёт строку, содержащую порядковые номера элементов перечеслений, переданных в коллекции.
     * Если коллекция равна null или пустая - возвращается пустая строка.
     * Пример результата: "2, 5, 7, 10".
     *
     * @param enumCollection коллекция перечислений
     * @param comma          строка разделитель, между порядковыми номерами элементов перечислений
     * @return строка, содержащаю порядковые номера элементов перечеслений, переданных в коллекции
     */
    public static String enumCollectionToOrdinalString(final Collection<? extends Enumeration<?>> enumCollection,
                                                       final String comma) {
        if (enumCollection == null) {
            return "";
        }

        final Collection<Integer> ordinalCollection = new ArrayList<Integer>();
        for (Enumeration e : enumCollection) {
            ordinalCollection.add(e.getOrdinal());
        }
        return objectCollectionToCommaString(ordinalCollection, comma);
    }

    private EnumUtils() {

    }
}
