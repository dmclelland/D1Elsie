package com.dmc.d1.algo.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.domain.WaveId;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class WaveAggregate extends Aggregate<WaveId>{

    private WaveId waveId;

    public WaveAggregate(WaveId waveId){
        this.waveId = waveId;

    }

    @Override
    protected void rollbackAggregateToInitialState() {

    }

    @Override
    protected WaveId getId() {
        return waveId;
    }
}
