package com.dmc.algo.nostrings;

import com.dmc.algo.Ric;
import com.dmc.d1.cqrs.codegen.Journalable;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasketNoStringJournalable implements Journalable, Marshallable {

    public Ric ric;

    public LocalDate tradeDate;

    public int divisor;

    public SecurityNoStringJournalable security;

    public List<BasketConstituentNoStringJournalable> basketConstituents = new ArrayList();

    public Map<Ric, BasketConstituentNoStringJournalable> basketConstituents2 = new HashMap();


    public Ric getRic() {
        return ric;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }


    public int getDivisor() {
        return divisor;
    }


    public SecurityNoStringJournalable getSecurity() {
        return security;
    }


    public List<BasketConstituentNoStringJournalable> getBasketConstituents() {
        return basketConstituents;
    }


    public Map<Ric, BasketConstituentNoStringJournalable> getBasketConstituents2() {
        return basketConstituents2;
    }

    @Override
    public void readMarshallable(WireIn wireIn) throws IORuntimeException {
        wireIn.read(() -> "ric").asEnum(Ric.class, this, (o, b) -> o.ric = b);
        wireIn.read(() -> "tradeDate").date(this, (o, b) -> o.tradeDate = b);
        wireIn.read(() -> "divisor").int32(this, (o, b) -> o.divisor = b);
        wireIn.read(() -> "security").object(SecurityNoStringJournalable.class, this, (o, b) -> o.security = b);
        wireIn.read(() -> "basketConstituents").sequence(this.basketConstituents, (l, v) -> {
            while (v.hasNextSequenceItem()) {
                l.add(v.object(BasketConstituentNoStringJournalable.class));
            }
        });
        wireIn.read(() -> "basketConstituents2").sequence(this.basketConstituents2, (l, v) -> {
            while (v.hasNextSequenceItem()) {
                BasketConstituentNoStringJournalable o = v.object(BasketConstituentNoStringJournalable.class);
                l.put(o.getRic(), o);
            }
        });
    }

    @Override
    public void writeMarshallable(WireOut wireOut) {
        wireOut.write(() -> "ric").asEnum(ric);
        wireOut.write(() -> "tradeDate").date(tradeDate);
        wireOut.write(() -> "divisor").int32(divisor);
        wireOut.write(() -> "security").object(SecurityNoStringJournalable.class, this.security);
        wireOut.write(() -> "basketConstituents").sequence(this.basketConstituents);
        wireOut.write(() -> "basketConstituents2").sequence(this.basketConstituents2.values());
    }


}
