package com.dmc.d1.cqrs.sample.command;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.sample.domain.MyId;
import com.dmc.d1.sample.domain.Basket;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class CreateComplexAggregateCommand implements Command {

    private final static String CLASS_NAME = CreateComplexAggregateCommand.class.getName();

    private Basket basket;
    private MyId id;

    public CreateComplexAggregateCommand(MyId id, Basket basket){
        this.id = id;
        this.basket = basket;
    }

    public MyId getId() {
        return id;
    }

    @Override
    public String getAggregateId() {
        return id.asString();
    }

    public Basket getBasket() {
        return basket;
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
