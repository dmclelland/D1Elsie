package com.dmc.d1.cqrs.sample.command;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.domain.Ric;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class UpdateComplexAggregateCommand implements Command {

    private final static String CLASS_NAME = UpdateComplexAggregateCommand.class.getName();

    private final long id;
    private final Ric ric;
    private final int adjustedShares;

    public UpdateComplexAggregateCommand(long id, Ric ric, int adjustedShares) {
        this.id = id;
        this.ric = ric;
        this.adjustedShares = adjustedShares;
    }


    public Ric getRic() {
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
