package com.dmc.d1.cqrs.sample.command;

import com.dmc.d1.cqrs.command.Command;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class UpdateComplexAggregateWithDeterministicExceptionCommand implements Command {

    private final static String CLASS_NAME = UpdateComplexAggregateWithDeterministicExceptionCommand.class.getName();

    private final long id;
    private final String ric;
    private final int adjustedShares;

    public UpdateComplexAggregateWithDeterministicExceptionCommand(long id, String ric, int adjustedShares) {
        this.id = id;
        this.ric = ric;
        this.adjustedShares = adjustedShares;
    }

    public String getRic() {
        return ric;
    }


    public int getAdjustedShares() {
        return adjustedShares;
    }

    @Override
    public long getAggregateId() {
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


}
