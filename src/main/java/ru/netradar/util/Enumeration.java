package ru.netradar.util;

import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;


public abstract class Enumeration<E extends Enumeration<E>> implements Serializable {

    private static final long serialVersionUID = 23908572039857L;

    /**
     * Уникальный код (порядковый номер).
     */
    private final int ordinal;

    /**
     * Уникальное имя объекта перечислимого типа для заданного класса.
     */
    private final transient String name;

    /**
     * Класс перечислимого типа.
     */
    private final transient Class<E> clazz;

    /**
     * Создает новый экземпляр перечислимого типа с указанным именем и понядковым номером и
     * регистрирует его в списке объектов для этого перечислимого типа.
     * Проверяет на уникальность имя и порядковый номер в перечислимом типе к которому
     * относится создаваемый объект.
     *
     * @param ordinal порядковый номер
     * @param name    имя
     * @throws ClassCastException       if uniqueness class is not a correct superclass.
     * @throws IllegalArgumentException если имя и/или порядкоый номер не уникальны.
     */
    @SuppressWarnings("unchecked")
    protected Enumeration(int ordinal, String name) {
        this.ordinal = ordinal;
        this.name = name;
        this.clazz = getExactUniqueEnumerationClass();
        EnumerationRegistry.register((E) this);
    }

    /**
     * Создает новый экземпляр перечислимого типа с указанным именем.
     * Задает уникальный порядковый номер для перечислимого типа путем добавления единицы
     * к последнему добавленному. Если не было ни одного добалено, то присваивается значение 0.
     * Проверяет на уникальность имя и порядковый номер в перечислимом типе к которому
     * относится создаваемый объект.
     *
     * @param name имя
     * @throws ClassCastException       if uniqueness class is not a correct superclass.
     * @throws IllegalArgumentException если имя и/или порядкоый номер не уникальны.
     */
    @SuppressWarnings("unchecked")
    protected Enumeration(String name) {
        this.clazz = getExactUniqueEnumerationClass();
        this.ordinal = EnumerationRegistry.getLastOrdinal(clazz) + 1;
        this.name = name;
        EnumerationRegistry.register((E) this);
    }

    public int getOrdinal() {
        return ordinal;
    }

    public String getName() {
        return name;
    }

    Class<E> getEnumerationClass() {
        return clazz;
    }

    /**
     * Ищет элемент перечислимого типа по заданному имени.
     *
     * @param clazz класс перечислимого типа.
     * @param name  имя.
     * @return возвращает элемент перечислимого типа. Значение null недопустимо.
     * @throws EnumerationNotFoundException элемент перечислимого типа не найден.
     */
    public static <E extends Enumeration<? super E>> E findByName(Class<E> clazz, String name) {
        EnumerationRegistry<E> registry = clazz != null ? EnumerationRegistry.getRegistry(clazz) : null;
        if (registry == null)
            throw new EnumerationNotFoundException("Enumeration class not found. class: " + clazz + ", name: " + name);
        return registry.findByName(name);
    }

    /**
     * Ищет элемент перечислимого типа по имени.
     * Если элемент не найден, то возвращает значение по умолчанию.
     *
     * @param clazz     класс перечислимого типа.
     * @param name      имя.
     * @param def_value значение, которое будет возвращено в случае отсутствия искомого.
     * @return возвращает элемент перечислимого типа. Если элемент не найден, то возвращает значение по умолчанию.
     */
    public static <E extends Enumeration<? super E>> E findByName(Class<E> clazz, String name, E def_value) {
        EnumerationRegistry<E> registry = clazz != null ? EnumerationRegistry.getRegistry(clazz) : null;
        if (registry == null)
            return def_value;
        if (registry.contains(name))
            return registry.findByName(name);
        return def_value;
    }

    public static <E extends Enumeration<? super E>> boolean contains(Class<E> clazz, String name) {
        EnumerationRegistry<E> registry = clazz != null ? EnumerationRegistry.getRegistry(clazz) : null;
        return registry != null && registry.contains(name);
    }

    /**
     * Ищет элемент перечислимого типа по заданному порядковому номеру.
     *
     * @param clazz   класс перечислимого типа.
     * @param ordinal порядковый номер.
     * @return возвращает элемент перечислимого типа. Значение null недопустимо.
     * @throws EnumerationNotFoundException элемент перечислимого типа не найден.
     */
    public static <E extends Enumeration<? super E>> E findByOrdinal(Class<E> clazz, int ordinal) {
        EnumerationRegistry<E> registry = clazz != null ? EnumerationRegistry.getRegistry(clazz) : null;
        if (registry == null)
            throw new EnumerationNotFoundException("Enumeration class not found. class: " + clazz + ", ordinal: " + ordinal);
        return registry.findByOrdinal(ordinal);
    }

    /**
     * Ищет элемент перечислимого типа по заданному порядковому номеру.
     * Если элемент не найден, то возвращает значение по умолчанию.
     *
     * @param clazz     класс перечислимого типа.
     * @param ordinal   порядковый номер.
     * @param def_value значение, которое будет возвращено в случае отсутствия искомого.
     * @return возвращает элемент перечислимого типа. Если элемент не найден, то возвращает значение по умолчанию.
     */
    public static <E extends Enumeration<? super E>> E findByOrdinal(Class<E> clazz, int ordinal, E def_value) {
        EnumerationRegistry<E> registry = clazz != null ? EnumerationRegistry.getRegistry(clazz) : null;
        if (registry == null)
            return def_value;
        if (registry.contains(ordinal))
            return registry.findByOrdinal(ordinal);
        return def_value;
    }

    public static <E extends Enumeration<? super E>> boolean contains(Class<E> clazz, int ordinal) {
        EnumerationRegistry<E> registry = clazz != null ? EnumerationRegistry.getRegistry(clazz) : null;
        return registry != null && registry.contains(ordinal);
    }

    /**
     * Maps support code
     */
    @Override
    public int hashCode() {
        return ordinal;
    }

    @Override
    public boolean equals(Object anObject) {
        return this == anObject;
    }

    @Override
    public String toString() {
        return name;
    }

    public static String toString(Enumeration e) {
        return new StringBuilder().
                append(e.clazz.getName()).
                append("={").
                append(" ord=").append(e.ordinal).
                append(" name=").append(e.name).
                append("}").toString();
    }

    /**
     * По десериализованному значению порядкового номера объекта перечислимого типа и по
     * имени класса находит уникальный инстанс объекта.
     * Ошибки поиска требуемого объекта в соответствии с контрактом данного
     * метода преобразуются в ошибки ввода/вывода.
     *
     * @return Возвращает правильный (уникальный)
     *         десериализованный объект перечислимого типа.
     * @throws java.io.ObjectStreamException ошибка маршалинга или поиска объекта.
     * @see {http://java.sun.com/j2se/1.3/docs/guide/serialization/spec/input.doc6.html#5903
     */
    @SuppressWarnings("unchecked")
    protected Object readResolve() throws ObjectStreamException {
        try {
            return Enumeration.findByOrdinal(getExactUniqueEnumerationClass(), ordinal);
        } catch (ClassCastException e) {
            throw new InvalidClassException(e.getMessage());
        } catch (EnumerationNotFoundException e) {
            throw new InvalidObjectException("Enumeration not found: " + e.getMessage());
        }
    }

    /**
     * Запрещает клонирование объектов перечислимых типов.
     */
    @Override
    @SuppressWarnings({"CloneDoesntCallSuperClone"})
    protected final Object clone() throws CloneNotSupportedException {
        if (!(this instanceof Cloneable))
            throw new CloneNotSupportedException();
        throw new UnsupportedOperationException("Enumeration instance cannot be cloned.");
    }

    /**
     * Возвращает уникальное имя класса перечислимого типа.
     *
     * @return Возвращает уникальное имя класса перечислимого типа.
     * @throws ClassCastException класс не является классом перечислимого типа.
     */
    protected final Class<E> getExactUniqueEnumerationClass() {
        Class<E> c = getUniqueEnumerationClass();
        if (!c.isInstance(this))
            throw new ClassCastException("Class is not a subclass of Enumeration: " + c);
        Class e = Enumeration.class;
        if (!e.isAssignableFrom(c) || c == e)
            throw new ClassCastException("Enumeration class is too generic: " + c);
        return c;
    }

    /**
     * Возвращает уникальный класс перечислимого типа. Этот уникальный класс должен быть
     * классом или классом предком текущего объекта перечислимого типа. Кроме того,
     * он должен быть наследником класса {@link Enumeration}.
     * <p/>
     * Таким образом новый непосредственный потомок класса {@link Enumeration} создает
     * новый уникальный класс перечислимого типа, который включает в себя
     * все его подклассы.
     *
     * @return Возвращает класс текущего объекта или его суперкласс, который является
     *         <code>непосредственным</code> потомком класса {@link Enumeration}
     */
    @SuppressWarnings("unchecked")
    private Class<E> getUniqueEnumerationClass() {
        Class<?> c = getClass();
        Class<?> s;
        while ((s = c.getSuperclass()) != Enumeration.class && s != null)
            c = s;
        return (Class<E>) c;
    }

    /**
     * @param clazz класс перечислимого типа.
     * @return Возвращает коллекцию всех значений Enumeration
     * @throws EnumerationNotFoundException элемент перечислимого типа не найден.
     */
    public static <E extends Enumeration<? super E>> Collection<E> values(Class<E> clazz) {
        EnumerationRegistry<E> registry = clazz != null ? EnumerationRegistry.getRegistry(clazz) : null;
        if (registry == null)
            throw new EnumerationNotFoundException("Enumeration class not found. class: " + clazz);
        return registry.values();
    }

    /**
     * @param clazz класс перечислимого типа.
     * @return Возвращает массив всех значений Enumeration
     * @throws EnumerationNotFoundException элемент перечислимого типа не найден.
     */
    public static <E extends Enumeration<? super E>> Object[] toArray(Class<E> clazz) {
        EnumerationRegistry<E> registry = clazz != null ? EnumerationRegistry.getRegistry(clazz) : null;
        if (registry == null)
            throw new EnumerationNotFoundException("Enumeration class not found. class: " + clazz);
        return registry.toArray();
    }

}
