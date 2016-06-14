package com.dmc.d1.algo.event;

import com.dmc.d1.cqrs.event.AggregateEventAbstract;
import com.dmc.d1.domain.WaveId;

/**
 * Created By davidclelland on 30/05/2016.
 */
public class WaveCreatedEventAbstract extends AggregateEventAbstract {
    private static final String CLASS_NAME = WaveCreatedEventAbstract.class.getName();

    private final WaveId waveId;

    public WaveCreatedEventAbstract(WaveId waveId) {
        setClassName(CLASS_NAME);
        setAggregateId(waveId.toString());

        this.waveId = waveId;
    }

    public WaveId getWaveId() {
        return waveId;
    }
}
