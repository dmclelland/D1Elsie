package com.dmc.d1.cqrs.command;

import com.dmc.d1.cqrs.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 18/05/2016.
 */
public abstract class AbstractDirectMethodInvoker implements AnnotatedMethodInvoker{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDirectMethodInvoker.class);

    private Map<String, AbstractCommandHandler> commandToHandler = new HashMap<>();

    public AbstractDirectMethodInvoker(List<? extends AbstractCommandHandler> commandHandlers){
        buildCommandToHandler(commandHandlers);
    }

    private void buildCommandToHandler(List<? extends AbstractCommandHandler> commandHandlers) {

        for (AbstractCommandHandler commandHandler : commandHandlers) {
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

    @Override
    public void invoke(Command command) {

        AbstractCommandHandler handler =  commandToHandler.get(command.getName());

        if (handler == null) {
            LOG.error("No corresponding command handler exists for " + command.toString());
            return;
        }

        try {
            invokeDirectly(command);
            handler.commitAggregate(command.getAggregateId());
        } catch (Exception e) {
            //rollback aggregate if any error
            handler.rollbackAggregate(command.getAggregateId());
            LOG.error("Unable to process command {} ", command.toString(), e);
        }
    }

    protected abstract void invokeDirectly(Command command);

}