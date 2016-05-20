package com.dmc.d1.cqrs.testdomain.command;

import com.dmc.d1.cqrs.command.AbstractCommandHandler;
import com.dmc.d1.cqrs.command.AbstractDirectMethodInvoker;
import com.dmc.d1.cqrs.command.Command;

import java.util.List;

public final class DirectAnnotatedMethodInvoker extends AbstractDirectMethodInvoker {
    private final List<? extends AbstractCommandHandler> commandHandlers;

    public DirectAnnotatedMethodInvoker(List<? extends AbstractCommandHandler> commandHandlers) {
        super(commandHandlers);
        this.commandHandlers = commandHandlers;
    }

    protected void invokeDirectly(Command command) {
        MyCommandHandler1 commandHandler0 = (MyCommandHandler1) commandHandlers.get(0);
        if (command.getName().equals("CreateAggregate1Command")) {
            commandHandler0.handle((CreateAggregate1Command) command);
            return;
        }
        if (command.getName().equals("UpdateAggregate1Command")) {
            commandHandler0.handle((UpdateAggregate1Command) command);
            return;
        }

        MyCommandHandler2 commandHandler1 = (MyCommandHandler2) commandHandlers.get(1);
        if (command.getName().equals("CreateAggregate2Command")) {
            commandHandler1.handle((CreateAggregate2Command) command);
            return;
        }
        if (command.getName().equals("UpdateAggregate2Command")) {
            commandHandler1.handle((UpdateAggregate2Command) command);
            return;
        }
    }
}
