package com.dmc.d1.cqrs.test.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.util.NewInstanceFactory;
import com.dmc.d1.test.domain.Basket;
import com.dmc.d1.test.domain.BasketBuilder;
import com.dmc.d1.test.domain.BasketConstituent;
import com.dmc.d1.test.event.BasketCreatedEvent;
import com.dmc.d1.test.event.BasketCreatedEventBuilder;

import java.time.LocalDate;
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
    protected void copyState(Aggregate copy) {
        ComplexAggregate agg = (ComplexAggregate) copy;
        //TODO add merge functionality
        this.basket = agg.basket==null ? null : BasketBuilder.mutableCopyBuilder(false, agg.basket).buildMutable(false);
    }

    public void createBasket(Basket basket) {

        apply(BasketCreatedEventBuilder.startBuilding(getId()).basket(basket).buildMutable(true));
    }

    @EventHandler
    public void handleEvent(BasketCreatedEvent event) {
        this.basket = BasketBuilder.mutableCopyBuilder(false, event.getBasket()).buildMutable(false);
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