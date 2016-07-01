package com.dmc.d1.algo.eventhandler;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.event.AnnotatedEventHandlerInvoker;
import com.dmc.d1.cqrs.test.event.Aggregate1EventHandler2;
import com.dmc.d1.test.event.HandledByExternalHandlersEvent;

public final class Aggregate1EventHandler2AnnotatedMethodInvoker implements AnnotatedEventHandlerInvoker<Aggregate1EventHandler2> {
  public void invoke(AggregateEvent event, Aggregate1EventHandler2 eventHandler) {
    if (event.getClassName().equals("com.dmc.d1.test.event.HandledByExternalHandlersEvent")) {
      eventHandler.handle((HandledByExternalHandlersEvent)event);
      return;
    }
  }
}
