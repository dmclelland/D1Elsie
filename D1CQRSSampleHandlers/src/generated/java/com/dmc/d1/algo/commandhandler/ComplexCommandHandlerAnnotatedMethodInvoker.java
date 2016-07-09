package com.dmc.d1.algo.commandhandler;

import com.dmc.d1.cqrs.AnnotatedCommandHandlerInvoker;
import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.test.aggregate.ComplexAggregate;
import com.dmc.d1.cqrs.test.command.CreateComplexAggregateCommand;
import com.dmc.d1.cqrs.test.commandhandler.ComplexCommandHandler;

public final class ComplexCommandHandlerAnnotatedMethodInvoker implements AnnotatedCommandHandlerInvoker<ComplexAggregate, ComplexCommandHandler> {
  public void invoke(Command command, ComplexCommandHandler commandHandler, ComplexAggregate aggregate) {
    if (command.getClassName().equals("com.dmc.d1.cqrs.test.command.CreateComplexAggregateCommand")) {
      commandHandler.handle((CreateComplexAggregateCommand)command, aggregate);
      return;
    }
  }
}
