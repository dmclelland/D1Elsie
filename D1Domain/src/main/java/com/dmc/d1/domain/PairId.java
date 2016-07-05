package com.dmc.d1.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class PairId extends Id {

    private static Map<String,PairId> CACHE = new HashMap<>();
    private static Function<String,PairId> SUPPLIER = ID-> new PairId(ID);

    protected PairId(String id) {
        super(id);
    }

    public static PairId from(String str) {
        return Id.from(str,CACHE,SUPPLIER);
    }
}
