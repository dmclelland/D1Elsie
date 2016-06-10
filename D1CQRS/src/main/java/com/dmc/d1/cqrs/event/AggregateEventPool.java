package com.dmc.d1.cqrs.event;

import com.dmc.d1.cqrs.util.InstanceAllocator;
import com.dmc.d1.cqrs.util.ObjectPool;

import java.util.List;

/**
 * Created By davidclelland on 09/06/2016.
 */
public class AggregateEventPool {

    private static ThreadLocal<ObjectPool<? extends AggregateEvent>> THREAD_LOCAL = new ThreadLocal<>();

    public  static <T extends AggregateEvent>  void initialise(List<String> eventNames, InstanceAllocator<T> instanceAllocator) {
        if (THREAD_LOCAL.get() == null) {
            ObjectPool<T> events = new ObjectPool<>(eventNames, instanceAllocator, 20);
            THREAD_LOCAL.set(events);
        }
    }



    public static boolean isInitialised() {
        return THREAD_LOCAL.get() != null;
    }


    public static void clear() {
        if (THREAD_LOCAL.get() == null)
            throw new IllegalStateException("Event pool has not been initialized");

        THREAD_LOCAL.get().reset();
    }

    public static <T extends AggregateEvent> T allocate(String eventIdentifier) {
        if (THREAD_LOCAL.get() == null)
            throw new IllegalStateException("Event pool has not been initialized");

        ObjectPool<? extends AggregateEvent> pool =  THREAD_LOCAL.get();

        AggregateEvent event = pool.allocateObject(eventIdentifier);

        //noinspection unchecked
        return (T)event;
    }

}