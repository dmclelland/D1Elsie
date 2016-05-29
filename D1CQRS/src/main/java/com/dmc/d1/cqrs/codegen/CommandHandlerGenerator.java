package com.dmc.d1.cqrs.codegen;

import com.dmc.d1.cqrs.Utils;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import com.dmc.d1.cqrs.command.AbstractCommandHandler;
import com.dmc.d1.cqrs.command.AnnotatedCommandHandlerInvoker;
import com.dmc.d1.cqrs.command.Command;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import org.reflections.Reflections;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 20/05/2016.
 */
public class CommandHandlerGenerator {

    private final String rootPackageToScan;
    private final String generatedSourceDirectory;
    private final String generatedPackageName;

    CommandHandlerGenerator(String rootPackageToScan,
                            String generatedSourceDirectory,
                            String generatedPackageName) {

        this.rootPackageToScan = checkNotNull(rootPackageToScan);
        this.generatedSourceDirectory = checkNotNull(generatedSourceDirectory);
        this.generatedPackageName = checkNotNull(generatedPackageName);
    }

    public void generate() throws Exception {


        Reflections reflections = new Reflections(this.rootPackageToScan);

        Set<Class<? extends AbstractCommandHandler>> commandHandlers = reflections.getSubTypesOf(AbstractCommandHandler.class);

        Set<String> commands = new HashSet<>();

        //for each commandHandler generate a separate direct method invoker

        for (Class<? extends AbstractCommandHandler> commandHandlerClass : commandHandlers) {

            MethodSpec.Builder invokeBuilder = MethodSpec.methodBuilder("invoke")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addParameter(Command.class, "command")
                    .addParameter(commandHandlerClass, "commandHandler");

            //register all annotated methods
            for (Method m : Utils.methodsOf(commandHandlerClass)) {

                if (m.isAnnotationPresent(CommandHandler.class)) {

                    if (m.getParameterTypes().length == 1 && Command.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        Class command = m.getParameterTypes()[0];

                        if (commands.contains(command.getSimpleName()))
                            throw new IllegalStateException(command.getSimpleName() + " has more than one handler");
                        else
                            commands.add(command.getSimpleName());

                        invokeBuilder.beginControlFlow("if (command.getName().equals($S))", command.getSimpleName());
                        invokeBuilder.addStatement("commandHandler.$L(($T)command)", m.getName(), command);
                        invokeBuilder.addStatement("return");
                        invokeBuilder.endControlFlow();
                    } else {
                        throw new IllegalStateException("A command handler must have a single argument of type " + Command.class.getName());
                    }
                }
            }

            MethodSpec invoke = invokeBuilder.build();
            String className = commandHandlerClass.getSimpleName() + "AnnotatedMethodInvoker";

            ParameterizedType aggregateType = (ParameterizedType) commandHandlerClass.getGenericSuperclass(); // OtherClass<String>
            Class<?> aggregateClass = (Class<?>) aggregateType.getActualTypeArguments()[0];

            TypeSpec directAnnotatedMethodInvoker = TypeSpec.classBuilder(className)
                    .addSuperinterface(ParameterizedTypeName.get(AnnotatedCommandHandlerInvoker.class, aggregateClass, commandHandlerClass))
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(invoke)
                    .build();

            JavaFile javaFile = JavaFile.builder(this.generatedPackageName, directAnnotatedMethodInvoker)
                    .build();

            javaFile.writeTo(new File(this.generatedSourceDirectory));

        }


    }
}
