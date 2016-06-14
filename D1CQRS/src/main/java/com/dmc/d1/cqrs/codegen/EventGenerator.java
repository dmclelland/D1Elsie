package com.dmc.d1.cqrs.codegen;

import com.dmc.d1.cqrs.event.*;
import com.dmc.d1.cqrs.util.InstanceAllocator;
import com.squareup.javapoet.*;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;

import javax.lang.model.element.Modifier;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
            ClassName afName = ClassName.get(generatedPackageName, "EventFactoryAbstract");


            //ClassName markerInterface = ClassName.get(com.dmc.d1.cqrs.event", className);
            TypeSpec.Builder abstractFactory = TypeSpec.interfaceBuilder(afName)
                    .addSuperinterface(EventFactory.class)
                    .addModifiers(Modifier.PUBLIC);


            TypeSpec.Builder basicFactory = TypeSpec.classBuilder("EventFactoryBasic")
                    .addSuperinterface(afName)
                    .addModifiers(Modifier.PUBLIC);


            MethodSpec.Builder allocateInstanceMethod = MethodSpec.methodBuilder("allocateInstance")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(String.class, "eventClass")
                    .returns(ChronicleAggregateEvent.class);



            MethodSpec.Builder chronicleFactoryConstructor = MethodSpec.constructorBuilder()
                    .addStatement("$T<String> eventNames = new $T<>()",List.class, ArrayList.class);


            TypeSpec.Builder chronicleFactory = TypeSpec.classBuilder("EventFactoryChronicle")
                    .addSuperinterface(afName)
                    .addModifiers(Modifier.PUBLIC);


            EventVo aggregateInitialisedVo = new EventVo();
            aggregateInitialisedVo.className = AggregateInitialisedEvent.class.getSimpleName();

            ClassName eventClass = ClassName.get(AggregateInitialisedEvent.class.getPackage().getName(), AggregateInitialisedEvent.class.getSimpleName());

            generateBasicEvent(aggregateInitialisedVo,eventClass,basicFactory);
            generateChronicleEvent(aggregateInitialisedVo,eventClass,chronicleFactory,chronicleFactoryConstructor,allocateInstanceMethod);

            //createAggregateInitialisedEvent
            MethodSpec.Builder abstractFactoryCreateMethod = MethodSpec.methodBuilder("create" + eventClass.simpleName())
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .returns(eventClass);

            abstractFactoryCreateMethod.addParameter(String.class, "id");

            abstractFactory.addMethod(abstractFactoryCreateMethod.build());


            //create chronicle initialised event


            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                EventVo vo = parseEventString(sCurrentLine);

                ClassName interfaceName = generateInterface(vo, abstractFactory);

                generateBasicEvent(vo, interfaceName, basicFactory);
                generateChronicleEvent(vo, interfaceName, chronicleFactory, chronicleFactoryConstructor, allocateInstanceMethod);
            }


            ParameterizedTypeName instanceAllocatorInterface = ParameterizedTypeName.get(InstanceAllocator.class, AggregateEvent.class);

            TypeSpec.Builder instanceAllocator = TypeSpec.classBuilder("ChronicleInstanceAllocator")
                    .addSuperinterface(instanceAllocatorInterface);

            chronicleFactoryConstructor.addStatement("$T allocator = new ChronicleInstanceAllocator()", instanceAllocatorInterface);
            chronicleFactoryConstructor.addStatement("$T.initialise(eventNames,allocator)", AggregateEventPool.class);
            chronicleFactory.addMethod(chronicleFactoryConstructor.build());

            allocateInstanceMethod.addStatement("throw new RuntimeException(\"Unexpected\")");
            instanceAllocator.addMethod(allocateInstanceMethod.build());

            JavaFile javaFileAF = JavaFile.builder(this.generatedPackageName, abstractFactory.build())
                    .build();

            javaFileAF.writeTo(new File(this.generatedSourceDirectory));

            JavaFile javaFileBF = JavaFile.builder(this.generatedPackageName, basicFactory.build())
                    .build();

            javaFileBF.writeTo(new File(this.generatedSourceDirectory));


            JavaFile javaFileIA = JavaFile.builder(this.generatedPackageName, instanceAllocator.build())
                    .build();
            javaFileIA.writeTo(new File(this.generatedSourceDirectory));

            JavaFile javaFilePF = JavaFile.builder(this.generatedPackageName, chronicleFactory.build())
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

        abstractFactoryCreateMethod.addParameter(String.class, "id");


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
                .initializer("\"$L.$L\"", interfaceClass.packageName(), interfaceClass.simpleName()).build();

        TypeSpec.Builder eventBuilder = TypeSpec.classBuilder(className)
                .addSuperinterface(interfaceClass)
                .superclass(AggregateEventAbstract.class)
                .addField(CLASS_NAME);


        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        constructorBuilder.addParameter(String.class, "id")
                .addStatement("setAggregateId(id)")
                .addStatement("setClassName(CLASS_NAME)");


        MethodSpec.Builder basicFactoryCreateMethod = MethodSpec.methodBuilder("create" + vo.className)
                .addModifiers(Modifier.PUBLIC)
                .returns(interfaceClass);


        basicFactoryCreateMethod.addParameter(String.class, "id");

        CodeBlock.Builder factoryCreateStatement = CodeBlock.builder();
        factoryCreateStatement.add("return new $T(id$L ", eventClass, vo.instanceVariables.size() > 0 ? "," : "");

        int noOfParams = vo.instanceVariables.keySet().size();
        int count = 0;

        for (String key : vo.instanceVariables.keySet()) {

            TypeName typeName = TypeName.get(vo.instanceVariables.get(key));
            if (typeName.isBoxedPrimitive())
                typeName = typeName.unbox();

            factoryCreateStatement.add(key);
            if (++count != noOfParams)
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


        factoryCreateStatement.add(");");

        basicFactoryCreateMethod.addCode(factoryCreateStatement.build());

        eventBuilder.addMethod(constructorBuilder.build());

        JavaFile javaFile = JavaFile.builder(this.generatedPackageName, eventBuilder.build())
                .build();

        javaFile.writeTo(new File(this.generatedSourceDirectory));

        basicFactory.addMethod(basicFactoryCreateMethod.build());


    }


    private void generateChronicleEvent(EventVo vo, ClassName interfaceClass, TypeSpec.Builder chronicleFactory,   MethodSpec.Builder chronicleFactoryConstructor,
                                        MethodSpec.Builder  allocateInstanceMethod) throws Exception {

        String className = vo.className + "Chronicle";

        String eventInterfaceName = interfaceClass.packageName()+"."+interfaceClass.simpleName();

        //class name is the interface
        FieldSpec CLASS_NAME = FieldSpec.builder(String.class, "CLASS_NAME", Modifier.FINAL, Modifier.STATIC)
                .initializer("$S",eventInterfaceName).build();


        chronicleFactoryConstructor.addStatement("eventNames.add($S)",eventInterfaceName );

        ClassName eventClass = ClassName.get(this.generatedPackageName, className);

        allocateInstanceMethod.beginControlFlow("if (eventClass.equals($S))",eventInterfaceName);
        allocateInstanceMethod.addStatement("return new $T()", eventClass);
        allocateInstanceMethod.endControlFlow();

        MethodSpec.Builder readMarshallableMethod = MethodSpec.methodBuilder("readMarshallable")
                .addParameter(WireIn.class, "wireIn")
                .addAnnotation(Override.class)
                .addException(IORuntimeException.class)
                .addModifiers(Modifier.PUBLIC);

        MethodSpec.Builder writeMarshallableMethod = MethodSpec.methodBuilder("writeMarshallable")
                .addParameter(WireOut.class, "wireOut")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC);

        TypeSpec.Builder eventBuilder = TypeSpec.classBuilder(className)
                .addSuperinterface(interfaceClass)
                .superclass(ChronicleAggregateEvent.class)
                .addField(CLASS_NAME);

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .build();


        MethodSpec.Builder setBuilder = MethodSpec.methodBuilder("set").addModifiers();
        MethodSpec.Builder resetBuilder = MethodSpec.methodBuilder("reset").addModifiers(Modifier.PUBLIC);

        setBuilder.addParameter(String.class, "id");
        setBuilder.addStatement("setAggregateId(id)");
        setBuilder.addStatement(" setClassName(CLASS_NAME)");

        MethodSpec.Builder chronicleFactoryCreateMethod = MethodSpec.methodBuilder("create" + vo.className)
                .addModifiers(Modifier.PUBLIC)
                .returns(interfaceClass);

        chronicleFactoryCreateMethod.addParameter(String.class, "id");


//        void set(String id, int i) {
//            setAggregateId(id);
//            setClassName(CLASS_NAME);
//            this.i = i;
//        }
//
//        public void reset() {
//            setAggregateId(null);
//            setClassName(null);
//            this.i = 0;
//        }

        CodeBlock.Builder factoryCreateStatement = CodeBlock.builder();
        factoryCreateStatement.addStatement("$T event = ($T) AggregateEventPool.allocate($S)", eventClass, eventClass, eventInterfaceName);


        int noOfParams = vo.instanceVariables.keySet().size();

        factoryCreateStatement.add("event.set(id$L", noOfParams > 0 ? "," : "");


        int count = 0;

//        @Override
//        public void readMarshallable(WireIn wireIn) throws IORuntimeException {
//            wireIn.read(()-> "id").text(this, (o, b) -> o.setAggregateId(b));
//            wireIn.read(()-> "i").int32(this, (o, b) -> o.i = b);
//            setClassName(CLASS_NAME);
//        }
//
//        @Override
//        public void writeMarshallable(WireOut wireOut) {
//            wireOut.write(()-> "id").text(getAggregateId());
//            wireOut.write(()-> "i").int32(i);
//        }

        readMarshallableMethod.addStatement("wireIn.read(()-> \"id\").text(this, (o, b) -> o.setAggregateId(b))");
        readMarshallableMethod.addStatement("setClassName(CLASS_NAME)");
        readMarshallableMethod.addStatement("wireIn.read(() -> \"aggregateClassName\").text(this, (o, b) -> o.setAggregateClassName(b));");

        writeMarshallableMethod.addStatement("wireOut.write(()-> \"id\").text(getAggregateId())");
        writeMarshallableMethod.addStatement("wireOut.write(() -> \"aggregateClassName\").text(getAggregateClassName());");


        for (String key : vo.instanceVariables.keySet()) {

            TypeName typeName = TypeName.get(vo.instanceVariables.get(key));
            if (typeName.isBoxedPrimitive())
                typeName = typeName.unbox();

            factoryCreateStatement.add(key);
            if (++count != noOfParams)
                factoryCreateStatement.add(",");

            chronicleFactoryCreateMethod.addParameter(typeName, key);

            String dataTypeMethod = typeName.isPrimitive() ? "int32" : "text";


            if(!key.equals("id")) {
                readMarshallableMethod.addStatement("wireIn.read(()-> $S).$L(this, (o, b) -> o.$L = b)", key, dataTypeMethod, key);
                writeMarshallableMethod.addStatement("wireOut.write(()-> $S).$L($L)", key, dataTypeMethod, key);
            }

            eventBuilder.addField(typeName, key, Modifier.PRIVATE);

            setBuilder.addParameter(typeName, key)
                    .addStatement("this.$N = $N", key, key);



            resetBuilder.addStatement("setAggregateId(null)");
            if (typeName.isPrimitive()) {
                if (TypeName.BOOLEAN == typeName)
                    resetBuilder.addStatement("this.$N = false", key);
                else
                    resetBuilder.addStatement("this.$N = 0", key);
            } else {
                resetBuilder.addStatement("this.$N = null", key);
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
        factoryCreateStatement.addStatement("return event");

        chronicleFactoryCreateMethod.addCode(factoryCreateStatement.build());


        eventBuilder.addMethod(readMarshallableMethod.build());
        eventBuilder.addMethod(writeMarshallableMethod.build());

        eventBuilder.addMethod(setBuilder.build());
        eventBuilder.addMethod(resetBuilder.build());
        eventBuilder.addMethod(constructor);

        JavaFile javaFile = JavaFile.builder(this.generatedPackageName, eventBuilder.build())
                .build();

        javaFile.writeTo(new File(this.generatedSourceDirectory));


        chronicleFactory.addMethod(chronicleFactoryCreateMethod.build());
    }


    private String capitalize(String str) {

        StringBuilder sb = new StringBuilder(str.length());

        sb.append(Character.toUpperCase(str.charAt(0)));

        sb.append(str.substring(1));
        return sb.toString();

    }

}