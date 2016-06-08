package com.dmc.d1.cqrs.event;

import net.openhft.chronicle.wire.Marshallable;

/**
 * Created by davidclelland on 16/05/2016.
 */
public interface ChronicleAggregateEvent extends AggregateEvent, Marshallable{


}
