package com.dmc.d1.cqrs.command;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.util.InstanceAllocator;
import com.dmc.d1.cqrs.util.ObjectPool;

import java.util.List;

/**
 * Created By davidclelland on 09/06/2016.
 */
public final class CommandPool {

    private static ThreadLocal<ObjectPool<Command>> THREAD_LOCAL = new ThreadLocal<>();

    private CommandPool(){

    }

    public  static  void initialise(List<String> commands, InstanceAllocator<Command> instanceAllocator) {
        if (THREAD_LOCAL.get() == null) {
            ObjectPool<Command> events = new ObjectPool<>(commands, instanceAllocator, 20);
            THREAD_LOCAL.set(events);
        }
    }

    public static boolean isInitialised() {
        return THREAD_LOCAL.get() != null;
    }


    public static void clear() {
        if (THREAD_LOCAL.get() == null)
            throw new IllegalStateException("Command pool has not been initialized");

        THREAD_LOCAL.get().reset();
    }

    public static Command allocate(String commandIdentifier) {
        if (THREAD_LOCAL.get() == null)
            throw new IllegalStateException("Command pool has not been initialized");

        ObjectPool<Command> pool =  THREAD_LOCAL.get();

        return pool.allocateObject(commandIdentifier);

    }

}