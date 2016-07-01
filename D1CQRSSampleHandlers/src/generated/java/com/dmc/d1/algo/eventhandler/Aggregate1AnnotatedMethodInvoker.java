package com.dmc.d1.algo.eventhandler;

import com.dmc.d1.cqrs.AnnotatedAggregateEventHandlerInvoker;
import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.test.aggregate.Aggregate1;
import com.dmc.d1.test.event.HandledByExternalHandlersEvent;
import com.dmc.d1.test.event.IntUpdatedEvent1;
import com.dmc.d1.test.event.IntUpdatedEvent2;
import com.dmc.d1.test.event.TriggerExceptionInNestedAggregateEvent;

public final class Aggregate1AnnotatedMethodInvoker implements AnnotatedAggregateEventHandlerInvoker<Aggregate1> {
  public void invoke(AggregateEvent event, Aggregate1 aggregate) {
    if (event.getClassName().equals("com.dmc.d1.test.event.IntUpdatedEvent2")) {
      aggregate.handleEvent2((IntUpdatedEvent2)event);
      return;
    }
    if (event.getClassName().equals("com.dmc.d1.test.event.HandledByExternalHandlersEvent")) {
      aggregate.handleEvent3((HandledByExternalHandlersEvent)event);
      return;
    }
    if (event.getClassName().equals("com.dmc.d1.test.event.TriggerExceptionInNestedAggregateEvent")) {
      aggregate.handleEvent3((TriggerExceptionInNestedAggregateEvent)event);
      return;
    }
    if (event.getClassName().equals("com.dmc.d1.test.event.IntUpdatedEvent1")) {
      aggregate.handleEvent1((IntUpdatedEvent1)event);
      return;
    }
  }
}
