package com.dmc.d1.cqrs.testdomain.command;

import com.dmc.d1.cqrs.testdomain.MyId;
import com.dmc.d1.cqrs.command.Command;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class CreateAggregate2Command implements Command {

    private final static String simpleClassName = CreateAggregate2Command.class.getSimpleName();

    private MyId id;
    private String str1;
    private String str2;

    public CreateAggregate2Command(MyId id, String str1, String str2){
        this.id = id;
        this.str1 = str1;
        this.str2 = str2;
    }

    public MyId getId() {
        return id;
    }

    @Override
    public String getName() {
        return simpleClassName;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.PROCESS_STARTER;
    }

    @Override
    public String getAggregateId() {
        return id.toString();
    }

    public String getStr1() {
        return str1;
    }

    public String getStr2() {
        return str2;
    }
}
