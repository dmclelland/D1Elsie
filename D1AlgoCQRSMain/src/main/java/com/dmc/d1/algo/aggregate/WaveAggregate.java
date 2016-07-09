package com.dmc.d1.algo.aggregate;

import com.dmc.d1.algo.domain.Wave;
import com.dmc.d1.algo.domain.WaveBuilder;
import com.dmc.d1.algo.event.WaveCreatedEvent;
import com.dmc.d1.algo.event.WaveCreatedEventBuilder;
import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.domain.*;

import java.time.LocalDate;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class WaveAggregate extends Aggregate {

    private BasketService basketService;

    WaveAggregate() {
    }

    private Wave wave;

    public void setServices(BasketService basketService){
        this.basketService = checkNotNull(basketService);
    }

    @Override
    protected void revertState(Aggregate copy) {

        WaveAggregate fromAgg = (WaveAggregate) copy;
        if(fromAgg.wave!=null)
            this.wave = WaveBuilder.copyBuilder(fromAgg.wave).buildImmutable();
    }

    public void createWave(WaveId waveId, OrderId orderId,InstrumentId instrumentId,
                           int quantity, TradeDirection tradeDirection, LocalDate tradeDate, UserId userId){

        Wave wave = WaveBuilder.startBuilding()
                .waveId(waveId)
                .orderId(orderId)
                .quantity(quantity)
                .tradeDirection(tradeDirection)
                .tradeDate(tradeDate)
                .userId(userId)
                .buildPooledJournalable();

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
    public void handleEvent(WaveCreatedEvent event){
        this.wave = WaveBuilder.copyBuilder(event.getWave()).buildImmutable();
    }



    private static Supplier<WaveAggregate> SUPPLIER = WaveAggregate::new;

    public static Supplier<WaveAggregate> newInstanceFactory() {
        return SUPPLIER;
    }

}
