package com.dmc.d1.cqrs.command;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 18/05/2016.
 */
class ReflectiveAnnotatedCommandHandlerInvoker<A extends Aggregate, T extends AbstractCommandHandler<A>> implements AnnotatedCommandHandlerInvoker<A,T> {

    private static final Logger LOG = LoggerFactory.getLogger(ReflectiveAnnotatedCommandHandlerInvoker.class);

    private final AbstractCommandHandler<A> commandHandler;

    private final Map<String, Method> annotatedCommandMethods = new HashMap<>();

    public ReflectiveAnnotatedCommandHandlerInvoker(AbstractCommandHandler<A> commandHandler) {
        this.commandHandler = checkNotNull(commandHandler);
        buildCommandMethods(commandHandler);
    }

    private void buildCommandMethods(AbstractCommandHandler<A> commandHandler) {

        //register all annotated methods
        for (Method m : Utils.methodsOf(commandHandler.getClass())) {
            if (m.isAnnotationPresent(com.dmc.d1.cqrs.annotations.CommandHandler.class)) {
                if (annotatedCommandMethods.containsKey(m.getParameterTypes()[0].getSimpleName()))
                    throw new IllegalStateException(m.getParameterTypes()[0].getSimpleName() + " has more than one handler");

                if (m.getParameterTypes().length == 1 && Command.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    annotatedCommandMethods.put(m.getParameterTypes()[0].getSimpleName(), m);
                } else {
                    throw new IllegalStateException("A command handler must have a single argument of type " + Command.class.getName());
                }
            }
        }
    }

    @Override
    public void invoke(Command command, T handler) {

        Method commandMethod = annotatedCommandMethods.get(command.getName());

        if (commandMethod == null) {
            LOG.error("No corresponding method command handler exists for " + command.toString());
            return;
        }

        try {
            commandMethod.invoke(commandHandler, command);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }
}