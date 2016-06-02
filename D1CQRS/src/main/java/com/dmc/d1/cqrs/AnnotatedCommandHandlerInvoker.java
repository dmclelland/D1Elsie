package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.command.Command;

/**
 * Created by davidclelland on 18/05/2016.
 */
public interface AnnotatedCommandHandlerInvoker<A extends Aggregate, T extends AbstractCommandHandler<A>> {
    void invoke(Command command, T handler);
}
