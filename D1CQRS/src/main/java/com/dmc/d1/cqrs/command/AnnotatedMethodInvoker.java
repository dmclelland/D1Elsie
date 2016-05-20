package com.dmc.d1.cqrs.command;

/**
 * Created by davidclelland on 18/05/2016.
 */
public interface AnnotatedMethodInvoker {
    void invoke(Command command);
}
