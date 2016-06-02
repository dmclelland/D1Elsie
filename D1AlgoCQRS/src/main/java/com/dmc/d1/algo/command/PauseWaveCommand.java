package com.dmc.d1.algo.command;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.domain.WaveId;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class PauseWaveCommand implements Command {

    private static String CLASS_NAME = PauseWaveCommand.class.getName();

    private final WaveId waveId;

    public PauseWaveCommand(WaveId waveId) {
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
    public String getClassName() {
        return CLASS_NAME;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.SYSTEM_STARTER;
    }
}