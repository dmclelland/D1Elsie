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
    private static String CLASS_NAME = Aggregate2.class.getName();

    private String s1;
    private String s2;


    Aggregate2(String id) {
        super(id,CLASS_NAME);
    }



    @Override
    protected void rollbackAggregateToInitialState() {
        this.s1 = null;
        this.s2 = null;
    }


    public void doSomething(String s1, String s2) {
        apply(eventFactory.createStringUpdatedEvent1(getId(), s1));
        apply(eventFactory.createStringUpdatedEvent2(getId(), s2));
    }

    public void doSomethingWhichCausesException(String s1, String s2) {
        apply(eventFactory.createStringUpdatedEvent1(getId(), s1));
        apply(eventFactory.createStringUpdatedEvent2(getId(), s2));
        apply(eventFactory.createTriggerExceptionEvent(getId()));
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

}
