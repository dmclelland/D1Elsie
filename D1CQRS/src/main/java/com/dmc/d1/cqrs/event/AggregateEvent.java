package com.dmc.d1.cqrs.event;

import com.dmc.d1.domain.Id;

/**
 * Created by davidclelland on 16/05/2016.
 */
public interface AggregateEvent {
    Id getId();
    public String getSimpleClassName();

}
