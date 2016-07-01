package com.dmc.d1.algo.commandhandler;

import com.dmc.d1.cqrs.AnnotatedCommandHandlerInvoker;
import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.test.aggregate.NestedAggregate1;
import com.dmc.d1.cqrs.test.command.CreateNestedAggregate1Command;
import com.dmc.d1.cqrs.test.command.ExceptionTriggeringNestedAggregateCommand;
import com.dmc.d1.cqrs.test.commandhandler.MyNestedCommandHandler1;

public final class MyNestedCommandHandler1AnnotatedMethodInvoker implements AnnotatedCommandHandlerInvoker<NestedAggregate1, MyNestedCommandHandler1> {
  public void invoke(Command command, MyNestedCommandHandler1 commandHandler) {
    if (command.getClassName().equals("com.dmc.d1.cqrs.test.command.CreateNestedAggregate1Command")) {
      commandHandler.handle((CreateNestedAggregate1Command)command);
      return;
    }
    if (command.getClassName().equals("com.dmc.d1.cqrs.test.command.ExceptionTriggeringNestedAggregateCommand")) {
      commandHandler.handle((ExceptionTriggeringNestedAggregateCommand)command);
      return;
    }
  }
}
