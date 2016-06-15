package com.dmc.d1.cqrs.event;

import com.dmc.d1.cqrs.util.InstanceAllocator;
import com.dmc.d1.cqrs.util.ObjectPool;

import java.util.List;

/**
 * Created By davidclelland on 09/06/2016.
 */
public final class AggregateEventPool {

    private AggregateEventPool(){
    }

    private static ThreadLocal<ObjectPool<AggregateEvent>> THREAD_LOCAL = new ThreadLocal<>();

    public  static  void initialise(List<String> eventNames, InstanceAllocator<AggregateEvent> instanceAllocator) {
        if (THREAD_LOCAL.get() == null) {
            ObjectPool<AggregateEvent> events = new ObjectPool<>(eventNames, instanceAllocator, 20);
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

    public static AggregateEvent allocate(String eventIdentifier) {
        if (THREAD_LOCAL.get() == null)
            throw new IllegalStateException("Event pool has not been initialized");

        ObjectPool<AggregateEvent> pool =  THREAD_LOCAL.get();

        return pool.allocateObject(eventIdentifier);

    }

}