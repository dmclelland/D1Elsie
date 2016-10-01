package com.dmc.d1.cqrs.sample.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.sample.domain.Basket2;
import com.dmc.d1.sample.domain.Basket2Builder;
import com.dmc.d1.sample.domain.BasketConstituent;
import com.dmc.d1.sample.domain.BasketConstituent2;
import com.dmc.d1.sample.event.*;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Created by davidclelland on 17/05/2016.
 */
@com.dmc.d1.cqrs.annotations.Aggregate
public class ComplexMutableAggregate extends Aggregate<ComplexMutableAggregate> {

    private Basket2 basket;

    ComplexMutableAggregate() {
    }

    public void createBasket2(Basket2 basket) {
        apply(Basket2CreatedEventBuilder.startBuilding(getId())
                .basket(basket)
                .buildJournalable());
    }


    public void updateBasketConstituent(String ric, int adjustedShares) {
        apply(UpdateBasketConstituentEventBuilder.startBuilding(getId())
                .ric(ric)
                .adjustedShares(adjustedShares)
                .lastUpdated(LocalDate.now())
                .buildJournalable());
    }


    public static int noOfExceptions =0;

    public void updateBasketConstituentWithDeterministicException(String ric, int adjustedShares) {


        apply(UpdateBasketConstituentEventWithExceptionBuilder.startBuilding(getId())
                .ric(ric)
                .adjustedShares(adjustedShares)
                .lastUpdated(LocalDate.now())
                .buildJournalable());

        if(this.basket.getBasketConstituents2().get(ric).getInitialAdjustedShares()== 57 ) {
            noOfExceptions++;
            throw new RuntimeException("Adjusted shares 57!");
        }
    }

    @EventHandler
    public void handleEvent(Basket2CreatedEvent event) {
        this.basket = Basket2Builder.copyBuilder(event.getBasket()).buildMutable();
    }

    @EventHandler
    public void handleEvent(UpdateBasketConstituentEvent event) {
        BasketConstituent2 constituent2 = basket.getBasketConstituents2().get(event.getRic());

        constituent2.setAdjustedShares(event.getAdjustedShares());
        constituent2.setLastUpdated(event.getLastUpdated());
    }

    @EventHandler
    public void handleEvent(UpdateBasketConstituentEventWithException event) {
        BasketConstituent2 constituent2 = basket.getBasketConstituents2().get(event.getRic());

        constituent2.setAdjustedShares(event.getAdjustedShares());
        constituent2.setLastUpdated(event.getLastUpdated());
    }


    public Basket2 getBasket() {
        return basket;
    }

    private static Supplier<ComplexMutableAggregate> SUPPLIER = ComplexMutableAggregate::new;

    public static Supplier<ComplexMutableAggregate> newInstanceFactory() {
        return SUPPLIER;
    }

    @Override
    protected ComplexMutableAggregate stateCopy(ComplexMutableAggregate from) {
        this.basket = Basket2Builder.copyBuilder(from.basket).buildMutable();
        return this;
    }
}