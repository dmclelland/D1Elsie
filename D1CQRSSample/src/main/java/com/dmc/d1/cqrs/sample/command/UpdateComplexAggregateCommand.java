package com.dmc.d1.cqrs.sample.command;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.sample.domain.MyId;
import com.dmc.d1.sample.domain.Basket;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class UpdateComplexAggregateCommand implements Command {

    private final static String CLASS_NAME = UpdateComplexAggregateCommand.class.getName();

    private final MyId id;
    private final String ric;
    private final int adjustedShares;

    public UpdateComplexAggregateCommand(MyId id, String ric, int adjustedShares){
        this.id = id;
        this.ric = ric;
        this.adjustedShares = adjustedShares;
    }

    public MyId getId() {
        return id;
    }

    public String getRic() {
        return ric;
    }


    public int getAdjustedShares() {
        return adjustedShares;
    }

    @Override
    public String getAggregateId() {
        return id.asString();
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
