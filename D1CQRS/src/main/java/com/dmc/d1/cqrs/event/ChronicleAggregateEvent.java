package com.dmc.d1.cqrs.event;

import com.dmc.d1.cqrs.event.AggregateEvent;
import net.openhft.lang.io.serialization.BytesMarshallable;

/**
 * Created by davidclelland on 16/05/2016.
 */
public interface ChronicleAggregateEvent extends AggregateEvent, BytesMarshallable{


}
