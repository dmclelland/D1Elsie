package com.dmc.d1.cqrs.event;

import com.dmc.d1.cqrs.Utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class SimpleEventBus<H extends AbstractEventHandler> implements EventBus {

    private Map<String, List<H>> eventToHandlers = new HashMap<>();

    public void registerEventHandlers(List<H> eventHandlers) {

        for (H eventHandler : eventHandlers) {
            //register all annotated methods
            for (Method m : Utils.methodsOf(eventHandler.getClass())) {
                if (m.isAnnotationPresent(com.dmc.d1.cqrs.annotations.EventHandler.class)) {
                    if (m.getParameterTypes().length == 1 && AggregateEvent.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        List<H> handlers = eventToHandlers.get(m.getParameterTypes()[0].getName());
                        if (handlers == null)
                            handlers = new ArrayList<>();

                        handlers.add(eventHandler);
                        eventToHandlers.put(m.getParameterTypes()[0].getName(), handlers);
                    } else {
                        throw new IllegalStateException("An event handler must have a single argument assignable from " + AggregateEvent.class.getName());
                    }
                }
            }
        }
    }

    @Override
    public void publish(AggregateEvent event) {
        List<H> handlers = eventToHandlers.get(event.getClassName());
        if (handlers != null) {
            for (H handler : handlers) {
                handler.handleEvent(event);
            }
        }
    }
}