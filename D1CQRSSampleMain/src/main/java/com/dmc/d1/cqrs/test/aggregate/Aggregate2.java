package com.dmc.d1.cqrs.test.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.test.event.*;

import java.util.function.Supplier;

/**
 * Created by davidclelland on 17/05/2016.
 */
@com.dmc.d1.cqrs.annotations.Aggregate
public class Aggregate2 extends Aggregate<Aggregate2> {
    private static String CLASS_NAME = Aggregate2.class.getName();

    private String s1;
    private String s2;

    Aggregate2() {
    }


    public void doSomething(String s1, String s2) {
        apply(StringUpdatedEvent1Builder.startBuilding(getId()).str(s1).buildPooledJournalable());
        apply(StringUpdatedEvent2Builder.startBuilding(getId()).str(s2).buildPooledJournalable());

    }

    public void doSomethingWhichCausesException(String s1, String s2) {
        apply(StringUpdatedEvent1Builder.startBuilding(getId()).str(s1).buildPooledJournalable());
        apply(StringUpdatedEvent2Builder.startBuilding(getId()).str(s2).buildPooledJournalable());
        apply(TriggerExceptionEventBuilder.startBuilding(getId()).buildPooledJournalable());
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


    private static Supplier<Aggregate2> SUPPLIER = Aggregate2::new;

    public static Supplier<Aggregate2> newInstanceFactory() {
        return SUPPLIER;
    }

    @Override
    protected Aggregate2 stateCopy(Aggregate2 from) {
        this.s1 = from.s1;
        this.s2 = from.s2;

        return this;
    }
}
