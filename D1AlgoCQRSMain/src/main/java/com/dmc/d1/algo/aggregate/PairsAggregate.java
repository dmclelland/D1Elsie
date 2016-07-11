package com.dmc.d1.algo.aggregate;

import com.dmc.d1.cqrs.Aggregate;

import java.util.function.Supplier;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class PairsAggregate extends Aggregate {

    private static String CLASS_NAME = PairsAggregate.class.getName();

    PairsAggregate() {
    }

    private static Supplier<PairsAggregate> SUPPLIER = PairsAggregate::new;

    public static Supplier<PairsAggregate> newInstanceFactory() {
        return SUPPLIER;
    }


}
