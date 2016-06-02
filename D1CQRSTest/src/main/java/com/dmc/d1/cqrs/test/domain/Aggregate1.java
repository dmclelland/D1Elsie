package com.dmc.d1.cqrs.test.domain;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.test.event.HandledByExternalHandlersEvent;
import com.dmc.d1.cqrs.test.event.IntUpdatedEvent1;
import com.dmc.d1.cqrs.test.event.IntUpdatedEvent2;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.test.event.TriggeringExceptionInNestedAggregateEvent;

/**
 * Created by davidclelland on 17/05/2016.
 */
@com.dmc.d1.cqrs.annotations.Aggregate
public class Aggregate1 extends Aggregate {

    private int i1;
    private int i2;
    private String str;

    private MyId id;

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
        apply(new IntUpdatedEvent1(id, i1));
        apply(new IntUpdatedEvent2(id, i2));
    }

    public void doSomething2(String str){
        MyNestedId nestedId = generateNestedId(id);

        apply(new HandledByExternalHandlersEvent(id, nestedId, str));

    }


    public void triggerExceptionInNestedAggregate(String str){
        MyNestedId nestedId = generateNestedId(id);

        apply(new TriggeringExceptionInNestedAggregateEvent(id, nestedId, str));

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
    public void handleEvent3(TriggeringExceptionInNestedAggregateEvent event){
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

