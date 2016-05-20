package com.dmc.d1.cqrs.testdomain;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.testdomain.event.IntUpdatedEvent1;
import com.dmc.d1.cqrs.testdomain.event.IntUpdatedEvent2;
import com.dmc.d1.cqrs.annotations.EventHandler;

/**
 * Created by davidclelland on 17/05/2016.
 */
@com.dmc.d1.cqrs.annotations.Aggregate
public class Aggregate1 extends Aggregate<MyId> {

    private int i1;
    private int i2;
    private MyId id;

    public Aggregate1(MyId id){
        this.id = id;
    }


    @Override
    protected MyId getId() {
        return id;
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

    @EventHandler
    public void handleEvent1(IntUpdatedEvent1 event){

        this.i1 = event.getI();
    }


    @EventHandler
    public void handleEvent2(IntUpdatedEvent2 event){
        this.i2 = event.getI();
    }


    public int getI1() {
        return i1;
    }

    public int getI2() {
        return i2;
    }


}

