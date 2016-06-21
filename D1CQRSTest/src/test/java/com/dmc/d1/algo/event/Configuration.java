package com.dmc.d1.algo.event;

import com.dmc.d1.cqrs.InitialisationEventFactory;
import com.dmc.d1.cqrs.util.NewInstanceFactory;
import com.dmc.d1.cqrs.util.Pooled;
import com.dmc.d1.cqrs.util.ThreadLocalObjectPool;
import com.dmc.d1.test.event.TestAggregateInitialisedEventBuilder;
import org.reflections.Reflections;

import java.util.Set;

/**
 * Created By davidclelland on 10/06/2016.
 */

public class Configuration {

    public static final String getChroniclePath() {
        return System.getProperty("java.io.tmpdir") + "/d1-events-" + System.currentTimeMillis();
    }

    public static InitialisationEventFactory initialisationEventFactoryChronicle() {
        return id ->
                TestAggregateInitialisedEventBuilder.startBuilding(id).buildMutable(true);
    }

    public static InitialisationEventFactory initialisationEventFactoryBasic() {
        return id ->
                TestAggregateInitialisedEventBuilder.startBuilding(id).buildMutable(false);
    }

}
