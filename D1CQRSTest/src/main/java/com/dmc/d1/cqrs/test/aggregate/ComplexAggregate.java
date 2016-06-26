package com.dmc.d1.cqrs.test.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.util.NewInstanceFactory;
import com.dmc.d1.test.domain.*;
import com.dmc.d1.test.event.BasketCreatedEvent;
import com.dmc.d1.test.event.BasketCreatedEventBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidclelland on 17/05/2016.
 */
@com.dmc.d1.cqrs.annotations.Aggregate
public class ComplexAggregate extends Aggregate {

    private static String CLASS_NAME = ComplexAggregate.class.getName();

    private Basket basket;

    ComplexAggregate() {
    }

    @Override
    protected void revertState(Aggregate old) {
        ComplexAggregate fromAgg = (ComplexAggregate) old;

        if (fromAgg.basket != null)
            this.basket = BasketBuilder.copyBuilder(fromAgg.basket).buildImmutable();
    }

    public void createBasket(Basket basket) {
        //TODO the basket and its nested objects should not be pooled
//        //to pool -> BasketBuilder.copyBuilder(basket).buildPooledJournalable();

        // buildPooled on builder -> takes in entity, if existing entity is pooled then just return
        // otherwise do as above

        //contrary, if the builder parameter is pooled, and we don't want the copy to be pooled
        //then we likewise need to take a copy

        //if ever we need to change form pooled ->not pooled or not pooled-> pooled then a copy is required
        //Security security2 = SecurityBuilder.copyBuilder(basket.getSecurity()).buildJournalable(false);

        apply(BasketCreatedEventBuilder.startBuilding(getId()).basket(basket).buildPooledJournalable());
    }

    @EventHandler
    public void handleEvent(BasketCreatedEvent event) {
        this.basket = BasketBuilder.copyBuilder(event.getBasket()).buildMutable();
    }

    public Basket getBasket() {
        return basket;
    }

    public static class Factory implements NewInstanceFactory<ComplexAggregate> {

        @Override
        public String getClassName() {
            return CLASS_NAME;
        }

        @Override
        public ComplexAggregate newInstance() {
            return new ComplexAggregate();
        }
    }
}