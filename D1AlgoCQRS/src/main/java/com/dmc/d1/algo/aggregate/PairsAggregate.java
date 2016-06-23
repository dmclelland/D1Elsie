package com.dmc.d1.algo.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.util.NewInstanceFactory;
import com.dmc.d1.domain.Id;
import com.dmc.d1.domain.PairId;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class PairsAggregate extends Aggregate{

    private static String CLASS_NAME = PairsAggregate.class.getName();

    PairsAggregate(){
    }


    @Override
    protected void revertState(Aggregate copy) {
        PairsAggregate agg = (PairsAggregate) copy;
    }



    public static class Factory implements NewInstanceFactory<PairsAggregate> {

        @Override
        public String getClassName() {
            return CLASS_NAME;
        }

        @Override
        public PairsAggregate newInstance() {
            return new PairsAggregate();
        }
    }


}
