package com.dmc.d1.algo.command;

import com.dmc.d1.cqrs.command.AnnotatedMethodInvoker;
import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.testdomain.command.*;

public final class MyCommandHandler2AnnotatedMethodInvoker implements AnnotatedMethodInvoker<MyCommandHandler2> {

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
