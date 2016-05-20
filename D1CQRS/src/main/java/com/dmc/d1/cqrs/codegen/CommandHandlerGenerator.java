package com.dmc.d1.cqrs.codegen;

import com.dmc.d1.cqrs.Utils;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import com.dmc.d1.cqrs.command.AbstractCommandHandler;
import com.dmc.d1.cqrs.command.AbstractDirectMethodInvoker;
import com.dmc.d1.cqrs.command.Command;
import com.squareup.javapoet.*;
import org.reflections.Reflections;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.lang.reflect.Method;
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
                            String generatedPackageName){


        this.rootPackageToScan = checkNotNull(rootPackageToScan);
        this.generatedSourceDirectory = checkNotNull(generatedSourceDirectory);
        this.generatedPackageName = checkNotNull(generatedPackageName);
    }



    public void generate() throws Exception{

        WildcardTypeName type = WildcardTypeName.subtypeOf(AbstractCommandHandler.class);

        ClassName list = ClassName.get("java.util", "List");
        TypeName listOfCommandHandlers = ParameterizedTypeName.get(list, type);

        FieldSpec commandHandlersField = FieldSpec.builder(listOfCommandHandlers,"commandHandlers", Modifier.PRIVATE, Modifier.FINAL).build();

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(listOfCommandHandlers, "commandHandlers")
                .addStatement("super($N)", "commandHandlers")
                .addStatement("this.$N = $N", "commandHandlers", "commandHandlers");

        MethodSpec.Builder invokeBuilder = MethodSpec.methodBuilder("invokeDirectly")
                .addModifiers(Modifier.PROTECTED)
                .returns(void.class)
                .addParameter(Command.class, "command");

        Reflections reflections = new Reflections(this.rootPackageToScan);

        Set<Class<? extends AbstractCommandHandler>> commandHandlers = reflections.getSubTypesOf(AbstractCommandHandler.class);

        Set<String> commands = new HashSet<>();

        int counter = 0;
        for (Class<? extends AbstractCommandHandler> commandHandlerClass : commandHandlers) {

            invokeBuilder.addStatement("$T commandHandler$L =  ($T)commandHandlers.get($L)", commandHandlerClass,counter,commandHandlerClass, counter) ;
            //register all annotated methods
            for (Method m : Utils.methodsOf(commandHandlerClass)) {

                if (m.isAnnotationPresent(CommandHandler.class)) {

                    if (m.getParameterTypes().length == 1 && Command.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        Class command = m.getParameterTypes()[0];

                        if(commands.contains(command.getSimpleName()))
                            throw new IllegalStateException(command.getSimpleName() + " has more than one handler");
                        else
                            commands.add(command.getSimpleName());

                        invokeBuilder.beginControlFlow("if (command.getName().equals($S))",command.getSimpleName());
                        invokeBuilder.addStatement("commandHandler$L.$L(($T)command)",counter, m.getName(),command);
                        invokeBuilder.addStatement("return");
                        invokeBuilder.endControlFlow();
                    } else {
                        throw new IllegalStateException("A command handler must have a single argument of type " + Command.class.getName());
                    }
                }
            }
            counter++;
        }

        MethodSpec invoke = invokeBuilder.build();
        MethodSpec constructor = constructorBuilder.build();

        TypeSpec directAnnotatedMethodInvoker = TypeSpec.classBuilder("DirectAnnotatedMethodInvoker")
                .addField(commandHandlersField)
                .superclass(AbstractDirectMethodInvoker.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(constructor)
                .addMethod(invoke)
                .build();

        JavaFile javaFile = JavaFile.builder(this.generatedPackageName, directAnnotatedMethodInvoker)
                .build();

        javaFile.writeTo(new File(this.generatedSourceDirectory));

    }

}
