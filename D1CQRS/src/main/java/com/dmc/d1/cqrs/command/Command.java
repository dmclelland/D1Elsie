package com.dmc.d1.cqrs.command;

import com.dmc.d1.domain.Id;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidclelland on 16/05/2016.
 */
public interface Command {

    enum CommandType {
        NESTED(1), PROCESS_STARTER(2), SYSTEM_STARTER(3);

        private int val;

        CommandType(int val) {
            this.val = val;
        }

        private static Map<Integer, CommandType> map = new HashMap<>();

        static {
            for (CommandType type : CommandType.values()) {
                map.put(type.val, type);
            }
        }

        CommandType fromVal(int val) {
            return map.get(val);
        }
    }

    ;

    Id getAggregateId();

    String getName();

    CommandType getCommandType();
}
