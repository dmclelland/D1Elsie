package com.dmc.d1.cqrs.util;

import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * Created By davidclelland on 09/06/2016.
 */
public final class ThreadLocalObjectPool {

    private ThreadLocalObjectPool() {
    }

    private static ThreadLocal<ObjectPool> THREAD_LOCAL = new ThreadLocal<>();

    public static <T extends Pooled> void initialise() throws Exception {
        if (THREAD_LOCAL.get() == null) {
            ObjectPool<T> pool = new ObjectPool(20);
            THREAD_LOCAL.set(pool);

            Reflections ref = new Reflections("com.dmc.d1");
            Set<Class<? extends Pooled>> pooledSet = ref.getSubTypesOf(Pooled.class);

            //for every pooled concrete object set up a slot
            for (Class<? extends Pooled> pooled : pooledSet) {
                if (!(Modifier.isAbstract(pooled.getModifiers()) || Modifier.isInterface(pooled.getModifiers()))) {
                    Method m = pooled.getDeclaredMethod("newInstanceFactory", null);
                    m.setAccessible(true);
                    NewInstanceFactory<T> newInstanceFactory = (NewInstanceFactory<T>) m.invoke(null);
                    createSlot(newInstanceFactory);
                }
            }
        }
    }

    public static boolean isInitialised() {
        return THREAD_LOCAL.get() != null;
    }

    public static void clear() {
        if (THREAD_LOCAL.get() == null)
            throw new IllegalStateException("Object pool has not been initialized");

        THREAD_LOCAL.get().reset();
    }

    public static <T> T allocateObject(String className){
        if (THREAD_LOCAL.get() == null)
            throw new IllegalStateException("Object pool has not been initialized");

        ObjectPool<T> pool = THREAD_LOCAL.get();

        return pool.allocateObject(className);
    }

    public static <T> int slotSize(String className){
        if (THREAD_LOCAL.get() == null)
            throw new IllegalStateException("Object pool has not been initialized");

        ObjectPool<T> pool = THREAD_LOCAL.get();
        return pool.slotSize(className);

    }


    private static <T> void createSlot(NewInstanceFactory<T> newInstanceFactory) {
        if (THREAD_LOCAL.get() == null)
            throw new IllegalStateException("Object pool has not been initialized");

        ObjectPool<T> pool = THREAD_LOCAL.get();
        pool.createSlot(newInstanceFactory);
    }



}