package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.event.AggregateEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class AggregateRepository<A extends Aggregate> {

    private final AggregateEventStore aggregateEventStore;
    private final Map<String, A> cache  = new HashMap<>();
    private final AnnotatedEventHandlerInvoker annotatedEventHandlerInvoker;

    private final Class<A> aggregateType;

    public AggregateRepository(AggregateEventStore aggregateEventStore,  Class<A> aggregateType, AnnotatedMethodInvokerStrategy strategy){
        this.aggregateEventStore = checkNotNull(aggregateEventStore);
        this.aggregateType = checkNotNull(aggregateType);

        this.annotatedEventHandlerInvoker = new ReflectiveAnnotatedEventHandlerInvoker();

    }

    public A create(A aggregate){
        //create aggregate event handler
        aggregate.setEventHandler(annotatedEventHandlerInvoker);
        cache.put(aggregate.getId(), aggregate);
        return aggregate;
    }

    public void commit(String id){
        A aggregate = cache.get(id);

        List<AggregateEvent> uncommittedEvents= aggregate.getUncommittedEvents();
        aggregateEventStore.add(uncommittedEvents);
        aggregate.clearUncommittedEvents();
    }

    public A find(String id){
        return cache.get(id);
    }

    public void rollback(String id){
        A aggregate = cache.get(id);
        Iterable<AggregateEvent> events = aggregateEventStore.get(id);
        aggregate.rollback(events);
    }


    private AnnotatedEventHandlerInvoker getEventHandler(AnnotatedMethodInvokerStrategy strategy) {
        if (AnnotatedMethodInvokerStrategy.GENERATED == strategy) {
            try {
                String className = this.getClass().getSimpleName() + "AnnotatedCommandHandlerInvoker";
                Class<?> clazz  = Class.forName("com.dmc.d1.algo.command." + className);
                return (AnnotatedEventHandlerInvoker) clazz.newInstance();

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Unable to resolve annotated method invoker for " + this.getClass().getSimpleName(), e);
            }
        } else {
            return new ReflectiveAnnotatedEventHandlerInvoker();
        }
    }


}