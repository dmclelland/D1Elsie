package com.dmc.d1.algo.aggregate;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.util.NewInstanceFactory;
import com.dmc.d1.domain.WaveId;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class WaveAggregate extends Aggregate {

    private static String CLASS_NAME = WaveAggregate.class.getName();

    WaveAggregate(){
    }


    @Override
    protected void revertState(Aggregate copy) {
        WaveAggregate agg = (WaveAggregate) copy;
    }



    public static class Factory implements NewInstanceFactory<WaveAggregate> {

        @Override
        public String getClassName() {
            return CLASS_NAME;
        }

        @Override
        public WaveAggregate newInstance() {
            return new WaveAggregate();
        }
    }

}
