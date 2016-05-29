package com.dmc.d1.cqrs.command;

import com.dmc.d1.cqrs.Aggregate;

/**
 * Created by davidclelland on 18/05/2016.
 */
public interface AnnotatedCommandHandlerInvoker<A extends Aggregate, T extends AbstractCommandHandler<A>> {
    void invoke(Command command, T handler);
}
