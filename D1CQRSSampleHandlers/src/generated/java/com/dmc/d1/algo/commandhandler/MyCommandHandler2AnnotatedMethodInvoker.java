package com.dmc.d1.algo.commandhandler;

import com.dmc.d1.cqrs.AnnotatedCommandHandlerInvoker;
import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.test.aggregate.Aggregate2;
import com.dmc.d1.cqrs.test.command.CreateAggregate2Command;
import com.dmc.d1.cqrs.test.command.ExceptionTriggeringAggregate2Command;
import com.dmc.d1.cqrs.test.command.UpdateAggregate2Command;
import com.dmc.d1.cqrs.test.commandhandler.MyCommandHandler2;

public final class MyCommandHandler2AnnotatedMethodInvoker implements AnnotatedCommandHandlerInvoker<Aggregate2, MyCommandHandler2> {
  public void invoke(Command command, MyCommandHandler2 commandHandler) {
    if (command.getClassName().equals("com.dmc.d1.cqrs.test.command.UpdateAggregate2Command")) {
      commandHandler.handle((UpdateAggregate2Command)command);
      return;
    }
    if (command.getClassName().equals("com.dmc.d1.cqrs.test.command.ExceptionTriggeringAggregate2Command")) {
      commandHandler.handle((ExceptionTriggeringAggregate2Command)command);
      return;
    }
    if (command.getClassName().equals("com.dmc.d1.cqrs.test.command.CreateAggregate2Command")) {
      commandHandler.handle((CreateAggregate2Command)command);
      return;
    }
  }
}
