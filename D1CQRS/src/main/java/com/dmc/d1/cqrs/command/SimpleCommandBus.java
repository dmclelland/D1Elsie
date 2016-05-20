package com.dmc.d1.cqrs.command;

import com.dmc.d1.cqrs.Utils;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class SimpleCommandBus implements CommandBus {

    private AnnotatedMethodInvoker annotatedMethodInvoker;

    public SimpleCommandBus(AnnotatedMethodInvoker annotatedMethodInvoker) {
        this.annotatedMethodInvoker = annotatedMethodInvoker;
    }

    //TODO locking policy needs to be established
    @Override
    public void dispatch(Command command) {
        annotatedMethodInvoker.invoke(command);
    }
}