package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.event.AggregateEvent;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by davidclelland on 16/05/2016.
 */
class ReflectiveAggregateEventHandler implements AggregateEventHandler {

    private final static Map<String, Method> HANDLERS = new HashMap<>();

    static{
        Reflections reflections = new Reflections("com.dmc.d1");

        Set<Class<? extends  Aggregate>> aggregateClasses = reflections.getSubTypesOf(Aggregate.class);

        for(Class<? extends  Aggregate> aggregateClass : aggregateClasses) {
            //register all annotated methods
            for (Method m : Utils.methodsOf(aggregateClass)) {
                if (m.isAnnotationPresent(EventHandler.class)) {
                    if (m.getParameterTypes().length == 1 && AggregateEvent.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        HANDLERS.put(m.getParameterTypes()[0].getSimpleName(), m);
                    }else{
                        throw new IllegalStateException("An event handler must have a single argument of type " + AggregateEvent.class.getName());
                    }
                }
            }
        }
    }

    @Override
    public void invoke(AggregateEvent event, Aggregate aggregate) {
        Method method = HANDLERS.get(event.getSimpleClassName());

        if(method==null)
            throw new IllegalStateException("No aggregate handler exists for event " + event.toString());

        try {
            method.invoke(aggregate, event);
            aggregate.addToUncommitted(event);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }
}
