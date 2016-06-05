package com.dmc.d1.cqrs.codegen;

import com.dmc.d1.cqrs.Utils;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.event.AbstractEventHandler;
import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.event.AnnotatedEventHandlerInvoker;
import com.squareup.javapoet.*;
import org.reflections.Reflections;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 20/05/2016.
 */
class EventGenerator {


    private final String generatedSourceDirectory;
    private final String generatedPackageName;

    EventGenerator(
                   String generatedSourceDirectory,
                   String generatedPackageName) {

        this.generatedSourceDirectory = checkNotNull(generatedSourceDirectory);
        this.generatedPackageName = checkNotNull(generatedPackageName);
    }

    public void generate() throws Exception {

        String fileName = getClass().getClassLoader().getResource("EventDefinitions.txt").getFile();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                EventVo vo = parseEventString(sCurrentLine);
                generateBasicEvent(vo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class EventVo {

        String className;

        Map<String, Class> instanceVariables = new LinkedHashMap<>();
    }


    private EventVo parseEventString(String str) throws Exception {

        String[] nvps = str.trim().split(",");
        Class clazz;

        EventVo vo = new EventVo();

        for (String nvp : nvps) {
            String[] nameAndValue = nvp.trim().split("=");
            String name = nameAndValue[0].trim();
            String val = nameAndValue[1].trim();

            if ("name".equals(name)) {
                vo.className = val;
            }else{
                clazz = Class.forName(val);
                vo.instanceVariables.put(name, clazz);
            }
        }

        return vo;
    }

    private void generateBasicEvent(EventVo vo) throws Exception {

        FieldSpec CLASS_NAME = FieldSpec.builder(String.class,"CLASS_NAME",Modifier.FINAL, Modifier.STATIC)
                .initializer(CodeBlock.of("$N.class.getName()", vo.className)).build();

        TypeSpec.Builder eventBuilder = TypeSpec.classBuilder(vo.className)
                .addSuperinterface(AggregateEvent.class)
                .addField(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC);

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (String key : vo.instanceVariables.keySet()) {

            TypeName typeName = TypeName.get(vo.instanceVariables.get(key));
            if(typeName.isBoxedPrimitive())
                typeName = typeName.unbox();

            eventBuilder.addField(typeName,key,Modifier.PRIVATE, Modifier.FINAL);

            constructorBuilder.addParameter(typeName, key)
                    .addStatement("this.$N = $N", key, key);

            eventBuilder.addMethod(
                    MethodSpec.methodBuilder("get" + capitalize(key))
                            .addModifiers(Modifier.PUBLIC)
                            .addStatement("return $N",key)
                            .returns(vo.instanceVariables.get(key))
                            .build()
            );
        }

        eventBuilder.addMethod(
                MethodSpec.methodBuilder("getAggregateId")
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("return id.toString()")
                        .addAnnotation(Override.class)
                        .returns(String.class)
                        .build()
        );

        eventBuilder.addMethod(
                MethodSpec.methodBuilder("getClassName")
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("return CLASS_NAME")
                        .addAnnotation(Override.class)
                        .returns(String.class)
                        .build()
        );

        eventBuilder.addMethod(constructorBuilder.build());

        JavaFile javaFile = JavaFile.builder(this.generatedPackageName, eventBuilder.build())
                .build();

        javaFile.writeTo(new File(this.generatedSourceDirectory));

    }


    private String capitalize(String str) {

        StringBuilder sb = new StringBuilder(str.length());

        sb.append(Character.toUpperCase(str.charAt(0)));

        sb.append(str.substring(1));
        return sb.toString();

    }

}