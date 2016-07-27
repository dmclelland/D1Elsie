package com.dmc.d1.cqrs.test.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.test.event.*;

import java.util.function.Supplier;

/**
 * Created by davidclelland on 17/05/2016.
 */
@com.dmc.d1.cqrs.annotations.Aggregate
public class Aggregate1 extends Aggregate<Aggregate1> {

    private static String CLASS_NAME = Aggregate1.class.getName();

    private int i1;
    private int i2;
    private String str;

    Aggregate1() {
    }


    public void doSomething(int i1, int i2) {
        apply(IntUpdatedEvent1Builder.startBuilding(getId()).i(i1).buildJournalable());
        apply(IntUpdatedEvent2Builder.startBuilding(getId()).i(i2).buildJournalable());
    }

    public void doSomething2(String str) {

        String nestedId = generateNestedId(getId());
        apply(HandledByExternalHandlersEventBuilder.startBuilding(getId())
                .nestedId(nestedId).str(str).buildJournalable());

    }


    public void triggerExceptionInNestedAggregate(String str) {
        String nestedId = generateNestedId(getId());
        apply(TriggerExceptionInNestedAggregateEventBuilder.startBuilding(getId())
                .nestedId(nestedId).str(str).buildJournalable());
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


    private static Supplier<Aggregate1> SUPPLIER = Aggregate1::new;
;
    public static Supplier<Aggregate1> newInstanceFactory() {
        return SUPPLIER;
    }


    @Override
    protected Aggregate1 stateCopy(Aggregate1 orig) {
        this.i1 = orig.i1;
        this.i2 = orig.i2;
        this.str = orig.str;

        return this;
    }
}