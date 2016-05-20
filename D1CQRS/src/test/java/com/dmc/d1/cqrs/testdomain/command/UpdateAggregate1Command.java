package com.dmc.d1.cqrs.testdomain.command;

import com.dmc.d1.cqrs.testdomain.MyId;
import com.dmc.d1.cqrs.command.Command;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class UpdateAggregate1Command implements Command {

    private final static String simpleClassName = UpdateAggregate1Command.class.getSimpleName();

    private MyId id;
    private int i;
    private int i2;

    public UpdateAggregate1Command(MyId id, int i, int i2){
        this.id = id;
        this.i = i;
        this.i2 = i2;
    }

    @Override
    public MyId getAggregateId() {
        return id;
    }

    public int getI() {
        return i;
    }

    public int getI2() {
        return i2;
    }

    @Override
    public String getName() {
        return simpleClassName;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.PROCESS_STARTER;
    }

}
