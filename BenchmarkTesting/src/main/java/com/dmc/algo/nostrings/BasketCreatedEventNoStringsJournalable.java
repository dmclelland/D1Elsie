package com.dmc.algo.nostrings;

import com.dmc.d1.cqrs.codegen.Journalable;
import com.dmc.d1.domain.TradeDirection;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;

public class BasketCreatedEventNoStringsJournalable implements Marshallable, Journalable {

    public int aggregateId;
    public TradeDirection tradeDirection;
    public BasketNoStringJournalable basket;

    public BasketNoStringJournalable getBasket() {
        return basket;
    }

    @Override
    public void readMarshallable(WireIn wireIn) throws IORuntimeException {
        wireIn.read(() -> "aggregateId").int32(this, (o, b) -> o.aggregateId = b);
        wireIn.read(() -> "tradeDirection").asEnum(TradeDirection.class, this, (o, b) -> o.tradeDirection = b);

        wireIn.read(() -> "basket").object(BasketNoStringJournalable.class, this, (o, b) -> o.basket = b);
    }

    @Override
    public void writeMarshallable(WireOut wireOut) {
        wireOut.write(() -> "aggregateId").int32(aggregateId);
        wireOut.write(() -> "tradeDirection").asEnum(tradeDirection);
        wireOut.write(() -> "basket").object(BasketNoStringJournalable.class, this.basket);
    }

}
