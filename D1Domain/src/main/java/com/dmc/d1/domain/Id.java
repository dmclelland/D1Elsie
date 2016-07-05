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
}
