package com.dmc.d1.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class UserId extends Id {

    private static Map<String,UserId> CACHE = new HashMap<>();
    private static Function<String,UserId> SUPPLIER = ID-> new UserId(ID);

    protected UserId(String id) {
        super(id);
    }

    public static UserId from(String str) {
        return Id.from(str,CACHE,SUPPLIER);
    }
}
