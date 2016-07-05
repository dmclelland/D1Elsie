package com.dmc.d1.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class OrderId extends Id {

    private static Map<String,OrderId> CACHE = new HashMap<>();
    private static Function<String,OrderId> SUPPLIER = ID-> new OrderId(ID);

    protected OrderId(String id) {
        super(id);
    }

    public static OrderId from(String str) {
        return Id.from(str,CACHE,SUPPLIER);
    }
}
