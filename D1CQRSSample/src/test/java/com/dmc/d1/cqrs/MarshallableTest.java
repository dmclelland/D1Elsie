package com.dmc.d1.cqrs;

import com.dmc.d1.sample.domain.Basket;
import com.dmc.d1.sample.domain.BasketBuilder;
import com.dmc.d1.sample.domain.SecurityBuilder;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.TextWire;
import net.openhft.chronicle.wire.Wire;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created By davidclelland on 29/06/2016.
 */
public class MarshallableTest {



    @Test
    public void testBytesMarshallable() {

        //ClassAliasPool.CLASS_ALIASES.addAlias(SecurityChronicle.class);
        //ClassAliasPool.CLASS_ALIASES.addAlias(BasketConstituentChronicle.class);
        Wire wire = new TextWire(Bytes.elasticByteBuffer());
        Basket basket = TestBasketBuilder.createBasket(111, 100);
        ((Marshallable) basket).writeMarshallable(wire);

        System.out.println(wire);


        Basket basket2 = BasketBuilder.startBuilding()
                .security(SecurityBuilder.startBuilding().buildJournalable()).buildJournalable();
        ((Marshallable) basket2).readMarshallable(wire);

        assertEquals(basket.getDivisor(), basket2.getDivisor());
        assertEquals(basket.getSecurity().getName(), basket2.getSecurity().getName());


    }
}
