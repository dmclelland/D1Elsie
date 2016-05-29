package com.dmc.d1.algo.command;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.domain.PairId;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class CreatePairCommand implements Command {

    private static String simpleClassName = CreatePairCommand.class.getSimpleName();

    private final PairId pairId;

    public CreatePairCommand(PairId pairId){
        this.pairId = pairId;
    }

    public PairId getPairId() {
        return pairId;
    }

    @Override
    public String getAggregateId() {
        return pairId.toString();
    }

    @Override
    public String getName() {
        return simpleClassName;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.SYSTEM_STARTER;
    }
}
