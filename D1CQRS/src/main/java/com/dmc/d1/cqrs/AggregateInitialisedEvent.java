package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.event.JournalableAggregateEvent;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;

/**
 * Created By davidclelland on 14/06/2016.
 */
class AggregateInitialisedEvent extends JournalableAggregateEvent {


    static final String CLASS_NAME = AggregateInitialisedEvent.class.getName();

    private String aggregateClassName;

    public AggregateInitialisedEvent() {
        setClassName(CLASS_NAME);
    }


    @Override
    public void readMarshallable(WireIn wireIn) throws IORuntimeException {
        wireIn.read(() -> "aggregateId").int64(this, (o, b) -> o.setAggregateId(b));
        setClassName(CLASS_NAME);
        wireIn.read(() -> "aggregateClassName").text(this, (o, b) -> o.aggregateClassName = b);

    }

    @Override
    public void writeMarshallable(WireOut wireOut) {
        wireOut.write(() -> "aggregateId").int64(getAggregateId());
        wireOut.write(() -> "aggregateClassName").text(getAggregateClassName());

    }

    @Override
    public String getAggregateClassName() {
        return aggregateClassName;
    }

    public void setAggregateClassName(String aggregateClassName) {
        this.aggregateClassName = aggregateClassName;
    }
}
