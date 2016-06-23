package com.dmc.d1.cqrs.event;

import com.dmc.d1.cqrs.util.Poolable;
import com.dmc.d1.cqrs.util.Resettable;
import net.openhft.chronicle.wire.Marshallable;

/**
 * Created by davidclelland on 16/05/2016.
 */
public abstract class ChronicleAggregateEvent extends AggregateEventAbstract implements Resettable, Marshallable,Poolable {
}
