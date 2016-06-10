package com.dmc.d1.cqrs.util;

import com.dmc.d1.cqrs.event.AggregateEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created By davidclelland on 09/06/2016.
 */
public class ObjectPool<T> {

    private final Map<String, EntityPool> pool = new HashMap<>();

    public void reset() {
        pool.values().forEach(p ->p.clear());
    }

    public T allocateObject(String eventIdentifier) {

        EntityPool<T> t  =  pool.get(eventIdentifier);

        return t.getEntry();
    }


    public ObjectPool(List<String> objectIdentifiers, InstanceAllocator<T> instanceAllocator, int size) {
        objectIdentifiers.forEach(e -> {
            pool.put(e, new EntityPool(e, size, instanceAllocator));
        });
    }
    private static class EntityPool<T> {
        final String eventIdentifier;
        final int poolSize;
        final List<T> pool;
        final InstanceAllocator<T> instanceAllocator;
        int counter = 0;

        public EntityPool(String eventIdentifier, int poolSize, InstanceAllocator instanceAllocator) {
            this.poolSize = poolSize;
            this.eventIdentifier = eventIdentifier;
            this.pool = new ArrayList<>(poolSize);
            this.instanceAllocator = checkNotNull(instanceAllocator);
            increasePoolSize();
        }

        T getEntry() {
            if (counter == poolSize) {
                increasePoolSize();
            }
            return pool.get(counter++);
        }

        private void increasePoolSize() {

            for (int i = 0; i < poolSize; i++) {
                pool.add(instanceAllocator.allocateInstance(eventIdentifier));
            }
        }

        private void clear() {
            for (int i = 0; i < counter; i++) {
                T e = pool.get(i);
                if (e instanceof Resettable) {
                    ((Resettable) e).reset();
                }
            }
            this.counter = 0;
        }
    }
}