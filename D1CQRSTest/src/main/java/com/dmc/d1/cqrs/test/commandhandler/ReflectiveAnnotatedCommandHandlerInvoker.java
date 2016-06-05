package com.dmc.d1.cqrs.test.commandhandler;

import com.dmc.d1.cqrs.AbstractCommandHandler;
import com.dmc.d1.cqrs.AnnotatedCommandHandlerInvoker;
import com.dmc.d1.cqrs.Utils;
import com.dmc.d1.cqrs.command.Command;
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
public class ReflectiveAnnotatedCommandHandlerInvoker implements AnnotatedCommandHandlerInvoker {

    private static final Logger LOG = LoggerFactory.getLogger(ReflectiveAnnotatedCommandHandlerInvoker.class);



    private final Map<String, Method> annotatedCommandMethods = new HashMap<>();

    public ReflectiveAnnotatedCommandHandlerInvoker(Class clazz) {

        buildCommandMethods(clazz);
    }

    private void buildCommandMethods(Class clazz) {

        //register all annotated methods
        for (Method m : Utils.methodsOf(clazz)) {
            if (m.isAnnotationPresent(com.dmc.d1.cqrs.annotations.CommandHandler.class)) {
                if (annotatedCommandMethods.containsKey(m.getParameterTypes()[0].getName()))
                    throw new IllegalStateException(m.getParameterTypes()[0].getName() + " has more than one handler");

                if (m.getParameterTypes().length == 1 && Command.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    annotatedCommandMethods.put(m.getParameterTypes()[0].getName(), m);
                } else {
                    throw new IllegalStateException("A command handler must have a single argument of type " + Command.class.getName());
                }
            }
        }
    }

    @Override
    public void invoke(Command command, AbstractCommandHandler commandHandler) {

        Method commandMethod = annotatedCommandMethods.get(command.getClassName());

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