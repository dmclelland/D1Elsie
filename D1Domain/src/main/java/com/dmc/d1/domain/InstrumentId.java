package com.dmc.d1.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class InstrumentId extends Id {

    private static Map<String,InstrumentId> CACHE = new HashMap<>();
    private static Function<String,InstrumentId> SUPPLIER = ID-> new InstrumentId(ID);

    protected InstrumentId(String id) {
        super(id);
    }

    public static InstrumentId from(String str) {
        return Id.from(str,CACHE,SUPPLIER);
    }
}
