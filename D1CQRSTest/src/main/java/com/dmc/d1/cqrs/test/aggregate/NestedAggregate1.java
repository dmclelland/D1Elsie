package com.dmc.d1.cqrs.test.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.util.NewInstanceFactory;
import com.dmc.d1.test.event.NestedUpdatedEvent1;
import com.dmc.d1.test.event.NestedUpdatedEvent1Builder;
import com.dmc.d1.test.event.TriggerExceptionNestedEvent;
import com.dmc.d1.test.event.TriggerExceptionNestedEventBuilder;

/**
 * Created by davidclelland on 17/05/2016.
 */
@com.dmc.d1.cqrs.annotations.Aggregate
public class NestedAggregate1 extends Aggregate {

    private static String CLASS_NAME = NestedAggregate1.class.getName();

    private String nestedProperty;


    NestedAggregate1() {}


    @Override
    protected void copyState(Aggregate copy) {
        NestedAggregate1 agg = (NestedAggregate1) copy;
        this.nestedProperty = agg.nestedProperty;
    }

    public void doSomething(String nestedProperty) {
        apply(NestedUpdatedEvent1Builder.startBuilding(getId()).str(nestedProperty).buildMutable(true));
    }

    public void doSomethingCausingError(String nestedProperty) {
        apply(NestedUpdatedEvent1Builder.startBuilding(getId()).str(nestedProperty).buildMutable(true));
        apply(TriggerExceptionNestedEventBuilder.startBuilding(getId()).buildMutable(true));
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

    public static class Factory implements NewInstanceFactory<NestedAggregate1> {

        @Override
        public String getClassName() {
            return CLASS_NAME;
        }

        @Override
        public NestedAggregate1 newInstance() {
            return new NestedAggregate1();
        }
    }
}

