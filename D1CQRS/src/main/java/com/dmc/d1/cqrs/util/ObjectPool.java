package com.dmc.d1.cqrs.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created By davidclelland on 09/06/2016.
 */
class ObjectPool<T extends Poolable> {

    private final Map<String, Slot<T>> slots = new HashMap<>();

    private final int size;

    public void reset() {
        slots.values().forEach(p -> {
            if (p.counter > 0) p.clear();
        });
    }

    public void createSlot(Supplier<T> newInstanceFactory) {

        String className = newInstanceFactory.get().getClassName();
        Slot<T> t = slots.get(className);

        if (t == null) {
            t = new Slot(className, size, newInstanceFactory);
            slots.put(className, t);
        }
    }

    public int slotSize(String className){
        return slots.get(className).pool.size();
    }


    public T allocateObject(String className) {

        Slot<T> t = slots.get(className);
        if (t == null)
            throw new IllegalStateException("Unable to allocate an object for " + className + " as a slot hasn't been created");

        return t.getEntry();
    }

    public ObjectPool(int size) {
        this.size = size;
    }

    private static class Slot<T extends Poolable> {
        final String className;
        final int initialSlotSize;
        final List<T> pool;
        final Supplier<T> newInstanceFactory;
        int counter = 0;

        public Slot(String className, int initialSlotSize, Supplier<T> newInstanceFactory) {
            this.initialSlotSize = initialSlotSize;
            this.className = className;
            this.pool = new ArrayList<>(initialSlotSize);
            this.newInstanceFactory = checkNotNull(newInstanceFactory);
            increasePoolSize();
        }

        T getEntry() {
            if (counter == pool.size()) {
                increasePoolSize();
            }
            return pool.get(counter++);
        }

        private void increasePoolSize() {
            for (int i = 0; i < initialSlotSize; i++) {
                pool.add(newInstanceFactory.get());
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