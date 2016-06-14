package com.dmc.d1.cqrs.test.command;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.test.domain.MyNestedId;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class ExceptionTriggeringNestedAggregateCommand implements Command {

    private final static String CLASS_NAME = ExceptionTriggeringNestedAggregateCommand.class.getName();

    private MyNestedId id;
    private String str;


    public ExceptionTriggeringNestedAggregateCommand(MyNestedId id, String str) {
        this.id = id;
        this.str = str;
    }

    public MyNestedId getId() {
        return id;
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
    public String getAggregateId() {
        return id.asString();
    }

    public String getStr() {
        return str;
    }

}
