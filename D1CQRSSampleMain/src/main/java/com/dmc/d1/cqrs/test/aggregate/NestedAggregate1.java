package com.dmc.d1.cqrs.test.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.test.event.NestedUpdatedEvent1;
import com.dmc.d1.test.event.NestedUpdatedEvent1Builder;
import com.dmc.d1.test.event.TriggerExceptionNestedEvent;
import com.dmc.d1.test.event.TriggerExceptionNestedEventBuilder;

import java.util.function.Supplier;

/**
 * Created by davidclelland on 17/05/2016.
 */
@com.dmc.d1.cqrs.annotations.Aggregate
public class NestedAggregate1 extends Aggregate {

    private static String CLASS_NAME = NestedAggregate1.class.getName();

    private String nestedProperty;


    NestedAggregate1() {
    }


    public void doSomething(String nestedProperty) {
        apply(NestedUpdatedEvent1Builder.startBuilding(getId()).str(nestedProperty).buildPooledJournalable());
    }

    public void doSomethingCausingError(String nestedProperty) {
        apply(NestedUpdatedEvent1Builder.startBuilding(getId()).str(nestedProperty).buildPooledJournalable());
        apply(TriggerExceptionNestedEventBuilder.startBuilding(getId()).buildPooledJournalable());
    }


    @EventHandler
    public void handleEvent(NestedUpdatedEvent1 event) {
        this.nestedProperty = event.getStr();
    }


    @EventHandler
    public void handleEvent(TriggerExceptionNestedEvent event) {
        throw new RuntimeException("This is a nested problem");
    }


    public String getNestedProperty() {
        return nestedProperty;
    }

    private static Supplier<NestedAggregate1> SUPPLIER = NestedAggregate1::new;

    public static Supplier<NestedAggregate1> newInstanceFactory() {
        return SUPPLIER;
    }
}

