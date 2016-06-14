package com.dmc.d1.cqrs.event;

import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;

/**
 * Created By davidclelland on 12/06/2016.
 */
public class ChronicleAggregateInitialisedEvent extends ChronicleAggregateEvent {

    private final static String CLASS_NAME = ChronicleAggregateInitialisedEvent.class.getName();

    public ChronicleAggregateInitialisedEvent(){
    }

    public void set(String id) {
        setAggregateId(id);
        setClassName(CLASS_NAME);
    }

    @Override
    public void reset() {
        setAggregateId(null);
    }

    @Override
    public void readMarshallable(WireIn wireIn) throws IORuntimeException {
        wireIn.read(()-> "id").text(this, (o, b) -> o.setAggregateId(b));
        setClassName(CLASS_NAME);
        wireIn.read(() -> "aggregateClassName").text(this, (o, b) -> o.setAggregateClassName(b));
    }

    @Override
    public void writeMarshallable(WireOut wireOut) {

        wireOut.write(()-> "id").text(getAggregateId());
        wireOut.write(() -> "aggregateClassName").text(getAggregateClassName());
    }
}