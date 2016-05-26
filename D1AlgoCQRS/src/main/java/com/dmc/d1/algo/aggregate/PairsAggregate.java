package com.dmc.d1.algo.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.domain.PairId;
import com.dmc.d1.domain.WaveId;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class PairsAggregate extends Aggregate<PairId>{

    private PairId pairId;

    public PairsAggregate(PairId pairId){
        this.pairId = pairId;

    }

    @Override
    protected void rollbackAggregateToInitialState() {

    }

    @Override
    protected PairId getId() {
        return pairId;
    }
}
