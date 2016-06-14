package com.dmc.d1.algo.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.domain.WaveId;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class WaveAggregate extends Aggregate {

    private static String CLASS_NAME = WaveAggregate.class.getName();

    private WaveId waveId;

    public WaveAggregate(WaveId waveId){
        super(waveId.toString(), CLASS_NAME);
        this.waveId = waveId;

    }

    @Override
    protected void rollbackAggregateToInitialState() {

    }

}
