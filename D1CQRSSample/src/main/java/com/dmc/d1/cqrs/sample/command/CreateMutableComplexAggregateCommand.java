package com.dmc.d1.cqrs.sample.command;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.sample.domain.MyId;
import com.dmc.d1.sample.domain.Basket;
import com.dmc.d1.sample.domain.Basket2;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class CreateMutableComplexAggregateCommand implements Command {

    private final static String CLASS_NAME = CreateMutableComplexAggregateCommand.class.getName();

    private Basket2 basket;
    private MyId id;

    public CreateMutableComplexAggregateCommand(MyId id, Basket2 basket){
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

    public Basket2 getBasket() {
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
