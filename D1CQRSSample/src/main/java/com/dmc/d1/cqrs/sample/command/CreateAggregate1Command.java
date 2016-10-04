package com.dmc.d1.cqrs.sample.command;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.sample.domain.MyId;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class CreateAggregate1Command implements Command {

    private final static String CLASS_NAME = CreateAggregate1Command.class.getName();

    private long id;
    private int i1;
    private int i2;

    public CreateAggregate1Command(long id, int i, int i2){
        this.id = id;
        this.i1 = i;
        this.i2 = i2;
    }


    @Override
    public long getAggregateId() {
        return id;
    }

    public int getI1() {
        return i1;
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

    @Override
    public boolean isAggregateInitiator() {
        return true;
    }

}
