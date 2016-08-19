package com.dmc.d1.cqrs.codegen;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.AnnotatedAggregateEventHandlerInvoker;
import com.dmc.d1.cqrs.Utils;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.event.AbstractEventHandler;
import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.event.AnnotatedEventHandlerInvoker;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import org.reflections.Reflections;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Created by davidclelland on 20/05/2016.
 */
public class EventHandlerGenerator {

    private final String rootPackageToScan;
    private final String generatedSourceDirectory;
    private final String generatedPackageName;

    public static void main(String[] args) {
        checkState(args.length == 3);

        String rootPackageToScan = args[0];
        String generatedSourceDirectory = args[1];
        String generatedPackageName = args[2];

        try {

            EventHandlerGenerator generator = new EventHandlerGenerator(
                    rootPackageToScan, generatedSourceDirectory, generatedPackageName);

            generator.generate();

        } catch (Exception e) {
            throw new RuntimeException("Unable to generate code", e);
        }
    }


    EventHandlerGenerator(String rootPackageToScan,
                          String generatedSourceDirectory,
                          String generatedPackageName) {

        this.rootPackageToScan = checkNotNull(rootPackageToScan);
        this.generatedSourceDirectory = checkNotNull(generatedSourceDirectory);
        this.generatedPackageName = checkNotNull(generatedPackageName);
    }

    public void generate() throws Exception {

        Reflections reflections = new Reflections(this.rootPackageToScan);
        generateAggregateEventHandlerMethodInvokers(reflections);
        generateExternalEventHandlerMethodInvokers(reflections);

    }


    private void generateAggregateEventHandlerMethodInvokers(Reflections reflections) throws Exception {

        Set<Class<? extends Aggregate>> aggregates = reflections.getSubTypesOf(Aggregate.class);

        Set<String> events = new HashSet<>();
        //aggregate events should only have one AGGREGATE handler
        //they obviously can (and will) have multiple external handlers

        for (Class<? extends Aggregate> aggregateClass : aggregates) {

            MethodSpec.Builder invokeBuilder = MethodSpec.methodBuilder("invoke")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addParameter(AggregateEvent.class, "event")
                    .addParameter(aggregateClass, "aggregate");

            //register all annotated methods
            for (Method m : Utils.methodsOf(aggregateClass)) {

                if (m.isAnnotationPresent(EventHandler.class)) {

                    if (m.getParameterTypes().length == 1 && AggregateEvent.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        Class event = m.getParameterTypes()[0];

                        if (events.contains(event.getName()))
                            throw new IllegalStateException(event.getName() + " has more than one handler");
                        else
                            events.add(event.getName());

                        invokeBuilder.beginControlFlow("if (event.getClassName().equals($S))", event.getName());
                        invokeBuilder.addStatement("aggregate.$L(($T)event)", m.getName(), event);
                        invokeBuilder.addStatement("return");
                        invokeBuilder.endControlFlow();
                    } else {
                        throw new IllegalStateException("An event handler must have a single argument of type " + AggregateEvent.class.getName());
                    }
                }
            }

            MethodSpec invoke = invokeBuilder.build();
            String className = aggregateClass.getSimpleName() + "AnnotatedMethodInvoker";

            TypeSpec directAnnotatedMethodInvoker = TypeSpec.classBuilder(className)
                    .addSuperinterface(ParameterizedTypeName.get(AnnotatedAggregateEventHandlerInvoker.class, aggregateClass))
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(invoke)
                    .build();

            JavaFile javaFile = JavaFile.builder(this.generatedPackageName, directAnnotatedMethodInvoker)
                    .build();

            javaFile.writeTo(new File(this.generatedSourceDirectory));

        }
    }


    private void generateExternalEventHandlerMethodInvokers(Reflections reflections) throws Exception {

        Set<Class<? extends AbstractEventHandler>> eventHandlers = reflections.getSubTypesOf(AbstractEventHandler.class);

        for (Class<? extends AbstractEventHandler> eventHandlerClass : eventHandlers) {

            MethodSpec.Builder invokeBuilder = MethodSpec.methodBuilder("invoke")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addParameter(AggregateEvent.class, "event")
                    .addParameter(eventHandlerClass, "eventHandler");

            //register all annotated methods
            for (Method m : Utils.methodsOf(eventHandlerClass)) {

                if (m.isAnnotationPresent(EventHandler.class)) {

                    if (m.getParameterTypes().length == 1 && AggregateEvent.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        Class event = m.getParameterTypes()[0];


                        invokeBuilder.beginControlFlow("if (event.getClassName().equals($S))", event.getName());
                        invokeBuilder.addStatement("eventHandler.$L(($T)event)", m.getName(), event);
                        invokeBuilder.addStatement("return");
                        invokeBuilder.endControlFlow();
                    } else {
                        throw new IllegalStateException("An event handler must have a single argument of type " + AggregateEvent.class.getName());
                    }
                }
            }

            MethodSpec invoke = invokeBuilder.build();
            String className = eventHandlerClass.getSimpleName() + "AnnotatedMethodInvoker";

            TypeSpec directAnnotatedMethodInvoker = TypeSpec.classBuilder(className)
                    .addSuperinterface(ParameterizedTypeName.get(AnnotatedEventHandlerInvoker.class, eventHandlerClass))
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(invoke)
                    .build();

            JavaFile javaFile = JavaFile.builder(this.generatedPackageName, directAnnotatedMethodInvoker)
                    .build();

            javaFile.writeTo(new File(this.generatedSourceDirectory));

        }
    }
}