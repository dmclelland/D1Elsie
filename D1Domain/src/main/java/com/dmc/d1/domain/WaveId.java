package com.dmc.d1.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class WaveId extends Id {

    private static Map<String,WaveId> CACHE = new HashMap<>();
    private static Function<String,WaveId> SUPPLIER = ID-> new WaveId(ID);

    protected WaveId(String id) {
        super(id);
    }

    public static WaveId from(String str) {
        return Id.from(str,CACHE,SUPPLIER);
    }

}
