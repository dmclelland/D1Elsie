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

    private static String CLASS_NAME = Aggregate1.class.getName();

    private int i1;
    private int i2;
    private String str;


    public Aggregate1(String id) {
        super(id, CLASS_NAME);
    }


    @Override
    protected void rollbackAggregateToInitialState() {
        this.i1 = 0;
        this.i2 = 0;
    }

    public void doSomething(int i1, int i2){
        apply(eventFactory.createIntUpdatedEvent1(getId(), i1));
        apply(eventFactory.createIntUpdatedEvent2(getId(), i2));
    }

    public void doSomething2(String str){
        String nestedId = generateNestedId(getId());
        apply(eventFactory.createHandledByExternalHandlersEvent(getId(), nestedId, str));
    }


    public void triggerExceptionInNestedAggregate(String str){
        String nestedId = generateNestedId(getId());
        apply(eventFactory.createTriggerExceptionInNestedAggregateEvent(getId(), nestedId, str));
    }

    private String generateNestedId(String id){
        return id+"Nested";
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

