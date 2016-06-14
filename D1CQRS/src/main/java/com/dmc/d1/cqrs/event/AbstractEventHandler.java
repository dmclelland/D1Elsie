package com.dmc.d1.cqrs.event;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 16/05/2016.
 */
public abstract class AbstractEventHandler {

    private final AnnotatedEventHandlerInvoker annotatedEventHandlerInvoker;

    protected AbstractEventHandler() {
        this.annotatedEventHandlerInvoker = checkNotNull(getMethodInvoker());
    }

    public void handleEvent(AggregateEvent event) {
        annotatedEventHandlerInvoker.invoke(event, this);
    }

    @SuppressWarnings("rawtypes")
    private AnnotatedEventHandlerInvoker getMethodInvoker() {
        try {
            String className = this.getClass().getSimpleName() + "AnnotatedMethodInvoker";
            Class<?> clazz = Class.forName("com.dmc.d1.algo.eventhandler." + className);
            return (AnnotatedEventHandlerInvoker) clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to resolve annotated method invoker for " + this.getClass().getSimpleName(), e);
        }
    }
}
