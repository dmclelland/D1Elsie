package com.dmc.d1.cqrs.codegen;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

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

            //events created using abstract family pattern
            //EventFactoryAbstract
            ClassName afName = ClassName.get(generatedPackageName,"EventFactoryAbstract");

            TypeSpec.Builder abstractFactory = TypeSpec.interfaceBuilder(afName)
                    .addModifiers(Modifier.PUBLIC);

            TypeSpec.Builder basicFactory = TypeSpec.classBuilder("EventFactoryBasic")
                    .addSuperinterface(afName)
                    .addModifiers(Modifier.PUBLIC);

            TypeSpec.Builder pooledFactory = TypeSpec.classBuilder("EventFactoryPooled")
                    .addSuperinterface(afName)
                    .addModifiers(Modifier.PUBLIC);


            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                EventVo vo = parseEventString(sCurrentLine);


//                @Override
//                public HandledByExternalHandlersEvent createHandledByExternalHandlersEvent(MyId myId, MyNestedId nestedId, String str) {
//                    return new HandledByExternalHandlersEventBasic(myId, nestedId, str);
//                }
//


                ClassName interfaceName = generateInterface(vo, abstractFactory);

                generateBasicEvent(vo, interfaceName, basicFactory);
                generatePooledEvent(vo, interfaceName, pooledFactory);
            }


            JavaFile javaFileAF = JavaFile.builder(this.generatedPackageName, abstractFactory.build())
                    .build();

            javaFileAF.writeTo(new File(this.generatedSourceDirectory));

            JavaFile javaFileBF = JavaFile.builder(this.generatedPackageName, basicFactory.build())
                    .build();

            javaFileBF.writeTo(new File(this.generatedSourceDirectory));

            JavaFile javaFilePF = JavaFile.builder(this.generatedPackageName, pooledFactory.build())
                    .build();

            javaFilePF.writeTo(new File(this.generatedSourceDirectory));
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
            } else {
                clazz = Class.forName(val);
                vo.instanceVariables.put(name, clazz);
            }
        }

        return vo;
    }

    private ClassName generateInterface(EventVo vo, TypeSpec.Builder abstractFactory) throws Exception {


        String interfaceName = vo.className;

        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(interfaceName)
                .addSuperinterface(AggregateEvent.class)
                .addModifiers(Modifier.PUBLIC);

        ClassName eventClass = ClassName.get(this.generatedPackageName, interfaceName);


        MethodSpec.Builder abstractFactoryCreateMethod = MethodSpec.methodBuilder("create" + interfaceName)
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .returns(eventClass);


        for (String key : vo.instanceVariables.keySet()) {

            TypeName typeName = TypeName.get(vo.instanceVariables.get(key));
            if (typeName.isBoxedPrimitive())
                typeName = typeName.unbox();

            interfaceBuilder.addMethod(
                    MethodSpec.methodBuilder("get" + capitalize(key))
                            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                            .returns(typeName)
                            .build()
            );

            abstractFactoryCreateMethod.addParameter(typeName, key);
        }

        interfaceBuilder.addMethod(
                MethodSpec.methodBuilder("getAggregateId")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addAnnotation(Override.class)
                        .returns(String.class)
                        .build()
        );

        interfaceBuilder.addMethod(
                MethodSpec.methodBuilder("getClassName")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addAnnotation(Override.class)
                        .returns(String.class)
                        .build()
        );


        abstractFactory.addMethod(abstractFactoryCreateMethod.build());


        JavaFile javaFile = JavaFile.builder(this.generatedPackageName, interfaceBuilder.build())
                .build();

        javaFile.writeTo(new File(this.generatedSourceDirectory));

        return ClassName.get(this.generatedPackageName, interfaceName);
    }

    private void generateBasicEvent(EventVo vo, ClassName interfaceClass, TypeSpec.Builder basicFactory) throws Exception {

        String className = vo.className + "Basic";

        ClassName eventClass = ClassName.get(this.generatedPackageName, className);


        //class name is the interface

        FieldSpec CLASS_NAME = FieldSpec.builder(String.class, "CLASS_NAME", Modifier.FINAL, Modifier.STATIC)
                .initializer("\"$L.$L\"",interfaceClass.packageName(), interfaceClass.simpleName()).build();

        TypeSpec.Builder eventBuilder = TypeSpec.classBuilder(className)
                .addSuperinterface(interfaceClass)
                .addField(CLASS_NAME);


        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);


        MethodSpec.Builder basicFactoryCreateMethod = MethodSpec.methodBuilder("create" + vo.className)
                .addModifiers(Modifier.PUBLIC)
                .returns(interfaceClass);


        CodeBlock.Builder factoryCreateStatement = CodeBlock.builder();
        factoryCreateStatement.add("return new $T(", eventClass);

//                public HandledByExternalHandlersEvent createHandledByExternalHandlersEvent(MyId myId, MyNestedId nestedId, String str) {
//                    return new HandledByExternalHandlersEventBasic(myId, nestedId, str);
//                }

        int noOfParams = vo.instanceVariables.keySet().size();
        int count = 0;

        for (String key : vo.instanceVariables.keySet()) {

            TypeName typeName = TypeName.get(vo.instanceVariables.get(key));
            if (typeName.isBoxedPrimitive())
                typeName = typeName.unbox();

            factoryCreateStatement.add(key);
            if(++count!=noOfParams)
                factoryCreateStatement.add(",");

            basicFactoryCreateMethod.addParameter(typeName, key);


            eventBuilder.addField(typeName, key, Modifier.PRIVATE, Modifier.FINAL);

            constructorBuilder.addParameter(typeName, key)
                    .addStatement("this.$N = $N", key, key);

            eventBuilder.addMethod(
                    MethodSpec.methodBuilder("get" + capitalize(key))
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(Override.class)
                            .addStatement("return $N", key)
                            .returns(typeName)
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

        factoryCreateStatement.add(");");

        basicFactoryCreateMethod.addCode(factoryCreateStatement.build());

        eventBuilder.addMethod(constructorBuilder.build());

        JavaFile javaFile = JavaFile.builder(this.generatedPackageName, eventBuilder.build())
                .build();

        javaFile.writeTo(new File(this.generatedSourceDirectory));

        basicFactory.addMethod(basicFactoryCreateMethod.build());



    }


    private void generatePooledEvent(EventVo vo, ClassName interfaceClass, TypeSpec.Builder pooledFactory) throws Exception {

        String className = vo.className + "Pooled";

        FieldSpec CLASS_NAME = FieldSpec.builder(String.class, "CLASS_NAME", Modifier.FINAL, Modifier.STATIC)
                .initializer(CodeBlock.of("$N.class.getName()", className)).build();

        FieldSpec INIT_POOL_SIZE = FieldSpec.builder(int.class, "INIT_POOL_SIZE", Modifier.FINAL, Modifier.STATIC)
                .initializer(CodeBlock.of("50")).build();

        FieldSpec noOfInstances = FieldSpec.builder(int.class, "noOfInstances", Modifier.STATIC)
                .initializer(CodeBlock.of("0")).build();

        ClassName eventClass = ClassName.get(this.generatedPackageName, className);
        ClassName list = ClassName.get("java.util", "List");
        ClassName arrayList = ClassName.get("java.util", "ArrayList");

        TypeName listOfClass = ParameterizedTypeName.get(list, eventClass);

        FieldSpec INSTANCES = FieldSpec.builder(listOfClass, "INSTANCES", Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("new $T<>(INIT_POOL_SIZE)", arrayList).build();

        CodeBlock staticCodeBlock = CodeBlock.builder().beginControlFlow("for (int i = 0; i < INIT_POOL_SIZE; i++)")
                .addStatement("INSTANCES.add(new $L())", className)
                .endControlFlow().build();

        TypeSpec.Builder eventBuilder = TypeSpec.classBuilder(className)
                .addSuperinterface(interfaceClass)
                .addField(CLASS_NAME)
                .addField(INIT_POOL_SIZE)
                .addField(noOfInstances)
                .addField(INSTANCES)
                .addStaticBlock(staticCodeBlock);

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE).build();


        MethodSpec.Builder createBuilder = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.STATIC)
                .returns(eventClass)
                .addStatement("$N  INSTANCE = INSTANCES.get(noOfInstances++)", className);

        MethodSpec.Builder clearBuilder = MethodSpec.methodBuilder("clear").addModifiers(Modifier.PRIVATE);

//                public HandledByExternalHandlersEvent createHandledByExternalHandlersEvent(MyId myId, MyNestedId nestedId, String str) {
//                    return  HandledByExternalHandlersEventPooled.create(myId, nestedId, str);
//                }

        MethodSpec.Builder pooledFactoryCreateMethod = MethodSpec.methodBuilder("create" + vo.className)
                .addModifiers(Modifier.PUBLIC)
                .returns(interfaceClass);


        CodeBlock.Builder factoryCreateStatement = CodeBlock.builder();
        factoryCreateStatement.add("return  $T.create(", eventClass);

        int noOfParams = vo.instanceVariables.keySet().size();
        int count = 0;


        for (String key : vo.instanceVariables.keySet()) {

            TypeName typeName = TypeName.get(vo.instanceVariables.get(key));
            if (typeName.isBoxedPrimitive())
                typeName = typeName.unbox();

            factoryCreateStatement.add(key);
            if(++count!=noOfParams)
                factoryCreateStatement.add(",");

            pooledFactoryCreateMethod.addParameter(typeName, key);


            eventBuilder.addField(typeName, key, Modifier.PRIVATE);

            createBuilder.addParameter(typeName, key)
                    .addStatement("INSTANCE.$N = $N", key, key);
            if (typeName.isPrimitive()) {
                if (TypeName.BOOLEAN == typeName)
                    clearBuilder.addStatement("this.$N = false", key);
                else
                    clearBuilder.addStatement("this.$N = 0", key);
            } else {
                clearBuilder.addStatement("this.$N = null", key);
            }
            eventBuilder.addMethod(
                    MethodSpec.methodBuilder("get" + capitalize(key))
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(Override.class)
                            .addStatement("return $N", key)
                            .returns(typeName)
                            .build()
            );
        }

        factoryCreateStatement.add(");");

        pooledFactoryCreateMethod.addCode(factoryCreateStatement.build());

        createBuilder.addStatement("return INSTANCE");

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

        eventBuilder.addMethod(MethodSpec.methodBuilder("clean")
                .addModifiers(Modifier.STATIC)
                .beginControlFlow("for (int i = 0; i < noOfInstances; i++)")
                .addStatement("INSTANCES.get(i).clear()")
                .endControlFlow()
                .build());


        eventBuilder.addMethod(createBuilder.build());

        eventBuilder.addMethod(clearBuilder.build());


        eventBuilder.addMethod(constructor);

        JavaFile javaFile = JavaFile.builder(this.generatedPackageName, eventBuilder.build())
                .build();

        javaFile.writeTo(new File(this.generatedSourceDirectory));


        pooledFactory.addMethod(pooledFactoryCreateMethod.build());
    }


    private String capitalize(String str) {

        StringBuilder sb = new StringBuilder(str.length());

        sb.append(Character.toUpperCase(str.charAt(0)));

        sb.append(str.substring(1));
        return sb.toString();

    }

}