package com.dmc.d1.cqrs.test.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.util.NewInstanceFactory;
import com.dmc.d1.test.event.*;

/**
 * Created by davidclelland on 17/05/2016.
 */
@com.dmc.d1.cqrs.annotations.Aggregate
public class Aggregate1 extends Aggregate {

    private static String CLASS_NAME = Aggregate1.class.getName();

    private int i1;
    private int i2;
    private String str;

    Aggregate1() {
    }

    @Override
    protected void copyState(Aggregate copy) {
        Aggregate1 agg = (Aggregate1)copy;
        this.i1 = agg.i1;
        this.i2 = agg.i2;
        this.str = agg.str;
    }

    public void doSomething(int i1, int i2) {
        apply(IntUpdatedEvent1Builder.startBuilding(getId()).i(i1).buildMutable(true));
        apply(IntUpdatedEvent2Builder.startBuilding(getId()).i(i2).buildMutable(true));
    }

    public void doSomething2(String str) {

        String nestedId = generateNestedId(getId());
        apply(HandledByExternalHandlersEventBuilder.startBuilding(getId())
                .nestedId(nestedId).str(str).buildMutable(true));

    }


    public void triggerExceptionInNestedAggregate(String str) {
        String nestedId = generateNestedId(getId());
        apply(TriggerExceptionInNestedAggregateEventBuilder.startBuilding(getId())
                .nestedId(nestedId).str(str).buildMutable(true));
    }

    private String generateNestedId(String id) {
        return id + "Nested";
    }

    @EventHandler
    public void handleEvent1(IntUpdatedEvent1 event) {
        this.i1 = event.getI();
    }


    @EventHandler
    public void handleEvent2(IntUpdatedEvent2 event) {
        this.i2 = event.getI();
    }


    @EventHandler
    public void handleEvent3(HandledByExternalHandlersEvent event) {
        this.str = event.getStr();
    }


    @EventHandler
    public void handleEvent3(TriggerExceptionInNestedAggregateEvent event) {
        this.str = event.getStr();
    }


    public int getI1() {
        return i1;
    }

    public int getI2() {
        return i2;
    }

    public String getStr() {
        return str;
    }


    public static class Factory implements NewInstanceFactory<Aggregate1> {

        @Override
        public String getClassName() {
            return CLASS_NAME;
        }

        @Override
        public Aggregate1 newInstance() {
            return new Aggregate1();
        }
    }
}