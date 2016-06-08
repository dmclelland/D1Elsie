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

    private String nestedProperty;
    private MyNestedId id;

    public NestedAggregate1(MyNestedId id){
        this.id = id;
    }

    @Override
    protected String getId() {
        return id.toString();
    }

    @Override
    protected void rollbackAggregateToInitialState() {
        this.nestedProperty = null;
    }

    public void doSomething(String nestedProperty){
        apply(eventFactory.createNestedUpdatedEvent1(id, nestedProperty));
    }

    public void doSomethingCausingError(String nestedProperty){
        apply(eventFactory.createNestedUpdatedEvent1(id, nestedProperty));
        apply(eventFactory.createTriggerExceptionNestedEvent(id));
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

