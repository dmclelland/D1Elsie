package com.dmc.d1.cqrs.sample.command;

import com.dmc.d1.cqrs.command.Command;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class UpdateAggregate1Command2 implements Command {

    private final static String CLASS_NAME = UpdateAggregate1Command2.class.getName();

    private long id;
    private String str;


    public UpdateAggregate1Command2(long id, String str) {
        this.id = id;
        this.str = str;
    }

    @Override
    public long getAggregateId() {
        return id;
    }

    public String getStr() {
        return str;
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
