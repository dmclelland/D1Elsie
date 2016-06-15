package com.dmc.d1.cqrs.util;

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

    public T allocateObject(String className) {
        EntityPool<T> t  =  pool.get(className);
        return t.getEntry();
    }

    public ObjectPool(List<String> classNames, InstanceAllocator<T> instanceAllocator, int size) {
        classNames.forEach(e -> {
            pool.put(e, new EntityPool(e, size, instanceAllocator));
        });
    }
    private static class EntityPool<T> {
        final String className;
        final int initialPoolSize;
        final List<T> pool;
        final InstanceAllocator<T> instanceAllocator;
        int counter = 0;

        public EntityPool(String className, int initialPoolSize, InstanceAllocator<T> instanceAllocator) {
            this.initialPoolSize = initialPoolSize;
            this.className = className;
            this.pool = new ArrayList<>(initialPoolSize);
            this.instanceAllocator = checkNotNull(instanceAllocator);
            increasePoolSize();
        }

        T getEntry() {
            if (counter == pool.size()) {
                increasePoolSize();
            }
            return pool.get(counter++);
        }

        private void increasePoolSize() {

            for (int i = 0; i < initialPoolSize; i++) {
                pool.add(instanceAllocator.allocateInstance(className));
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