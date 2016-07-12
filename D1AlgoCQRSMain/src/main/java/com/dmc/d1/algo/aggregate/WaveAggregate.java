package com.dmc.d1.algo.aggregate;

import com.dmc.d1.algo.domain.Wave;
import com.dmc.d1.algo.domain.WaveBuilder;
import com.dmc.d1.algo.event.WaveCreatedEvent;
import com.dmc.d1.algo.event.WaveCreatedEventBuilder;
import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.domain.*;

import java.time.LocalDate;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class WaveAggregate extends Aggregate<WaveAggregate> {

    private BasketService basketService;

    WaveAggregate() {
    }

    private Wave wave;

    public void createWave(WaveId waveId, OrderId orderId, InstrumentId instrumentId,
                           int quantity, TradeDirection tradeDirection, LocalDate tradeDate, UserId userId) {

        Wave wave = WaveBuilder.startBuilding()
                .waveId(waveId)
                .orderId(orderId)
                .quantity(quantity)
                .tradeDirection(tradeDirection)
                .tradeDate(tradeDate)
                .userId(userId)
                .buildJournalable();

        apply(WaveCreatedEventBuilder.startBuilding(waveId.asString())
                .wave(wave).buildPooledJournalable());

        Basket theoretical = basketService.createBasket(instrumentId, quantity);
        //optimise


        //now get basket for quantity of security
        //need theoretical and optimised (if optimised)

        //from basket create list order, send list order to build
        //list order is mutable -> the execution report on the legs can have
        //the orderstatus/quantity/price  modified.
    }

    @EventHandler
    public void handleEvent(WaveCreatedEvent event) {
        this.wave = WaveBuilder.copyBuilder(event.getWave()).buildImmutable();
    }

    @Override
    protected WaveAggregate stateCopy(WaveAggregate orig) {
        this.wave = WaveBuilder.copyBuilder(orig.wave).buildImmutable();

        return this;
    }


    public static class WaveSupplier implements Supplier<WaveAggregate> {

        private final BasketService basketService;

        public WaveSupplier(BasketService basketService) {
            this.basketService = checkNotNull(basketService);
        }

        @Override
        public WaveAggregate get() {
            WaveAggregate aggregate = new WaveAggregate();
            aggregate.basketService = basketService;
            return aggregate;
        }
    }
}