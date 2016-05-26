package com.dmc.d1.algo.command;

import com.dmc.d1.cqrs.command.AnnotatedMethodInvoker;
import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.testdomain.command.CreateAggregate1Command;
import com.dmc.d1.cqrs.testdomain.command.MyCommandHandler1;
import com.dmc.d1.cqrs.testdomain.command.UpdateAggregate1Command;

public final class MyCommandHandler1AnnotatedMethodInvoker implements AnnotatedMethodInvoker<MyCommandHandler1> {


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
