package com.dmc.d1.cqrs.command;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidclelland on 16/05/2016.
 */
public interface Command {

    String getAggregateId();

    String getClassName();

    CommandType getCommandType();

    default boolean isAggregateInitiator(){
        return false;
    }

    enum CommandType {
        NESTED(1), PROCESS_STARTER(2), SYSTEM_STARTER(3);

        private static final Map<Integer, CommandType> map = new HashMap<>();

        static {
            for (CommandType type : CommandType.values()) {
                map.put(type.val, type);
            }
        }

        private final int val;

        CommandType(int val) {
            this.val = val;
        }

        CommandType fromVal(int val) {
            return map.get(val);
        }
    }



}
