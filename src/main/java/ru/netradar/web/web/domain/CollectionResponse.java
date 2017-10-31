package ru.netradar.web.web.domain;


import java.util.Collection;

public class CollectionResponse<T> {
    private final Collection<T> collection;

    public CollectionResponse() {
        collection = null;
    }

    public CollectionResponse(Collection<T> collection) {
        this.collection = collection;
    }

    public Collection<T> getCollection() {
        return collection;
    }

    public int getCount() {
        return collection.size();
    }

}
