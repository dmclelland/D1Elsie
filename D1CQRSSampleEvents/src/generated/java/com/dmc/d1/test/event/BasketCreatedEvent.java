package com.dmc.d1.test.event;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.util.StateEquals;
import com.dmc.d1.test.domain.Basket;

public interface BasketCreatedEvent extends AggregateEvent, StateEquals<BasketCreatedEvent> {
  Basket getBasket();
}
