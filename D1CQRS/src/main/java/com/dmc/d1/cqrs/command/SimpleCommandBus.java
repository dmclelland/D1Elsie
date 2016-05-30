package com.dmc.d1.cqrs.command;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.Utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class SimpleCommandBus<A extends Aggregate, T extends AbstractCommandHandler<A>> implements CommandBus {

    private Map<String, T> commandToHandler = new HashMap<>();


    public SimpleCommandBus(List<T> commandHandlers){
        buildCommandToHandler(commandHandlers);
    }

    private void buildCommandToHandler(List<T> commandHandlers) {

        //if multiple handlers for the same command then this is an error
        for (T commandHandler : commandHandlers) {
            //register all annotated methods
            for (Method m : Utils.methodsOf(commandHandler.getClass())) {
                if (m.isAnnotationPresent(com.dmc.d1.cqrs.annotations.CommandHandler.class)) {
                    if(commandToHandler.containsKey(m.getParameterTypes()[0].getSimpleName()))
                        throw new IllegalStateException(m.getParameterTypes()[0].getSimpleName() + " has more than one handler");

                    if (m.getParameterTypes().length == 1 && Command.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        commandToHandler.put(m.getParameterTypes()[0].getSimpleName(), commandHandler);
                    } else {
                        throw new IllegalStateException("A command handler must have a single argument of type " + Command.class.getName());
                    }
                }
            }
        }
    }

    //TODO locking policy needs to be established
    @Override
    public void dispatch(Command command) {
       T handler =  commandToHandler.get(command.getName());
        if(handler==null)
            throw new IllegalStateException("No command handler registered for command " + command.getName());

        handler.invokeCommand(command);
    }
}