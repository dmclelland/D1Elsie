package com.dmc.d1.algo.eventhandler;

import com.dmc.d1.cqrs.AnnotatedAggregateEventHandlerInvoker;
import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.test.aggregate.Aggregate2;
import com.dmc.d1.test.event.StringUpdatedEvent1;
import com.dmc.d1.test.event.StringUpdatedEvent2;
import com.dmc.d1.test.event.TriggerExceptionEvent;

public final class Aggregate2AnnotatedMethodInvoker implements AnnotatedAggregateEventHandlerInvoker<Aggregate2> {
  public void invoke(AggregateEvent event, Aggregate2 aggregate) {
    if (event.getClassName().equals("com.dmc.d1.test.event.StringUpdatedEvent2")) {
      aggregate.handleEvent2((StringUpdatedEvent2)event);
      return;
    }
    if (event.getClassName().equals("com.dmc.d1.test.event.TriggerExceptionEvent")) {
      aggregate.handleEvent2((TriggerExceptionEvent)event);
      return;
    }
    if (event.getClassName().equals("com.dmc.d1.test.event.StringUpdatedEvent1")) {
      aggregate.handleEvent1((StringUpdatedEvent1)event);
      return;
    }
  }
}
