package com.dmc.d1.cqrs.test.aggregate;

import com.dmc.d1.algo.event.EventFactoryAbstract;
import com.dmc.d1.algo.event.EventFactoryBasic;
import com.dmc.d1.algo.event.NestedUpdatedEvent1;
import com.dmc.d1.algo.event.TriggerExceptionNestedEvent;
import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.test.domain.MyNestedId;

/**
 * Created by davidclelland on 17/05/2016.
 */
@com.dmc.d1.cqrs.annotations.Aggregate
public class NestedAggregate1 extends Aggregate<EventFactoryAbstract> {

    private static String CLASS_NAME = NestedAggregate1.class.getName();

    private String nestedProperty;


    public NestedAggregate1(String id) {
        super(id, CLASS_NAME);
    }

    @Override
    protected void rollbackAggregateToInitialState() {
        this.nestedProperty = null;
    }

    public void doSomething(String nestedProperty){
        apply(eventFactory.createNestedUpdatedEvent1(getId(), nestedProperty));
    }

    public void doSomethingCausingError(String nestedProperty){
        apply(eventFactory.createNestedUpdatedEvent1(getId(), nestedProperty));
        apply(eventFactory.createTriggerExceptionNestedEvent(getId()));
    }


    @EventHandler
    public void handleEvent(NestedUpdatedEvent1 event){
        this.nestedProperty = event.getStr();
    }


    @EventHandler
    public void handleEvent(TriggerExceptionNestedEvent event){
        throw new RuntimeException("This is a nested problem");
    }



    public String getNestedProperty() {
        return nestedProperty;
    }
}

