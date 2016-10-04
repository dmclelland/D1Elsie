package com.dmc.d1.algo.command;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.domain.WaveId;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class ResumeWaveCommand implements Command {

    private static String CLASS_NAME = ResumeWaveCommand.class.getName();

    private final long waveId;

    public ResumeWaveCommand(long waveId) {
        this.waveId = waveId;
    }

    @Override
    public long getAggregateId() {
        return waveId;
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
