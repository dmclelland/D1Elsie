package com.dmc.d1.cqrs.sample.command;

import com.dmc.d1.cqrs.command.Command;


/**
 * Created by davidclelland on 17/05/2016.
 */
public class ExceptionTriggeringNestedAggregateCommand implements Command {

    private final static String CLASS_NAME = ExceptionTriggeringNestedAggregateCommand.class.getName();

    private long id;
    private String str;


    public ExceptionTriggeringNestedAggregateCommand(long id, String str) {
        this.id = id;
        this.str = str;
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

    public String getStr() {
        return str;
    }

}
