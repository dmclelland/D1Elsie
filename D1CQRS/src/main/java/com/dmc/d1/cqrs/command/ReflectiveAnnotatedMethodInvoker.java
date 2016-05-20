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
public class ReflectiveAnnotatedMethodInvoker implements AnnotatedMethodInvoker{

    private Map<String, AnnotatedCommandMethod> annotatedCommandMethods = new HashMap<>();

    private static final Logger LOG = LoggerFactory.getLogger(ReflectiveAnnotatedMethodInvoker.class);

    public ReflectiveAnnotatedMethodInvoker(List<? extends AbstractCommandHandler> commandHandlers){
        buildCommandMethods(commandHandlers);
    }

    private void buildCommandMethods(List<? extends AbstractCommandHandler> commandHandlers) {

        for (AbstractCommandHandler commandHandler : commandHandlers) {
            //register all annotated methods
            for (Method m : Utils.methodsOf(commandHandler.getClass())) {
                if (m.isAnnotationPresent(com.dmc.d1.cqrs.annotations.CommandHandler.class)) {
                    if (m.getParameterTypes().length == 1 && Command.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        annotatedCommandMethods.put(m.getParameterTypes()[0].getSimpleName(), new AnnotatedCommandMethod(commandHandler, m));
                    } else {
                        throw new IllegalStateException("A command handler must have a single argument of type " + Command.class.getName());
                    }
                }
            }
        }
    }

    @Override
    public void invoke(Command command) {

        AnnotatedCommandMethod commandMethod = annotatedCommandMethods.get(command.getName());

        if (commandMethod == null) {
            LOG.error("No corresponding method command handler exists for " + command.toString());
            return;
        }

        AbstractCommandHandler handler =  commandMethod.getCommandHandler();

        try {
            commandMethod.invoke(command);
            handler.commitAggregate(command.getAggregateId());
        } catch (Exception e) {
            //rollback aggregate if any error
            handler.rollbackAggregate(command.getAggregateId());
            LOG.error("Unable to process command {} ", command.toString(), e);
        }
    }

    private static class AnnotatedCommandMethod {

        private final AbstractCommandHandler commandHandler;
        private final Method method;

        AnnotatedCommandMethod(AbstractCommandHandler commandHandler, Method method){
            this.commandHandler = checkNotNull(commandHandler);
            this.method = checkNotNull(method);
        }

        void invoke(Command command){
            try {
                method.invoke(commandHandler, command);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getTargetException());
            }
        }

        public AbstractCommandHandler getCommandHandler() {
            return commandHandler;
        }
    }

}
