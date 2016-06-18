package com.dmc.d1.cqrs.codegen;

import com.dmc.d1.cqrs.util.Pooled;
import com.dmc.d1.cqrs.util.ThreadLocalObjectPool;
import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.event.AggregateEventAbstract;
import com.dmc.d1.cqrs.event.AggregateInitialisedEvent;
import com.dmc.d1.cqrs.event.ChronicleAggregateEvent;
import com.dmc.d1.cqrs.util.NewInstanceFactory;
import com.dmc.d1.cqrs.util.Resettable;
import com.squareup.javapoet.*;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 20/05/2016.
 */
class EventAndCommandGenerator {

    enum Type {
        EVENT, COMMAND, DOMAIN
    }

    private final String generatedSourceDirectory;


    EventAndCommandGenerator(
            String generatedSourceDirectory) {

        this.generatedSourceDirectory = checkNotNull(generatedSourceDirectory);
    }

    public void generate() throws Exception {
        URL fileName = getClass().getClassLoader().getResource("CodeGen.xml");

        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(fileName);

        Element root = doc.getRootElement();


        //TODO apply ordering
        for (Element elem : root.getChild("domain").getChildren()) {
            ClassVo vo = parseClassElem(elem);

            ClassName interfaceName = generateInterface(vo, Type.DOMAIN);
            generateBasicClass(vo, interfaceName, Type.DOMAIN);
            generateChronicleClass(vo, interfaceName, Type.DOMAIN);
            generateBuilder(vo, interfaceName, Type.DOMAIN);
        }

        //create chronicle initialised event
        for (Element elem : root.getChild("event").getChildren()) {
            ClassVo vo = parseClassElem(elem);

            ClassName interfaceName = generateInterface(vo, Type.EVENT);
            generateBasicClass(vo, interfaceName, Type.EVENT);
            generateChronicleClass(vo, interfaceName, Type.EVENT);
            generateBuilder(vo, interfaceName, Type.EVENT);
        }

        for (Element elem : root.getChild("command").getChildren()) {
//            ClassVo vo = parseClassElem(elem);
//
//            ClassName interfaceName = generateInterface(vo);
//
//            generateBasicClass(vo, interfaceName);
//            generateChronicleClass(vo, interfaceName, Command.class);
//            generateBuilder(vo, interfaceName);
        }
    }

    private static class ClassVo {
        String packageName;
        String className;
        boolean initialisationEvent;

        Map<String, TypeName> instanceVariables = new LinkedHashMap<>();
    }


    private ClassVo parseClassElem(Element element) throws Exception {


        Class clazz;
        Class parameterizedClazz;

        ClassVo vo = new ClassVo();
        String fullClass = element.getAttributeValue("name");

        if(element.getAttribute("initialisationEvent")!=null && "true".equals(element.getAttributeValue("initialisationEvent"))){
            vo.initialisationEvent = true;
        }

        int pos = fullClass.lastIndexOf(".");

        vo.packageName = fullClass.substring(0, pos);
        vo.className = fullClass.substring(pos + 1);

        for (Element field : element.getChildren("field")) {
            String name = field.getAttributeValue("name");
            clazz = Class.forName(field.getAttributeValue("type"));

            TypeName typeName;
            if (field.getAttribute("parameterized-type") != null) {
                parameterizedClazz = clazz = Class.forName(field.getAttributeValue("parameterized-type"));
                typeName = ParameterizedTypeName.get(clazz, parameterizedClazz);
            } else {
                typeName = TypeName.get(clazz);
            }


            vo.instanceVariables.put(name, typeName);

        }

        return vo;
    }

    private ClassName generateInterface(ClassVo vo, Type type) throws Exception {

        String interfaceName = vo.className;

        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(interfaceName)
                .addModifiers(Modifier.PUBLIC);

        if (Type.EVENT == type) {
            if(vo.initialisationEvent)
                interfaceBuilder.addSuperinterface(AggregateInitialisedEvent.class);
            else
                interfaceBuilder.addSuperinterface(AggregateEvent.class);
        }
        for (String key : vo.instanceVariables.keySet()) {

            TypeName typeName = getFieldTypeName(vo, key);

            interfaceBuilder.addMethod(
                    MethodSpec.methodBuilder("get" + capitalize(key))
                            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                            .returns(typeName)
                            .build()
            );
        }

        JavaFile javaFile = JavaFile.builder(vo.packageName, interfaceBuilder.build())
                .build();

        javaFile.writeTo(new File(this.generatedSourceDirectory));

        return ClassName.get((vo.packageName), interfaceName);
    }

    private void generateBasicClass(ClassVo vo, ClassName interfaceClass, Type type) throws Exception {

        String className = vo.className + "Basic";

        //class name is the interface
        FieldSpec CLASS_NAME = FieldSpec.builder(String.class, "CLASS_NAME", Modifier.FINAL, Modifier.STATIC)
                .initializer("\"$L.$L\"", interfaceClass.packageName(), interfaceClass.simpleName()).build();

        TypeSpec.Builder eventBuilder = TypeSpec.classBuilder(className)
                .addSuperinterface(interfaceClass)
                .addField(CLASS_NAME);

        if (Type.EVENT == type)
            eventBuilder.superclass(AggregateEventAbstract.class);


        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        if (Type.EVENT == type) {
            constructorBuilder.addParameter(String.class, "id")
                    .addStatement("setAggregateId(id)")
                    .addStatement("setClassName(CLASS_NAME)");
        }

        for (String key : vo.instanceVariables.keySet()) {

            TypeName typeName = getFieldTypeName(vo, key);

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

        eventBuilder.addMethod(constructorBuilder.build());

        JavaFile javaFile = JavaFile.builder(vo.packageName, eventBuilder.build())
                .build();

        javaFile.writeTo(new File(this.generatedSourceDirectory));
    }


    private void generateChronicleClass(ClassVo vo, ClassName interfaceClass, Type type) throws Exception {

        String className = vo.className + "Chronicle";

        ClassName chronicleClass = ClassName.get(vo.packageName, className);

        String eventInterfaceName = interfaceClass.packageName() + "." + interfaceClass.simpleName();

        //class name is the interface
        FieldSpec CLASS_NAME = FieldSpec.builder(String.class, "CLASS_NAME", Modifier.FINAL, Modifier.STATIC)
                .initializer("$S", eventInterfaceName).build();

        ClassName factory = ClassName.get(NewInstanceFactory.class);

        ParameterizedTypeName factoryInterface = ParameterizedTypeName.get(factory, chronicleClass);

        TypeSpec newInstanceFactory = TypeSpec.classBuilder("Factory")
                .addSuperinterface(factoryInterface)
                .addModifiers(Modifier.STATIC, Modifier.PRIVATE)
                .addMethod(MethodSpec.methodBuilder("getClassName").addModifiers(Modifier.PUBLIC).returns(String.class).addStatement("return CLASS_NAME").build())
                .addMethod(MethodSpec.methodBuilder("newInstance").addModifiers(Modifier.PUBLIC).returns(chronicleClass).addStatement("return new $T()", chronicleClass).build())
                .build();

        FieldSpec FACTORY = FieldSpec.builder(factoryInterface, "FACTORY", Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("new Factory()").build();

        MethodSpec newInstanceFactoryMethod = MethodSpec.methodBuilder("newInstanceFactory").returns(factoryInterface).addModifiers(Modifier.STATIC)
                .addStatement("return FACTORY").build();

        MethodSpec getNewInstanceFactoryMethod = MethodSpec.methodBuilder("getNewInstanceFactory").returns(factoryInterface).addModifiers(Modifier.PUBLIC)
                .addStatement("return FACTORY")
                .addAnnotation(Override.class)
                .build();

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
                .addField(CLASS_NAME)
                .addType(newInstanceFactory)
                .addField(FACTORY)
                .addMethod(newInstanceFactoryMethod)
                .addMethod(getNewInstanceFactoryMethod);



        if (Type.EVENT == type) {
            eventBuilder.superclass(ChronicleAggregateEvent.class);
        } else if (Type.DOMAIN == type) {
            eventBuilder.addSuperinterface(Resettable.class);
            eventBuilder.addSuperinterface(Marshallable.class);
            eventBuilder.addSuperinterface(Pooled.class);
        }


        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build();


        MethodSpec.Builder setBuilder = MethodSpec.methodBuilder("set");
        MethodSpec.Builder resetBuilder = MethodSpec.methodBuilder("reset").addModifiers(Modifier.PUBLIC);

        if (Type.EVENT == type) {
            setBuilder.addParameter(String.class, "id");
            setBuilder.addStatement("setAggregateId(id)");
            setBuilder.addStatement("setClassName(CLASS_NAME)");
        }

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

        if (Type.EVENT == type) {
            readMarshallableMethod.addStatement("wireIn.read(()-> \"aggregateId\").text(this, (o, b) -> o.setAggregateId(b))");
            readMarshallableMethod.addStatement("setClassName(CLASS_NAME)");
            readMarshallableMethod.addStatement("wireIn.read(() -> \"aggregateClassName\").text(this, (o, b) -> o.setAggregateClassName(b));");
            writeMarshallableMethod.addStatement("wireOut.write(()-> \"aggregateId\").text(getAggregateId())");
            writeMarshallableMethod.addStatement("wireOut.write(() -> \"aggregateClassName\").text(getAggregateClassName());");

        }

        for (String key : vo.instanceVariables.keySet()) {

            TypeName typeName = getFieldTypeName(vo, key);
            String dataTypeMethod = getChronicleDataTypeMethod(typeName);

            if (!key.equals("id")) {
                readMarshallableMethod.addStatement("wireIn.read(()-> $S).$L(this, (o, b) -> o.$L = b)", key, dataTypeMethod, key);
                writeMarshallableMethod.addStatement("wireOut.write(()-> $S).$L($L)", key, dataTypeMethod, key);
            }

            eventBuilder.addField(typeName, key, Modifier.PRIVATE);

            setBuilder.addParameter(typeName, key)
                    .addStatement("this.$N = $N", key, key);

            if (Type.EVENT == type)
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

        eventBuilder.addMethod(readMarshallableMethod.build());
        eventBuilder.addMethod(writeMarshallableMethod.build());

        eventBuilder.addMethod(setBuilder.build());
        eventBuilder.addMethod(resetBuilder.build());
        eventBuilder.addMethod(constructor);

        JavaFile javaFile = JavaFile.builder(vo.packageName, eventBuilder.build())
                .build();

        javaFile.writeTo(new File(this.generatedSourceDirectory));

    }

    @NotNull
    private String getChronicleDataTypeMethod(TypeName typeName) {
        return typeName.isPrimitive() ? "int32" : "text";
    }

    private TypeName getFieldTypeName(ClassVo vo, String key) {
        TypeName typeName = vo.instanceVariables.get(key);
        if (typeName.isBoxedPrimitive())
            typeName = typeName.unbox();
        return typeName;
    }


    private void generateBuilder(ClassVo vo, ClassName interfaceClass, Type type) throws Exception {

        String className = vo.className + "Builder";
        TypeSpec.Builder builderBuilder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC);


        ClassName threadLocalName = ClassName.get("java.lang", "ThreadLocal");
        ClassName builderName = ClassName.get(vo.packageName, className);

        ParameterizedTypeName threadLocalBuilder = ParameterizedTypeName.get(threadLocalName, builderName);


        FieldSpec THREAD_LOCAL = FieldSpec.builder(threadLocalBuilder, "THREAD_LOCAL", Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("new ThreadLocal<>()").build();
        builderBuilder.addField(THREAD_LOCAL);

        builderBuilder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE).build());

        MethodSpec.Builder startBuilding = MethodSpec.methodBuilder("startBuilding")
                .addModifiers(Modifier.STATIC,Modifier.PUBLIC)
                .returns(builderName);

        if (Type.EVENT == type)
            startBuilding.addParameter(String.class, "id");

        startBuilding.beginControlFlow("if (THREAD_LOCAL.get() == null)");
        startBuilding.addStatement("THREAD_LOCAL.set(new $T())", builderName);
        startBuilding.endControlFlow();
        startBuilding.addStatement("$T builder = THREAD_LOCAL.get()", builderName);

        if (Type.EVENT == type)
            startBuilding.addStatement("builder.id = id");


        ClassName chronicleClass = ClassName.get(vo.packageName, vo.className + "Chronicle");
        ClassName basicClass = ClassName.get(vo.packageName, vo.className + "Basic");

        MethodSpec.Builder buildChronicleMethod = MethodSpec.methodBuilder("buildChronicle")
                .addModifiers(Modifier.PUBLIC)
                .returns(vo.initialisationEvent ? ClassName.get(AggregateInitialisedEvent.class) : interfaceClass);
        buildChronicleMethod.addStatement("$T chronicleEvent = $T.allocateObject($T.CLASS_NAME)", chronicleClass, ThreadLocalObjectPool.class, chronicleClass);
        CodeBlock.Builder chronicleSet = CodeBlock.builder().add("chronicleEvent.set(");


        MethodSpec.Builder buildBasicMethod = MethodSpec.methodBuilder("buildBasic")
                .addModifiers(Modifier.PUBLIC)
                .returns(vo.initialisationEvent ? ClassName.get(AggregateInitialisedEvent.class) :interfaceClass);
        CodeBlock.Builder basicNew = CodeBlock.builder().add("$T basic = new $T(", basicClass, basicClass);

        int noOfParams = vo.instanceVariables.keySet().size();


        if (Type.EVENT == type) {
            chronicleSet.add("id$L", noOfParams == 0 ? "" : ",");
            basicNew.add("id$L", noOfParams == 0 ? "" : ",");
        }

        int i = 1;
        if (Type.EVENT == type) {
            builderBuilder.addField(String.class, "id", Modifier.PRIVATE);
        }
        for (String key : vo.instanceVariables.keySet()) {

            TypeName typeName = getFieldTypeName(vo, key);


            builderBuilder.addField(typeName, key, Modifier.PRIVATE);


            if (typeName.isPrimitive()) {
                if (TypeName.BOOLEAN == typeName)
                    startBuilding.addStatement("builder.$N = false", key);
                else
                    startBuilding.addStatement("builder.$N = 0", key);
            } else {
                startBuilding.addStatement("builder.$N = null", key);
            }

            MethodSpec parameterBuilder = MethodSpec.methodBuilder(key).addParameter(typeName, key)
                    .returns(builderName)
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("THREAD_LOCAL.get().$L = $L", key, key)
                    .addStatement("return THREAD_LOCAL.get()").build();

            builderBuilder.addMethod(parameterBuilder);

            chronicleSet.add("$L$L", key, i == noOfParams ? "" : ",");
            basicNew.add("$L$L", key, i == noOfParams ? "" : ",");
            i++;
        }
        chronicleSet.add(");");
        basicNew.add(");");

        buildChronicleMethod.addCode(chronicleSet.build());
        buildChronicleMethod.addStatement("return chronicleEvent");

        buildBasicMethod.addCode(basicNew.build());
        buildBasicMethod.addStatement("return basic");

        startBuilding.addStatement("return builder");

        builderBuilder.addMethod(startBuilding.build());
        builderBuilder.addMethod(buildChronicleMethod.build());
        builderBuilder.addMethod(buildBasicMethod.build());


        JavaFile javaFile = JavaFile.builder(vo.packageName, builderBuilder.build())
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