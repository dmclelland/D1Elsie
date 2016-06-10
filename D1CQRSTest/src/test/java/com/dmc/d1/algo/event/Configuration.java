package com.dmc.d1.algo.event;

import com.dmc.d1.cqrs.event.EventFactory;
import com.dmc.d1.cqrs.util.InstanceAllocator;

/**
 * Created By davidclelland on 10/06/2016.
 */

public class Configuration {

    public static final String getChroniclePath(){
       return  System.getProperty("java.io.tmpdir") + "/d1-events-"+ System.currentTimeMillis();
    }

    public static InstanceAllocator getInstanceAllocatorChronicle(){
        return new ChronicleInstanceAllocator();
    }

    public static EventFactory getEventFactoryChronicle(){
        return new EventFactoryChronicle();
    }

}
