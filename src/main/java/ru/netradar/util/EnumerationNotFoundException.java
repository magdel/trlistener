package ru.netradar.util;


public class EnumerationNotFoundException extends RuntimeException {

    public EnumerationNotFoundException() {
        super();
    }

    public EnumerationNotFoundException(String message) {
        super(message);
    }

    public EnumerationNotFoundException(Class clazz, int ordinal) {
        this((clazz != null ? clazz.getName() : "null") + ", ordinal=" + ordinal);
    }

    public EnumerationNotFoundException(Class clazz, String name) {
        this((clazz != null ? clazz.getName() : "null") + "name=" + name + ')');
    }
}
