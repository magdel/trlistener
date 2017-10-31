package ru.netradar.util;


import java.util.*;


final class EnumerationRegistry<E extends Enumeration<? super E>> {
    private static HashMap<Class, EnumerationRegistry> registies = new HashMap<Class, EnumerationRegistry>();

    private final Class<E> clazz; // Unique Enumeration class.
    private volatile int size; // Number of registered Enumeration instances.
    private volatile int last_ordinal = -1; // Last registered ordinal.
    private HashMap<String, E> name_map = new HashMap<String, E>();
    private HashMap<Integer, E> ordinal_map = new HashMap<Integer, E>();
    private List<E> values = new ArrayList<E>();

    private EnumerationRegistry(Class<E> clazz) {
        this.clazz = clazz;
    }

    public E findByOrdinal(int ordinal) {
        E e = ordinal_map.get(ordinal);
        if (e != null)
            return e;
        throw new EnumerationNotFoundException(clazz, ordinal);
    }

    public boolean contains(int ordinal) {
        return ordinal_map.containsKey(ordinal);
    }

    public E findByName(String name) {
        E e = name_map.get(name);
        if (e != null)
            return e;
        throw new EnumerationNotFoundException(clazz, name);
    }

    public boolean contains(String name) {
        return name_map.containsKey(name);
    }

    public Object[] toArray() {
        return values.toArray();
    }

    public Collection<E> values() {
        return Collections.unmodifiableList(values);
    }

    private void registerInternal(E e) {
        if (clazz != e.getEnumerationClass())
            throw new ClassCastException("Requires " + clazz + ", found " + e.getEnumerationClass());

        if (name_map.put(e.getName(), e) != null) {
            throw new IllegalArgumentException("Name: " + e.getName() + " not unique");
        }

        if (ordinal_map.put(e.getOrdinal(), e) != null) {
            throw new IllegalArgumentException("Ordinal: " + e.getOrdinal() + " not unique");
        }

        values.add(e);
        last_ordinal = e.getOrdinal();
        ++size;
    }

    @SuppressWarnings("unchecked")
    static <E extends Enumeration<E>> void register(E t) {
        Class<E> ec = t.getEnumerationClass();
        EnumerationRegistry<E> registry = registies.get(ec);
        if (registry == null)
            registies.put(ec, registry = new EnumerationRegistry(ec));
        registry.registerInternal(t);
    }

    /**
     * Метод возвращает объект хранилища перечислимых типов для указанного класса.
     *
     * @param clazz класс перечислимых типов
     * @return объект хранилища. В случае отсутствия возвращается null.
     */
    @SuppressWarnings("unchecked")
    static <E extends Enumeration<? super E>> EnumerationRegistry<E> getRegistry(Class<E> clazz) {
        if (clazz == null)
            return null;
        EnumerationRegistry registry = registies.get(clazz);

        if (registry == null && Enumeration.class.isAssignableFrom(clazz))
            try { // For serialization
                Class.forName(clazz.getName(), true, clazz.getClassLoader());
                registry = registies.get(clazz);
            } catch (ClassNotFoundException e) {
                throw new NoClassDefFoundError(e.getMessage());
            }
        return registry;
    }

    /**
     * Возвращает значение последнего зарегистрированного уникального номера.
     * Если ни один перечислимый тип в данной коллекции типов не был
     * зарегистрирован, то возвращается значение -1.
     *
     * @param clazz -
     * @return -
     */
    static int getLastOrdinal(Class<? extends Enumeration> clazz) {
        EnumerationRegistry registy = getRegistry(clazz);
        return registy == null || registy.size == 0 ? -1 : registy.last_ordinal;
    }

}
