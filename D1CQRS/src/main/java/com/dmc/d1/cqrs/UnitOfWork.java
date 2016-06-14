package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.event.AggregateEventPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created By davidclelland on 01/06/2016.
 * <p>
 * Holds an ordered set of aggregates that require committing or rolling back
 * <p>
 * The entire stack is either rolled back or committed - if if any of the aggregates
 * require rolling back then all aggregates are rolled back
 */
class UnitOfWork {

    private static final Logger LOG = LoggerFactory.getLogger(UnitOfWork.class);

    private static class Aggregates {
        List<Aggregate> list = new ArrayList<>();
        int counter = 0;
        boolean rollback = false;

        void clear(){
            list.clear();
            counter=0;
            rollback=false;
        }
    }

    private static ThreadLocal<Aggregates> threadLocal = new ThreadLocal<>();

    private UnitOfWork() {
    }

    static <A extends Aggregate> void add(Aggregate aggregate) {

        if (threadLocal.get() == null) {
            threadLocal.set(new Aggregates());
        }
        threadLocal.get().list.add(aggregate);
        threadLocal.get().counter++;
    }

    private static void clear() {
        threadLocal.get().clear();

        if(AggregateEventPool.isInitialised())
            AggregateEventPool.clear();
    }

    //if root then commit -> unless a nested aggregate flagged that the aggregates should be rolled back
    static void commit() {
        if (threadLocal.get() != null) {
            threadLocal.get().counter--;
            if (aggregateIsRoot()) {
                try {
                    if (threadLocal.get().rollback) {
                        doRollback();
                    } else {
                        for(int i = threadLocal.get().list.size()-1;i>=0;i--){
                            Aggregate aggregate = threadLocal.get().list.get(i);
                            aggregate.commit();
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Unexpected error committing aggregates - attempt roll back", e);
                    try {
                        doRollback();
                    }catch(Exception e1){
                        LOG.error("Unable to roll back", e1);
                    }
                } finally {
                    clear();
                }
            }
        }
    }

    private static boolean aggregateIsRoot() {
        return threadLocal.get().counter == 0;
    }

    static void rollback() {
        if (threadLocal.get() != null) {
            threadLocal.get().counter--;

            if (aggregateIsRoot())
                doRollback();
            else {
                threadLocal.get().rollback = true;
            }
        }
    }

    private static void doRollback() {
        try {
            for(int i = threadLocal.get().list.size()-1;i>=0;i--){
                Aggregate aggregate = threadLocal.get().list.get(i);
                aggregate.rollback();
            }
        } catch (Exception e) {
            LOG.error("Unexpected error rolling back aggregates", e);
        } finally {
            clear();
        }
    }
}

