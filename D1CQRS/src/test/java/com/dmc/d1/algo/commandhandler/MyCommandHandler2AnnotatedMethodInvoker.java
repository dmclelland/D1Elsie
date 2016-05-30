package com.dmc.d1.algo.commandhandler;

import com.dmc.d1.cqrs.command.AnnotatedCommandHandlerInvoker;
import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.testdomain.Aggregate2;
import com.dmc.d1.cqrs.testdomain.command.*;

public final class MyCommandHandler2AnnotatedMethodInvoker implements AnnotatedCommandHandlerInvoker<Aggregate2, MyCommandHandler2> {

    @Override
    public void invoke(Command command, MyCommandHandler2 commandHandler ) {

        if (command.getName().equals("CreateAggregate2Command")) {
            commandHandler.handle((CreateAggregate2Command) command);
            return;
        }

        if (command.getName().equals("UpdateAggregate2Command")) {
            commandHandler.handle((UpdateAggregate2Command) command);
            return;
        }
    }
}
