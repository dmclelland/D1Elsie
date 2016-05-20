package com.dmc.d1.cqrs.testdomain;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.testdomain.event.StringUpdatedEvent1;
import com.dmc.d1.cqrs.testdomain.event.StringUpdatedEvent2;
import com.dmc.d1.cqrs.annotations.EventHandler;

/**
 * Created by davidclelland on 17/05/2016.
 */
@com.dmc.d1.cqrs.annotations.Aggregate
public class Aggregate2 extends Aggregate {


    private String s1;
    private String s2;
    private MyId id;

    public Aggregate2(MyId id) {
        this.id = id;
    }

    @Override
    protected void rollbackAggregateToInitialState() {
        this.s1 = null;
        this.s2 = null;
    }


    public void doSomething(String s1, String s2) {
        apply(new StringUpdatedEvent1(getId(), s1));
        apply(new StringUpdatedEvent2(getId(), s2));
    }

    @EventHandler
    public void handleEvent1(StringUpdatedEvent1 event) {

        this.s1 = event.getStr();
    }


    @EventHandler
    public void handleEvent2(StringUpdatedEvent2 event) {
        this.s2 = event.getStr();
    }


    public String getS1() {
        return s1;
    }

    public String getS2() {
        return s2;
    }


    @Override
    protected MyId getId() {
        return id;
    }
}
