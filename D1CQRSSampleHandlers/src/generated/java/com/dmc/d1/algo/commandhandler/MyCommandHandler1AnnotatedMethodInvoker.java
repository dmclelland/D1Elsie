package com.dmc.d1.algo.commandhandler;

import com.dmc.d1.cqrs.AnnotatedCommandHandlerInvoker;
import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.test.aggregate.Aggregate1;
import com.dmc.d1.cqrs.test.command.CreateAggregate1Command;
import com.dmc.d1.cqrs.test.command.NestedExceptionTriggeringAggregate1Command;
import com.dmc.d1.cqrs.test.command.UpdateAggregate1Command;
import com.dmc.d1.cqrs.test.command.UpdateAggregate1Command2;
import com.dmc.d1.cqrs.test.commandhandler.MyCommandHandler1;

public final class MyCommandHandler1AnnotatedMethodInvoker implements AnnotatedCommandHandlerInvoker<Aggregate1, MyCommandHandler1> {
  public void invoke(Command command, MyCommandHandler1 commandHandler, Aggregate1 aggregate) {
    if (command.getClassName().equals("com.dmc.d1.cqrs.test.command.UpdateAggregate1Command2")) {
      commandHandler.handle((UpdateAggregate1Command2)command, aggregate);
      return;
    }
    if (command.getClassName().equals("com.dmc.d1.cqrs.test.command.NestedExceptionTriggeringAggregate1Command")) {
      commandHandler.handle((NestedExceptionTriggeringAggregate1Command)command, aggregate);
      return;
    }
    if (command.getClassName().equals("com.dmc.d1.cqrs.test.command.UpdateAggregate1Command")) {
      commandHandler.handle((UpdateAggregate1Command)command, aggregate);
      return;
    }
    if (command.getClassName().equals("com.dmc.d1.cqrs.test.command.CreateAggregate1Command")) {
      commandHandler.handle((CreateAggregate1Command)command, aggregate);
      return;
    }
  }
}
