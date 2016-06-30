package com.dmc.d1.cqrs.test.domain;

import com.dmc.d1.domain.Id;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class MyNestedId extends Id {
    static class MyNestedIdFactory extends IDFactory<MyNestedId> {

        @Override
        protected MyNestedId create(String str) {
            return new MyNestedId(str);
        }
    }

    private static Map<String,MyNestedId> CACHE = new HashMap<>();
    private static MyNestedIdFactory FACTORY = new MyNestedIdFactory();


    protected MyNestedId(String id) {
        super(id);
    }

    public static MyNestedId from(String str) {

        return Id.from(str,CACHE,FACTORY);

    }

}
