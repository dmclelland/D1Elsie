package com.dmc.d1.algo.eventhandler;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.event.AnnotatedEventHandlerInvoker;
import com.dmc.d1.cqrs.test.event.Aggregate1EventHandler;
import com.dmc.d1.test.event.HandledByExternalHandlersEvent;
import com.dmc.d1.test.event.TriggerExceptionInNestedAggregateEvent;

public final class Aggregate1EventHandlerAnnotatedMethodInvoker implements AnnotatedEventHandlerInvoker<Aggregate1EventHandler> {
  public void invoke(AggregateEvent event, Aggregate1EventHandler eventHandler) {
    if (event.getClassName().equals("com.dmc.d1.test.event.HandledByExternalHandlersEvent")) {
      eventHandler.handle((HandledByExternalHandlersEvent)event);
      return;
    }
    if (event.getClassName().equals("com.dmc.d1.test.event.TriggerExceptionInNestedAggregateEvent")) {
      eventHandler.handle((TriggerExceptionInNestedAggregateEvent)event);
      return;
    }
  }
}
