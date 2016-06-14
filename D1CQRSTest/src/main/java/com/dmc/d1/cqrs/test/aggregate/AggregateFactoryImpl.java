package com.dmc.d1.cqrs.test.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.AggregateFactory;
import com.dmc.d1.cqrs.test.domain.MyId;
import com.dmc.d1.cqrs.test.domain.MyNestedId;

import java.util.Map;

/**
 * Created By davidclelland on 13/06/2016.
 */
public class AggregateFactoryImpl implements AggregateFactory {


    private static String AGGREGATE1_NAME = Aggregate1.class.getName();
    private static String AGGREGATE2_NAME = Aggregate2.class.getName();
    private static String NESTED_AGGREGATE_NAME = NestedAggregate1.class.getName();

    @Override
    public Aggregate create(String id, String className) {
        if(className.equals(AGGREGATE1_NAME))
            return new Aggregate1(id);

        if(className.equals(AGGREGATE2_NAME))
            return new Aggregate2(id);

        if(className.equals(NESTED_AGGREGATE_NAME))
            return new NestedAggregate1(id);

        throw new IllegalStateException("No aggregate of type " + className);
    }
}
