package com.dmc.d1.cqrs.test.domain;

import com.dmc.d1.domain.Id;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class MyId extends Id {

    static class MyIdFactory extends IDFactory<MyId> {

        @Override
        protected MyId create(String str) {
            return new MyId(str);
        }
    }

    private static Map<String,MyId> CACHE = new HashMap<>();
    private static MyIdFactory FACTORY = new MyIdFactory();


    protected MyId(String id) {
        super(id);
    }

    public static MyId from(String str) {

        return Id.from(str,CACHE,FACTORY);

    }


}
