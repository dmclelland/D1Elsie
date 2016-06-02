package com.dmc.d1.cqrs.event;

/**
 * Created by davidclelland on 18/05/2016.
 */
public interface AnnotatedEventHandlerInvoker<H extends AbstractEventHandler>{
    void invoke(AggregateEvent event, H eventHandler);
}
