package com.dmc.d1.cqrs.sample.command;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.sample.domain.MyId;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class NestedExceptionTriggeringAggregate1Command implements Command {

    private final static String CLASS_NAME = NestedExceptionTriggeringAggregate1Command.class.getName();

   private long id;
    private String str;

    public NestedExceptionTriggeringAggregate1Command(long id, String str){
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
