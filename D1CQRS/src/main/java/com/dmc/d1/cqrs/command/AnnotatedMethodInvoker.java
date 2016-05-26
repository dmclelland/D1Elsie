package com.dmc.d1.cqrs.command;

/**
 * Created by davidclelland on 18/05/2016.
 */
public interface AnnotatedMethodInvoker<T extends AbstractCommandHandler> {
    void invoke(Command command, T handler);
}
