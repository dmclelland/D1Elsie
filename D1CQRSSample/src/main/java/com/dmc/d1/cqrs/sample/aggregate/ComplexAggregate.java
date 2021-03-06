package com.dmc.d1.cqrs.sample.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.sample.domain.Basket;
import com.dmc.d1.sample.domain.BasketBuilder;
import com.dmc.d1.sample.event.BasketCreatedEvent;
import com.dmc.d1.sample.event.BasketCreatedEventBuilder;

import java.util.function.Supplier;

/**
 * Created by davidclelland on 17/05/2016.
 */
@com.dmc.d1.cqrs.annotations.Aggregate
public class ComplexAggregate extends Aggregate<ComplexAggregate> {

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

    @Override
    protected ComplexAggregate stateCopy(ComplexAggregate from) {
        this.basket = BasketBuilder.copyBuilder(from.basket).buildImmutable();
        return this;
    }
}