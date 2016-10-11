package com.dmc.d1.cqrs.sample.command;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.domain.StockRic;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class UpdateComplexAggregateWithDeterministicExceptionCommand implements Command {

    private final static String CLASS_NAME = UpdateComplexAggregateWithDeterministicExceptionCommand.class.getName();

    private final long id;
    private final StockRic ric;
    private final int adjustedShares;
    private final int rollbackTrigger;

    public UpdateComplexAggregateWithDeterministicExceptionCommand(long id, StockRic ric, int adjustedShares, int rollbackTrigger) {
        this.id = id;
        this.ric = ric;
        this.adjustedShares = adjustedShares;
        this.rollbackTrigger = rollbackTrigger;
    }

    public StockRic getRic() {
        return ric;
    }


    public int getAdjustedShares() {
        return adjustedShares;
    }

    @Override
    public long getAggregateId() {
        return id;
    }

    public int getRollbackTrigger() {
        return rollbackTrigger;
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
