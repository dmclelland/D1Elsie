package com.dmc.d1.cqrs.test.domain;

import com.dmc.d1.domain.Id;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class MyId extends Id {

    private static final Map<String,MyId> CACHE = new HashMap<>();
    private static Function<String,MyId> SUPPLIER = ID-> new MyId(ID);

    protected MyId(String id) {
        super(id);
    }

    public static MyId from(String str) {
        return Id.from(str,CACHE,SUPPLIER);
    }
}