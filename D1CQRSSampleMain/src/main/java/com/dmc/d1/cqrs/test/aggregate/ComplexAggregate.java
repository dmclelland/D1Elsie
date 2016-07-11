package com.dmc.d1.cqrs.test.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.test.domain.Basket;
import com.dmc.d1.test.domain.BasketBuilder;
import com.dmc.d1.test.event.BasketCreatedEvent;
import com.dmc.d1.test.event.BasketCreatedEventBuilder;

import java.util.function.Supplier;

/**
 * Created by davidclelland on 17/05/2016.
 */
@com.dmc.d1.cqrs.annotations.Aggregate
public class ComplexAggregate extends Aggregate {

    private static String CLASS_NAME = ComplexAggregate.class.getName();

    private Basket basket;

    ComplexAggregate() {
    }

    public void createBasket(Basket basket) {
        apply(BasketCreatedEventBuilder.startBuilding(getId()).basket(basket).buildJournalable());
    }

    @EventHandler
    public void handleEvent(BasketCreatedEvent event) {
        this.basket = BasketBuilder.copyBuilder(event.getBasket()).buildImmutable();
    }

    public Basket getBasket() {
        return basket;
    }

    private static Supplier<ComplexAggregate> SUPPLIER = ComplexAggregate::new;

    public static Supplier<ComplexAggregate> newInstanceFactory() {
        return SUPPLIER;
    }
}