package com.dmc.d1.cqrs.sample.domain;

import com.dmc.d1.domain.Id;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class MyNestedId extends Id {

    private static Map<String,MyNestedId> CACHE = new HashMap<>();
    private static Function<String,MyNestedId> SUPPLIER = ID-> new MyNestedId(ID);


    protected MyNestedId(String id) {
        super(id);
    }

    public static MyNestedId from(String str) {

        return Id.from(str,CACHE,SUPPLIER);

    }

}
