package com.dmc.d1.algo.eventhandler;

import com.dmc.d1.cqrs.AnnotatedAggregateEventHandlerInvoker;
import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.test.aggregate.NestedAggregate1;
import com.dmc.d1.test.event.NestedUpdatedEvent1;
import com.dmc.d1.test.event.TriggerExceptionNestedEvent;

public final class NestedAggregate1AnnotatedMethodInvoker implements AnnotatedAggregateEventHandlerInvoker<NestedAggregate1> {
  public void invoke(AggregateEvent event, NestedAggregate1 aggregate) {
    if (event.getClassName().equals("com.dmc.d1.test.event.NestedUpdatedEvent1")) {
      aggregate.handleEvent((NestedUpdatedEvent1)event);
      return;
    }
    if (event.getClassName().equals("com.dmc.d1.test.event.TriggerExceptionNestedEvent")) {
      aggregate.handleEvent((TriggerExceptionNestedEvent)event);
      return;
    }
  }
}
