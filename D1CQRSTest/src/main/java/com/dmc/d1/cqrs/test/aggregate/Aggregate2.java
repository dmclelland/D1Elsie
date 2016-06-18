package com.dmc.d1.cqrs.test.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.util.NewInstanceFactory;
import com.dmc.d1.test.event.*;

/**
 * Created by davidclelland on 17/05/2016.
 */
@com.dmc.d1.cqrs.annotations.Aggregate
public class Aggregate2 extends Aggregate {
    private static String CLASS_NAME = Aggregate2.class.getName();

    private String s1;
    private String s2;


    Aggregate2() {
    }


    @Override
    protected void rollbackAggregateToInitialState() {
        this.s1 = null;
        this.s2 = null;
    }


    public void doSomething(String s1, String s2) {
        apply(StringUpdatedEvent1Builder.startBuilding(getId()).str(s1).buildChronicle());
        apply(StringUpdatedEvent2Builder.startBuilding(getId()).str(s2).buildChronicle());

    }

    public void doSomethingWhichCausesException(String s1, String s2) {
        apply(StringUpdatedEvent1Builder.startBuilding(getId()).str(s1).buildChronicle());
        apply(StringUpdatedEvent2Builder.startBuilding(getId()).str(s2).buildChronicle());
        apply(TriggerExceptionEventBuilder.startBuilding(getId()).buildChronicle());
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


    public static class Factory implements NewInstanceFactory<Aggregate2> {

        @Override
        public String getClassName() {
            return CLASS_NAME;
        }

        @Override
        public Aggregate2 newInstance() {
            return new Aggregate2();
        }
    }

}
