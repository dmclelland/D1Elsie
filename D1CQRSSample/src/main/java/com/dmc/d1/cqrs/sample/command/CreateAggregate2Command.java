package com.dmc.d1.cqrs.sample.command;

import com.dmc.d1.cqrs.command.Command;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class CreateAggregate2Command implements Command {

    private final static String CLASS_NAME = CreateAggregate2Command.class.getName();

    private long id;
    private String str1;
    private String str2;

    public CreateAggregate2Command(long id, String str1, String str2) {
        this.id = id;
        this.str1 = str1;
        this.str2 = str2;
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
    public long getAggregateId() {
        return id;
    }

    public String getStr1() {
        return str1;
    }

    public String getStr2() {
        return str2;
    }

    @Override
    public boolean isAggregateInitiator() {
        return true;
    }
}
