package com.dmc.d1.domain;

import java.util.Map;

/**
 * Created by davidclelland on 16/05/2016.
 */
public abstract class Id {

    protected static abstract class IDFactory<ID extends Id> {
        protected abstract ID create(String str);
    }

    protected String id;

    protected Id(String id) {
        this.id = id;
    }

    public String asString() {
        return id;
    }

    public String toString(){
        return asString();
    }


    protected static <ID extends Id> ID from(String str, Map<String, ID> cache, IDFactory<ID> factory) {
        ID id = cache.get(str);

        if (id == null) {
            id = factory.create(str);
            cache.put(str, id);
        }
        return id;
    }
}
