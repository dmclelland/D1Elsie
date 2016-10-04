package com.dmc.d1.cqrs.sample.command;

import com.dmc.d1.cqrs.command.Command;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class UpdateAggregate1Command implements Command {

    private final static String CLASS_NAME = UpdateAggregate1Command.class.getName();

    private long id;
    private int i;
    private int i2;

    public UpdateAggregate1Command(long id, int i, int i2) {
        this.id = id;
        this.i = i;
        this.i2 = i2;
    }


    @Override
    public long getAggregateId() {
        return id;
    }

    public int getI() {
        return i;
    }

    public int getI2() {
        return i2;
    }

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.PROCESS_STARTER;
    }

}
