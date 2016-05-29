package com.dmc.d1.algo.command;

import com.dmc.d1.cqrs.command.AnnotatedCommandHandlerInvoker;
import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.testdomain.Aggregate1;
import com.dmc.d1.cqrs.testdomain.command.CreateAggregate1Command;
import com.dmc.d1.cqrs.testdomain.command.MyCommandHandler1;
import com.dmc.d1.cqrs.testdomain.command.UpdateAggregate1Command;

public final class MyCommandHandler1AnnotatedMethodInvoker implements AnnotatedCommandHandlerInvoker<Aggregate1,MyCommandHandler1> {

    @Override
    public void invoke(Command command, MyCommandHandler1 commandHandler) {

        if (command.getName().equals("CreateAggregate1Command")) {
            commandHandler.handle((CreateAggregate1Command) command);
            return;
        }
        if (command.getName().equals("UpdateAggregate1Command")) {
            commandHandler.handle((UpdateAggregate1Command) command);
            return;
        }
    }
}
