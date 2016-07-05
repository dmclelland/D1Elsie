package com.dmc.d1.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by davidclelland on 16/05/2016.
 */
public abstract class Id {

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


    protected static <ID extends Id> ID from(String str,  Map<String, ID> cache,  Function<String,ID> create) {
        ID id = cache.get(str);

        if (cache.get(str) == null) {
            id = create.apply(str);
            cache.put(str, id);
        }
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Id id1 = (Id) o;

        return id != null ? id.equals(id1.id) : id1.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
