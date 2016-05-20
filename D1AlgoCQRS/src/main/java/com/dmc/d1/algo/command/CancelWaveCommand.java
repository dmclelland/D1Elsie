package com.dmc.d1.algo.command;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.domain.WaveId;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class CancelWaveCommand implements Command {

    private static String simpleClassName = CancelWaveCommand.class.getSimpleName();

    private final WaveId waveId;

    public CancelWaveCommand(WaveId waveId) {
        this.waveId = waveId;
    }

    @Override
    public WaveId getAggregateId() {
        return waveId;
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
