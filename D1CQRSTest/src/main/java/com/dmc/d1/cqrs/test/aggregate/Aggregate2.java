package com.dmc.d1.cqrs.test.aggregate;

import com.dmc.d1.algo.event.*;
import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.test.domain.MyId;

/**
 * Created by davidclelland on 17/05/2016.
 */
@com.dmc.d1.cqrs.annotations.Aggregate
public class Aggregate2 extends Aggregate<EventFactoryAbstract> {


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
        apply(eventFactory.createStringUpdatedEvent1(id, s1));
        apply(eventFactory.createStringUpdatedEvent2(id, s2));
    }

    public void doSomethingWhichCausesException(String s1, String s2) {
        apply(eventFactory.createStringUpdatedEvent1(id, s1));
        apply(eventFactory.createStringUpdatedEvent2(id, s2));
        apply(eventFactory.createTriggerExceptionEvent(id));
    }

    @EventHandler
    public void handleEvent1(StringUpdatedEvent1 event) {

        this.s1 = event.getStr();
    }


    @EventHandler
    public void handleEvent2(StringUpdatedEvent2 event) {
        this.s2 = event.getStr();
    }


    @EventHandler
    public void handleEvent2(TriggerExceptionEvent event) {
        throw new RuntimeException("This is a problem");
    }


    public String getS1() {
        return s1;
    }

    public String getS2() {
        return s2;
    }


    @Override
    protected String getId() {
        return id.toString();
    }


}
