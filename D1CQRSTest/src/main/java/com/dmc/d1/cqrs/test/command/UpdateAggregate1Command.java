package com.dmc.d1.cqrs.test.command;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.test.domain.MyId;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class UpdateAggregate1Command implements Command {

    private final static String CLASS_NAME = UpdateAggregate1Command.class.getName();

    private MyId id;
    private int i;
    private int i2;

    public UpdateAggregate1Command(MyId id, int i, int i2){
        this.id = id;
        this.i = i;
        this.i2 = i2;
    }

    public MyId getId() {
        return id;
    }

    @Override
    public String getAggregateId() {
        return id.asString();
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
