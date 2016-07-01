package com.dmc.d1.algo.eventhandler;

import com.dmc.d1.cqrs.AnnotatedAggregateEventHandlerInvoker;
import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.test.aggregate.ComplexAggregate;
import com.dmc.d1.test.event.BasketCreatedEvent;

public final class ComplexAggregateAnnotatedMethodInvoker implements AnnotatedAggregateEventHandlerInvoker<ComplexAggregate> {
  public void invoke(AggregateEvent event, ComplexAggregate aggregate) {
    if (event.getClassName().equals("com.dmc.d1.test.event.BasketCreatedEvent")) {
      aggregate.handleEvent((BasketCreatedEvent)event);
      return;
    }
  }
}
