package com.dmc.d1.cqrs.command;

/**
 * Created by davidclelland on 16/05/2016.
 */
public interface CommandBus {

    void dispatch(Command command);
}
