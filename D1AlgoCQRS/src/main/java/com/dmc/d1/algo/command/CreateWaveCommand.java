package com.dmc.d1.algo.command;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.domain.WaveId;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class CreateWaveCommand implements Command {

    private static String simpleClassName = CreateWaveCommand.class.getSimpleName();

    private final WaveId waveId;

    public CreateWaveCommand(WaveId waveId){
        this.waveId = waveId;
    }

    public WaveId getWaveId() {
        return waveId;
    }

    @Override
    public String getAggregateId() {
        return waveId.toString();
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
