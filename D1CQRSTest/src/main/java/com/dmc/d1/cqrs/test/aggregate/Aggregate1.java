package com.dmc.d1.cqrs.test.aggregate;

import com.dmc.d1.algo.event.*;
import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.test.domain.MyId;
import com.dmc.d1.cqrs.test.domain.MyNestedId;

/**
 * Created by davidclelland on 17/05/2016.
 */
@com.dmc.d1.cqrs.annotations.Aggregate
public class Aggregate1 extends Aggregate<EventFactoryAbstract>{

    private int i1;
    private int i2;
    private String str;

    private final MyId id;

    public Aggregate1(MyId id){
        this.id = id;
    }

    @Override
    protected String getId() {
        return id.toString();
    }

    @Override
    protected void rollbackAggregateToInitialState() {
        this.i1 = 0;
        this.i2 = 0;
    }

    public void doSomething(int i1, int i2){
        apply(eventFactory.createIntUpdatedEvent1(id, i1));
        apply(eventFactory.createIntUpdatedEvent2(id, i2));
    }

    public void doSomething2(String str){
        MyNestedId nestedId = generateNestedId(id);
        apply(eventFactory.createHandledByExternalHandlersEvent(id, nestedId, str));
    }


    public void triggerExceptionInNestedAggregate(String str){
        MyNestedId nestedId = generateNestedId(id);
        apply(eventFactory.createTriggerExceptionInNestedAggregateEvent(id, nestedId, str));
    }

    private MyNestedId generateNestedId(MyId id){
        return new MyNestedId(id.toString()+"Nested");
    }

    @EventHandler
    public void handleEvent1(IntUpdatedEvent1 event){
        this.i1 = event.getI();
    }


    @EventHandler
    public void handleEvent2(IntUpdatedEvent2 event){
        this.i2 = event.getI();
    }


    @EventHandler
    public void handleEvent3(HandledByExternalHandlersEvent event){
        this.str = event.getStr();
    }


    @EventHandler
    public void handleEvent3(TriggerExceptionInNestedAggregateEvent event){
        this.str = event.getStr();
    }


    public int getI1() {
        return i1;
    }

    public int getI2() {
        return i2;
    }

    public String getStr(){
        return str;
    }
}

