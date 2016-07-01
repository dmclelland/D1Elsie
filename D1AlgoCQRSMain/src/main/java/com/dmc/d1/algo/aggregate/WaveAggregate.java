package com.dmc.d1.algo.aggregate;

import com.dmc.d1.cqrs.Aggregate;

import java.util.function.Supplier;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class WaveAggregate extends Aggregate {

    private static String CLASS_NAME = WaveAggregate.class.getName();

    WaveAggregate() {
    }


    @Override
    protected void revertState(Aggregate copy) {
        WaveAggregate agg = (WaveAggregate) copy;
    }


    private static Supplier<WaveAggregate> SUPPLIER = WaveAggregate::new;

    public static Supplier<WaveAggregate> newInstanceFactory() {
        return SUPPLIER;
    }

}
