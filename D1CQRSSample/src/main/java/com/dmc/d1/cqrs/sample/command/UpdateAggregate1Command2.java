package com.dmc.d1.cqrs.sample.command;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.sample.domain.MyId;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class UpdateAggregate1Command2 implements Command {

    private final static String CLASS_NAME = UpdateAggregate1Command2.class.getName();

    private MyId id;
    private String str;


    public UpdateAggregate1Command2(MyId id, String str) {
        this.id = id;
        this.str = str;
    }

    public MyId getId() {
        return id;
    }

    @Override
    public String getAggregateId() {
        return id.asString();
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
