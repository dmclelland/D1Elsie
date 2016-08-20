package com.dmc.d1.algo.command;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.domain.PairId;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class CreatePairCommand implements Command {

    private static String CLASS_NAME = CreatePairCommand.class.getName();

    private final PairId pairId;

    public CreatePairCommand(PairId pairId){
        this.pairId = pairId;
    }

    public PairId getPairId() {
        return pairId;
    }

    @Override
    public String getAggregateId() {
        return pairId.asString();
    }

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.SYSTEM_STARTER;
    }

    @Override
    public boolean isAggregateInitiator() {
        return true;
    }
}
