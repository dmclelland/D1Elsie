package com.dmc.d1.cqrs.util;

import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Created By davidclelland on 09/06/2016.
 */
public final class ThreadLocalObjectPool {

    private ThreadLocalObjectPool() {
    }

    private static List<Supplier<? extends Poolable>> POOLABLE_FACTORIES = new ArrayList<>();


    static {
        try {
            Reflections ref = new Reflections("com.dmc.d1");
            Set<Class<? extends Poolable>> pooledSet = ref.getSubTypesOf(Poolable.class);
            for (Class<? extends Poolable> pooled : pooledSet) {
                if (!(Modifier.isAbstract(pooled.getModifiers()) || Modifier.isInterface(pooled.getModifiers()))) {
                    Method m = pooled.getDeclaredMethod("newInstanceFactory", null);
                    m.setAccessible(true);
                    Supplier<? extends Poolable> newInstanceFactory = (Supplier<? extends Poolable>) m.invoke(null);
                    POOLABLE_FACTORIES.add(newInstanceFactory);
                }
            }
        }catch(Exception e){
            throw new RuntimeException("Unable to ", e);
        }
    }

    public  static void initialise(){
    }


//    class WildcardFixed {
//        void foo(List<?> i) {
//            fooHelper(i);
//        }
//        // Helper method created so that the wildcard can be captured
//        // through type inference.
//        private <T> void fooHelper(List<T> l) {
//            l.set(0, l.get(0));
//        }
//    }

    // Thread local variable containing each thread's ID
    private static final ThreadLocal<ObjectPool> THREAD_LOCAL =
            new ThreadLocal<ObjectPool>() {
                @Override
                protected ObjectPool initialValue() {
                    ObjectPool pool = new ObjectPool(20);

                    for ( Supplier<? extends Poolable> supplier : POOLABLE_FACTORIES){
                        pool.createSlot(supplier);
                    }

                    return pool;
                }
            };



    public static void clear() {

        THREAD_LOCAL.get().reset();
    }

    public static <T extends Poolable> T allocateObject(String className){
        ObjectPool<T> pool = THREAD_LOCAL.get();

        return pool.allocateObject(className);
    }

    public static <T extends Poolable> int slotSize(String className){

        ObjectPool<T> pool = THREAD_LOCAL.get();
        return pool.slotSize(className);

    }


    private static <T extends Poolable> void createSlot(Supplier<T> newInstanceFactory) {
        ObjectPool<T> pool = THREAD_LOCAL.get();
        pool.createSlot(newInstanceFactory);
    }



}