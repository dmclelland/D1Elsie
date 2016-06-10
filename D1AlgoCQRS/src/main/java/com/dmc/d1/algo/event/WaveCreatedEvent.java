package com.dmc.d1.algo.event;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.domain.WaveId;

/**
 * Created By davidclelland on 30/05/2016.
 */
public class WaveCreatedEvent implements AggregateEvent {
    private static final String CLASS_NAME = WaveCreatedEvent.class.getName();

    private final WaveId waveId;

    public WaveCreatedEvent(WaveId waveId) {
        this.waveId = waveId;
    }

    @Override
    public String getAggregateId() {
        return waveId.toString();
    }

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

}
