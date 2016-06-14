package com.dmc.d1.algo.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.domain.Id;
import com.dmc.d1.domain.PairId;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class PairsAggregate extends Aggregate{

    private static String CLASS_NAME = PairsAggregate.class.getName();


    private PairId pairId;

    public PairsAggregate(PairId pairId){
        super(pairId.asString(), CLASS_NAME);

        this.pairId = pairId;
    }

    @Override
    protected void rollbackAggregateToInitialState() {

    }


}
