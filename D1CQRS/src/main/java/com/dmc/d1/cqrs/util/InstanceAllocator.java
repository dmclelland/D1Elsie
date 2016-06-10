package com.dmc.d1.cqrs.util;

import com.dmc.d1.cqrs.event.AggregateEvent;

/**
 * Created By davidclelland on 09/06/2016.
 */
public interface InstanceAllocator<E> {

    E allocateInstance(String eventIdentifier);
}
