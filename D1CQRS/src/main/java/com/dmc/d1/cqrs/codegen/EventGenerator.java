package com.dmc.d1.cqrs.codegen;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.event.AggregateEventAbstract;
import com.dmc.d1.cqrs.event.JournalableAggregateEvent;
import com.dmc.d1.cqrs.util.StateEquals;
import com.dmc.d1.domain.Id;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Created by davidclelland on 20/05/2016.
 */
public class EventGenerator {


    public static void main(String[] args) {
        checkState(args.length == 2);

        String configFilePath = args[0];
        String generatedSourceDirectory = args[1];

        try {

            EventGenerator generator = new EventGenerator(configFilePath,
                    generatedSourceDirectory);

            generator.generate();

        } catch (Exception e) {
            throw new RuntimeException("Unable to generate code", e);
        }
    }

    enum Type {
        EVENT, DOMAIN
    }

    private final String generatedSourceDirectory;
    private final String configFilePath;


    EventGenerator(
            String configFilePath, String generatedSourceDirectory) {
        this.configFilePath = checkNotNull(configFilePath);
        this.generatedSourceDirectory = checkNotNull(generatedSourceDirectory);
    }

    private Map<String, ClassVo> vos = new HashMap<>();

    public void generate() throws Exception {
        File configFile = new File(configFilePath);

        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(configFile);

        Element root = doc.getRootElement();


        //TODO apply ordering
        for (Element elem : root.getChild("domain").getChildren()) {
            ClassVo vo = parseClassElem(elem);

            ClassName interfaceName = generateInterface(vo, Type.DOMAIN);
            generateImmutableClass(vo, interfaceName, Type.DOMAIN);

            if (vo.updatable)
                generateMutableClass(vo, interfaceName);

            generateJournalableClass(vo, interfaceName, Type.DOMAIN);
            generateBuilder(vo, interfaceName, Type.DOMAIN);
            vos.put(vo.getFullClassname(), vo);
        }

        //create Journalable initialised event
        for (Element elem : root.getChild("event").getChildren()) {
            ClassVo vo = parseClassElem(elem);

            ClassName interfaceName = generateInterface(vo, Type.EVENT);
            generateImmutableClass(vo, interfaceName, Type.EVENT);
            generateJournalableClass(vo, interfaceName, Type.EVENT);
            generateBuilder(vo, interfaceName, Type.EVENT);
            vos.put(vo.getFullClassname(), vo);
        }

        for (Element elem : root.getChild("command").getChildren()) {
//            ClassVo vo = parseClassElem(elem);
//
//            ClassName interfaceName = generateInterface(vo);
//
//            generateImmutableClass(vo, interfaceName);
//            generateJournalableClass(vo, interfaceName, Command.class);
//            generateBuilder(vo, interfaceName);
        }
    }


    private ClassVo parseClassElem(Element element) throws Exception {


        ClassVo vo = new ClassVo();
        String fullClass = element.getAttributeValue("name");

        int pos = fullClass.lastIndexOf(".");

        vo.packageName = fullClass.substring(0, pos);
        vo.className = fullClass.substring(pos + 1);

        if (element.getAttribute("updatable") != null && "true".equals(element.getAttributeValue("updatable"))) {
            vo.updatable = true;
        }

        if (element.getAttribute("cache-key") != null) {
            vo.cacheKey = element.getAttributeValue("cache-key");
        }

        for (Element field : element.getChildren("field")) {
            String name = field.getAttributeValue("name");
            String classNameStr = field.getAttributeValue("type");

            ClassName className = classFromFullName(classNameStr);


            ClassName parameterizedClazz = null;

            TypeName typeName;
            String keyName = null;
            ClassName keyType = null;
            if (field.getAttribute("parameterized-type") != null) {
                String ptFullName = field.getAttributeValue("parameterized-type");
                parameterizedClazz = classFromFullName(ptFullName);


                if (field.getAttribute("key") != null) {
                    keyName = field.getAttributeValue("key");
                    FieldDataVo keyData = vos.get(ptFullName).instanceVariables.get(keyName);

                    typeName = ParameterizedTypeName.get(className, keyData.type, parameterizedClazz);
                } else {

                    typeName = ParameterizedTypeName.get(className, parameterizedClazz);
                }

            } else {
                if (className.isBoxedPrimitive()) {

                    if (field.getAttribute("toPrimitive") == null ||
                            "true".equals(field.getAttributeValue("toPrimitive"))) {
                        typeName = className.unbox();
                    } else {
                        typeName = className;
                    }
                } else {
                    typeName = className;
                }
            }

            ClassName concreteType = null;
            if (field.getAttribute("concrete-type") != null) {
                concreteType = classFromFullName(field.getAttributeValue("concrete-type"));
            }


            FieldDataVo fieldData = new FieldDataVo();
            if (!typeName.isPrimitive())
                fieldData.className = classNameStr;

            fieldData.type = typeName;
            fieldData.concreteType = concreteType;
            fieldData.chronicleType = chronicleType(typeName);
            fieldData.parameterizedClass = parameterizedClazz;
            fieldData.updatable = field.getAttribute("updatable") == null
                    || field.getAttributeValue("updatable").equals("false") ? false : true;


            fieldData.keyName = keyName;
            fieldData.keyType = keyType;


            vo.instanceVariables.put(name, fieldData);

        }

        return vo;
    }


    private ClassName generateInterface(ClassVo vo, Type type) throws Exception {

        String interfaceName = vo.className;

        ClassName interfaceClass = ClassName.get((vo.packageName), interfaceName);

        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(interfaceName)
                .addModifiers(Modifier.PUBLIC);

        if (Type.EVENT == type) {
            interfaceBuilder.addSuperinterface(AggregateEvent.class);
        }

        ParameterizedTypeName stateEquals = ParameterizedTypeName.get(ClassName.get(StateEquals.class), interfaceClass);
        interfaceBuilder.addSuperinterface(stateEquals);

        for (String key : vo.instanceVariables.keySet()) {

            FieldDataVo fieldData = getFieldData(vo, key);

            interfaceBuilder.addMethod(
                    MethodSpec.methodBuilder("get" + capitalize(key))
                            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                            .returns(fieldData.type)
                            .build()
            );
        }

//
//        interfaceBuilder.addMethod(MethodSpec.methodBuilder("deepClone")
//                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
//                .returns(interfaceClass)
//                .build());

        JavaFile javaFile = JavaFile.builder(vo.packageName, interfaceBuilder.build())
                .build();

        javaFile.writeTo(new File(this.generatedSourceDirectory));

        return interfaceClass;
    }

    private void generateImmutableClass(ClassVo vo, ClassName interfaceClass, Type type) throws Exception {

        String className = vo.className + "Immutable";

        ClassName immutableClass = ClassName.get(vo.packageName, className);


        //class name is the interface
        FieldSpec CLASS_NAME = FieldSpec.builder(String.class, "CLASS_NAME", Modifier.FINAL, Modifier.STATIC)
                .initializer("\"$L.$L\"", interfaceClass.packageName(), interfaceClass.simpleName()).build();

        TypeSpec.Builder eventBuilder = TypeSpec.classBuilder(className)
                .addSuperinterface(interfaceClass)
                .addSuperinterface(Immutable.class)
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
            FieldDataVo fieldData = getFieldData(vo, key);
            eventBuilder.addField(fieldData.type, key, Modifier.PRIVATE, Modifier.FINAL);

            //if domain object then create a immutable copy method
            if ("object".equals(fieldData.chronicleType) || "sequence".equals(fieldData.chronicleType)) {

                String name = fullNameFromClass("object".equals(fieldData.chronicleType)
                        ? (ClassName) fieldData.type : fieldData.parameterizedClass);
                ClassVo domain = vos.get(name);

                if (domain != null) {
                    eventBuilder.addMethod(
                            buildImmutableCopy(domain, key, fieldData));

                    constructorBuilder.addParameter(fieldData.type, key)
                            .addStatement("this.$L = immutable$L($L)", key, capitalize(key), key);
                } else {
                    if ("sequence".equals(fieldData.chronicleType)) {
                        constructorBuilder.addParameter(fieldData.type, key)
                                .addStatement("this.$L = new $T($L)", key, fieldData.concreteType, key);
                    } else {
                        constructorBuilder.addParameter(fieldData.type, key)
                                .addStatement("this.$L = $L", key, key);
                    }
                }
            } else {
                constructorBuilder.addParameter(fieldData.type, key)
                        .addStatement("this.$L = $L", key, key);
            }

            MethodSpec.Builder accessorBuilder = MethodSpec.methodBuilder("get" + capitalize(key))
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .returns(fieldData.type);

            if ("sequence".equals(fieldData.chronicleType)) {
                accessorBuilder.addStatement("return new $T($L)", fieldData.concreteType, key);
            } else {
                accessorBuilder.addStatement("return $L", key);
            }

            eventBuilder.addMethod(accessorBuilder.build());
        }

        eventBuilder.addMethod(constructorBuilder.build());

        //eventBuilder.addMethod(deepCloneBuilder(vo, immutableClass, type));
        eventBuilder.addMethod(equalsBuilder(vo, immutableClass, type));
        eventBuilder.addMethod(hashCodeBuilder(vo, type));
        eventBuilder.addMethod(stateEqualsBuilder(vo));

        JavaFile javaFile = JavaFile.builder(vo.packageName, eventBuilder.build())
                .build();

        javaFile.writeTo(new File(this.generatedSourceDirectory));
    }


    private void generateMutableClass(ClassVo vo, ClassName interfaceClass) throws Exception {


        String className = vo.className + "Mutable";

        ClassName mutableClass = ClassName.get(vo.packageName, className);

        //class name is the interface
        FieldSpec CLASS_NAME = FieldSpec.builder(String.class, "CLASS_NAME", Modifier.FINAL, Modifier.STATIC)
                .initializer("\"$L.$L\"", interfaceClass.packageName(), interfaceClass.simpleName()).build();

        TypeSpec.Builder eventBuilder = TypeSpec.classBuilder(className)
                .addSuperinterface(interfaceClass)
                .addSuperinterface(Mutable.class)
                .addField(CLASS_NAME);


        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (String key : vo.instanceVariables.keySet()) {
            FieldDataVo fieldData = getFieldData(vo, key);
            if (fieldData.updatable) {
                eventBuilder.addField(fieldData.type, key, Modifier.PRIVATE);
            } else {
                eventBuilder.addField(fieldData.type, key, Modifier.PRIVATE, Modifier.FINAL);
            }
            //if domain object then create a immutable copy method
            if ("object".equals(fieldData.chronicleType) || "sequence".equals(fieldData.chronicleType)) {

                String name = fullNameFromClass("object".equals(fieldData.chronicleType) ? (ClassName) fieldData.type
                        : fieldData.parameterizedClass);

                ClassVo domain = vos.get(name);

                if (domain != null) {
                    if (fieldData.updatable) {
                        eventBuilder.addMethod(
                                buildMutableCopy(domain, key, fieldData));

                        constructorBuilder.addParameter(fieldData.type, key)
                                .addStatement("this.$L = mutable$L($L)", key, capitalize(key), key);
                    } else {
                        eventBuilder.addMethod(
                                buildImmutableCopy(domain, key, fieldData));

                        constructorBuilder.addParameter(fieldData.type, key)
                                .addStatement("this.$L = immutable$L($L)", key, capitalize(key), key);
                    }
                } else {
                    if ("sequence".equals(fieldData.chronicleType)) {
                        constructorBuilder.addParameter(fieldData.type, key)
                                .addStatement("this.$L = new $T($L)", key, fieldData.concreteType, key);
                    } else {
                        constructorBuilder.addParameter(fieldData.type, key)
                                .addStatement("this.$L = $L", key, key);
                    }
                }
            } else {
                constructorBuilder.addParameter(fieldData.type, key)
                        .addStatement("this.$L = $L", key, key);
            }

            MethodSpec.Builder accessorBuilder = MethodSpec.methodBuilder("get" + capitalize(key))
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .returns(fieldData.type);

            if ("sequence".equals(fieldData.chronicleType) && !fieldData.updatable) {
                accessorBuilder.addStatement("return new $T($L)", fieldData.concreteType, key);
            } else {
                accessorBuilder.addStatement("return $L", key);
            }

            eventBuilder.addMethod(accessorBuilder.build());
        }

        eventBuilder.addMethod(constructorBuilder.build());

        //eventBuilder.addMethod(deepCloneBuilder(vo, immutableClass, type));
        eventBuilder.addMethod(equalsBuilder(vo, mutableClass, Type.DOMAIN));
        eventBuilder.addMethod(hashCodeBuilder(vo, Type.DOMAIN));
        eventBuilder.addMethod(stateEqualsBuilder(vo));

        JavaFile javaFile = JavaFile.builder(vo.packageName, eventBuilder.build())
                .build();

        javaFile.writeTo(new File(this.generatedSourceDirectory));
    }


    @NotNull
    private MethodSpec buildImmutableCopy(ClassVo domain, String key, FieldDataVo fieldData) {
        ClassName builderClass = ClassName.get(domain.packageName, domain.className + "Builder");

        MethodSpec.Builder builder = MethodSpec.methodBuilder("immutable" + capitalize(key))
                .addParameter(fieldData.type, key)
                .addModifiers(Modifier.PRIVATE)
                .returns(fieldData.type);

        if ("object".equals(fieldData.chronicleType)) {


            builder.addStatement("return $L instanceof $T ? " +
                    "$L : $T.copyBuilder($L).buildImmutable()", key, Immutable.class, key, builderClass, key);
        } else if ("sequence".equals(fieldData.chronicleType)) {


            if ("java.util.Map".equals(fieldData.className)) {

                builder.addStatement("$T newMap = new $T()", fieldData.type, fieldData.concreteType);
                builder.addStatement("$L.forEach((key, val) -> newMap.put(key, val instanceof $T ? val : $T.copyBuilder(val).buildImmutable()))", key, Immutable.class, builderClass);
                builder.addStatement("return newMap");
            } else {
                builder.addStatement("$T newList = new $T()", fieldData.type, fieldData.concreteType);
                builder.addStatement("$L.forEach((val) -> newList.add(val instanceof $T ? val : $T.copyBuilder(val).buildImmutable()))", key, Immutable.class, builderClass);
                builder.addStatement("return newList");

            }
        }
        return builder.build();
    }


    @NotNull
    private MethodSpec buildMutableCopy(ClassVo domain, String key, FieldDataVo fieldData) {
        ClassName builderClass = ClassName.get(domain.packageName, domain.className + "Builder");

        MethodSpec.Builder builder = MethodSpec.methodBuilder("mutable" + capitalize(key))
                .addParameter(fieldData.type, key)
                .addModifiers(Modifier.PRIVATE)
                .returns(fieldData.type);

        if ("object".equals(fieldData.chronicleType)) {


            builder.addStatement("return $L instanceof $T ? " +
                    "$L : $T.copyBuilder($L).buildMutable()", key, Immutable.class, key, builderClass, key);
        } else if ("sequence".equals(fieldData.chronicleType)) {


            if ("java.util.Map".equals(fieldData.className)) {

                builder.addStatement("$T newMap = new $T()", fieldData.type, fieldData.concreteType);
                builder.addStatement("$L.forEach((key, val) -> newMap.put(key, val instanceof $T ? val : $T.copyBuilder(val).buildMutable()))", key, Mutable.class, builderClass);
                builder.addStatement("return newMap");
            } else {
                builder.addStatement("$T newList = new $T()", fieldData.type, fieldData.concreteType);
                builder.addStatement("$L.forEach((val) -> newList.add(val instanceof $T ? val : $T.copyBuilder(val).buildMutable()))", key, Mutable.class, builderClass);
                builder.addStatement("return newList");

            }
        }
        return builder.build();
    }


    @NotNull
    private MethodSpec buildJournalableCopy(ClassVo domain, String key, FieldDataVo fieldData) {
        ClassName builderClass = ClassName.get(domain.packageName, domain.className + "Builder");

        MethodSpec.Builder builder = MethodSpec.methodBuilder("journalable" + capitalize(key))
                .addParameter(fieldData.type, key)
                .addModifiers(Modifier.PRIVATE)
                .returns(fieldData.type);

        if ("object".equals(fieldData.chronicleType)) {

            // return basket instanceof Journalable ? basket : pooled ?  BasketBuilder.copyBuilder(basket).buildJournalable() : BasketBuilder.copyBuilder(basket).buildJournalable();

            builder.addStatement("return $L instanceof $T ? " +
                            "$L :$T.copyBuilder($L).buildJournalable()",
                    key, Journalable.class, key, builderClass, key);
        } else if ("sequence".equals(fieldData.chronicleType)) {

            if ("java.util.Map".equals(fieldData.className)) {
                builder.beginControlFlow("if (!$L.isEmpty() && $L.values().iterator().next() instanceof Journalable)", key, key);
                builder.addStatement("return $L", key);
                builder.endControlFlow();

                builder.addStatement("$T newMap = new $T()", fieldData.type, fieldData.concreteType);

                builder.addStatement("$L.forEach((key, val) -> newMap.put(key, val instanceof $T ? val : $T.copyBuilder(val).buildJournalable()))",
                        key, Journalable.class, builderClass);
                builder.addStatement("return newMap");
            } else {
                builder.beginControlFlow("if (!$L.isEmpty() && $L.get(0) instanceof Journalable)", key, key);
                builder.addStatement("return $L", key);
                builder.endControlFlow();
                builder.addStatement("$T newList = new $T()", fieldData.type, fieldData.concreteType);


                builder.addStatement("$L.forEach((val) -> newList.add(val instanceof $T ? val : $T.copyBuilder(val).buildJournalable()))",
                        key, Journalable.class, builderClass);
                builder.addStatement("return newList");

            }
        }
        return builder.build();

    }

    private void generateJournalableClass(ClassVo vo, ClassName interfaceClass, Type type) throws Exception {

        String className = vo.className + "Journalable";

        ClassName journalableClass = ClassName.get(vo.packageName, className);

        String eventInterfaceName = interfaceClass.packageName() + "." + interfaceClass.simpleName();

        //class name is the interface
        FieldSpec CLASS_NAME = FieldSpec.builder(String.class, "CLASS_NAME", Modifier.FINAL, Modifier.STATIC)
                .initializer("$S", eventInterfaceName).build();

        ClassName supplier = ClassName.get(Supplier.class);

        ParameterizedTypeName supplierInterface = ParameterizedTypeName.get(supplier, journalableClass);


        FieldSpec SUPPLIER = FieldSpec.builder(supplierInterface, "SUPPLIER", Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("$T::new", journalableClass).build();

        MethodSpec newInstanceFactoryMethod = MethodSpec.methodBuilder("newInstanceFactory")
                .returns(supplierInterface).addModifiers(Modifier.STATIC)
                .addStatement("return SUPPLIER").build();


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
                .addSuperinterface(Journalable.class)
                .addField(CLASS_NAME)
                .addField(SUPPLIER)
                .addMethod(newInstanceFactoryMethod);

        if (Type.EVENT == type) {
            eventBuilder.superclass(JournalableAggregateEvent.class);
        } else if (Type.DOMAIN == type) {
            eventBuilder.addSuperinterface(Marshallable.class);
        }


        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement(Type.EVENT == type ? "setClassName(CLASS_NAME)" : "")
                .build();


        MethodSpec.Builder setBuilder = MethodSpec.methodBuilder("set");
        MethodSpec.Builder resetBuilder = MethodSpec.methodBuilder("reset").addModifiers(Modifier.PUBLIC);

        if (Type.EVENT == type) {
            setBuilder.addParameter(String.class, "id");
            setBuilder.addStatement("setAggregateId(id)");
            setBuilder.addStatement("setClassName(CLASS_NAME)");
        }


        if (Type.EVENT == type) {
            readMarshallableMethod.addStatement("wireIn.read(()-> \"aggregateId\").text(this, (o, b) -> o.setAggregateId(b))");
            readMarshallableMethod.addStatement("setClassName(CLASS_NAME)");
            readMarshallableMethod.addStatement("wireIn.read(() -> \"aggregateClassName\").text(this, (o, b) -> o.setAggregateClassName(b))");
            writeMarshallableMethod.addStatement("wireOut.write(()-> \"aggregateId\").text(getAggregateId())");
            writeMarshallableMethod.addStatement("wireOut.write(() -> \"aggregateClassName\").text(getAggregateClassName())");

        }
//
//
//        wireIn.read(() -> "basketConstituents").sequence(this.basketConstituents, (l, v) -> {
//            while (v.hasNextSequenceItem()) l.add(v.object(BasketConstituent.class));
//        });
//        wireOut.write(() -> "basketConstituents").sequence(this.basketConstituents);

        for (String key : vo.instanceVariables.keySet()) {
            FieldDataVo fieldData = getFieldData(vo, key);

            if (!key.equals("id")) {
                if ("sequence".equals(fieldData.chronicleType)) {
                    TypeName parameterizedType = fieldData.parameterizedClass;

                    readMarshallableMethod.addCode("wireIn.read(()-> $S).sequence(this.$L,(l,v) -> {", key, key);
                    readMarshallableMethod.addCode("while (v.hasNextSequenceItem()){");


                    if ("java.util.Map".equals(fieldData.className)) {
                        readMarshallableMethod.addCode("$T o = v.object($T.class);", parameterizedType, parameterizedType);
                        readMarshallableMethod.addCode("l.put(o.get$L(), o);}", capitalize(fieldData.keyName));
                    } else {
                        readMarshallableMethod.addCode("l.add(v.object($T.class));}", parameterizedType);
                    }
                    readMarshallableMethod.addCode(" });\n");

                    writeMarshallableMethod.addCode("wireOut.write(() -> $S)", key);
                    if ("java.util.Map".equals(fieldData.className)) {
                        writeMarshallableMethod.addCode(".sequence(this.$L.values());\n", key);
                    } else {
                        writeMarshallableMethod.addCode(".sequence(this.$L);\n", key);
                    }
                } else if ("enum".equals(fieldData.chronicleType)) {

                    //wireIn.read(()-> "tradeDirection").asEnum(TradeDirection.class, this, (o, b) -> o.tradeDirection = b);
                    //wireOut.write(()-> "tradeDirection").asEnum(TradeDirection.BUY);

                    readMarshallableMethod.addStatement("wireIn.read(()-> $S).asEnum($T.class, this, (o, b) -> o.$L = b)", key, fieldData.type, key);

                    writeMarshallableMethod.addStatement("wireOut.write(()-> $S).asEnum(this.$L)", key, key);


                } else if ("object".equals(fieldData.chronicleType)) {

                    //Security e = ThreadLocalObjectPool.allocateObject(Security.class.getName());
//                    wireIn.read(() -> "security").object(e, Security.class);
//                    this.security = e;

                    //only allocate objects for defined xml types
                    String name = fullNameFromClass((ClassName) fieldData.type);

                    ClassVo domain = vos.get(name);

                    if (domain != null) {
                        //wireIn.read(() -> "basket").object(Basket.class, this, (o,b) -> o.basket = b);
                        readMarshallableMethod.addStatement("wireIn.read(() -> $S).object($T.class, this, (o,b) -> o.$L = b)", key, fieldData.type, key);
                        writeMarshallableMethod.addStatement("wireOut.write(()-> $S).object($T.class, this.$L)", key, fieldData.type, key);
                    } else {
                        //if id then treat as string
                        if (Id.class.isAssignableFrom(Class.forName(name))) {
                            readMarshallableMethod.addStatement("wireIn.read(()-> $S).text(this, (o, b) -> o.$L = $T.from(b))", key, key, fieldData.type);
                            writeMarshallableMethod.addStatement("wireOut.write(()-> $S).text($L.asString())", key, key);
                        }
                    }

                    //readMarshallableMethod.addStatement("wireIn.read(()-> $S).object($T.class,this, (o,b) -> o.$L = b)", key, fieldData.type, key);
                } else {
                    readMarshallableMethod.addStatement("wireIn.read(()-> $S).$L(this, (o, b) -> o.$L = b)", key, fieldData.chronicleType, key);
                    writeMarshallableMethod.addStatement("wireOut.write(()-> $S).$L($L)", key, fieldData.chronicleType, key);
                }
            }

            //if domain object then create a immutable copy method
            if ("object".equals(fieldData.chronicleType) || "sequence".equals(fieldData.chronicleType)) {

                String name = fullNameFromClass("object".equals(fieldData.chronicleType) ? (ClassName) fieldData.type : fieldData.parameterizedClass);
                ClassVo domain = vos.get(name);


                if (domain != null) {
                    eventBuilder.addMethod(
                            buildJournalableCopy(domain, key, fieldData));

                    setBuilder.addParameter(fieldData.type, key)
                            .addStatement("this.$L = journalable$L($L)", key, capitalize(key), key);
                } else {
                    if ("sequence".equals(fieldData.chronicleType)) {
                        setBuilder.addParameter(fieldData.type, key)
                                .addStatement("this.$L = new $T($L)", key, fieldData.concreteType, key);
                    } else {
                        setBuilder.addParameter(fieldData.type, key)
                                .addStatement("this.$L = $L", key, key);
                    }
                }
            } else {
                setBuilder.addParameter(fieldData.type, key)
                        .addStatement("this.$L = $L", key, key);
            }


            FieldSpec.Builder field = FieldSpec.builder(fieldData.type, key, Modifier.PRIVATE);
            if ("sequence".equals(fieldData.chronicleType)) {
                field.initializer(CodeBlock.builder().add("new $T()", fieldData.concreteType).build());
            }

            eventBuilder.addField(field.build());


            if (Type.EVENT == type)
                resetBuilder.addStatement("setAggregateId(null)");

            if (fieldData.type.isPrimitive()) {
                if (TypeName.BOOLEAN == fieldData.type)
                    resetBuilder.addStatement("this.$N = false", key);
                else
                    resetBuilder.addStatement("this.$N = 0", key);
            } else {
                if ("sequence".equals(fieldData.chronicleType)) {
                    resetBuilder.addStatement("this.$N.clear()", key);
                } else {
                    resetBuilder.addStatement("this.$N = null", key);
                }
            }

            eventBuilder.addMethod(
                    MethodSpec.methodBuilder("get" + capitalize(key))
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(Override.class)
                            .addStatement("return $N", key)
                            .returns(fieldData.type)
                            .build()
            );
        }


        eventBuilder.addMethod(readMarshallableMethod.build());
        eventBuilder.addMethod(writeMarshallableMethod.build());

        eventBuilder.addMethod(setBuilder.build());
        eventBuilder.addMethod(resetBuilder.build());
        //eventBuilder.addMethod(deepCloneBuilder(vo, journalableClass, type));
        eventBuilder.addMethod(stateEqualsBuilder(vo));
        eventBuilder.addMethod(equalsBuilder(vo, journalableClass, type));
        eventBuilder.addMethod(hashCodeBuilder(vo, type));
        eventBuilder.addMethod(constructor);

        JavaFile javaFile = JavaFile.builder(vo.packageName, eventBuilder.build())
                .build();

        javaFile.writeTo(new File(this.generatedSourceDirectory));
    }


    private MethodSpec equalsBuilder(ClassVo vo, ClassName className, Type type) {

        MethodSpec.Builder equalsBuilder = MethodSpec.methodBuilder("equals")
                .addParameter(Object.class, "o")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN);

        equalsBuilder.addStatement("if (this == o) return true");
        equalsBuilder.addStatement("if (o == null || getClass() != o.getClass()) return false");
        equalsBuilder.addStatement("$T that = ($T)o", className, className);

        if (Type.EVENT == type) {

            equalsBuilder.addStatement("if (getAggregateId() != null ? !getAggregateId().equals(that.getAggregateId()) : that.getAggregateId() != null) return false");
            equalsBuilder.addStatement("if (getClassName() != null ? !getClassName().equals(that.getClassName()) : that.getClassName() != null) return false");
            equalsBuilder.addStatement("if (getAggregateClassName() != null ? !getAggregateClassName().equals(that.getAggregateClassName()) : that.getAggregateClassName() != null) return false");
        }


        for (String key : vo.instanceVariables.keySet()) {
            FieldDataVo fieldData = vo.instanceVariables.get(key);
            if (fieldData.type.isPrimitive()) {
                equalsBuilder.addStatement("if ($L!=that.$L) return false", key, key);
            } else if ("enum".equals(fieldData.chronicleType)) {
                equalsBuilder.addStatement("if ($L != null ? $L!=that.$L : that.$L != null) return false", key, key, key, key);

            } else {
                equalsBuilder.addStatement("if ($L != null ? !$L.equals(that.$L) : that.$L != null) return false", key, key, key, key);
            }
        }
        equalsBuilder.addStatement("return true");
        return equalsBuilder.build();

//        if (divisor != that.divisor) return false;
//        if (ric != null ? !ric.equals(that.ric) : that.ric != null) return false;
//        if (tradeDate != null ? !tradeDate.equals(that.tradeDate) : that.tradeDate != null) return false;
//        if (security != null ? !security.equals(that.security) : that.security != null) return false;
//        if (basketConstituents != null ? !basketConstituents.equals(that.basketConstituents) : that.basketConstituents != null)
//            return false;


    }


    private MethodSpec stateEqualsBuilder(ClassVo vo) {

        MethodSpec.Builder hasSameStateBuilder = MethodSpec.methodBuilder("stateEquals")
                .addParameter(classFromFullName(vo.getFullClassname()), "o")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN);

        for (String key : vo.instanceVariables.keySet()) {
            FieldDataVo fieldData = vo.instanceVariables.get(key);


            if (fieldData.type.isPrimitive()) {
                hasSameStateBuilder.addStatement("if (o.get$L()!=this.$L) return false", capitalize(key), key);
            } else if ("enum".equals(fieldData.chronicleType)) {
                hasSameStateBuilder.addStatement("if ($L != null ? $L!=o.get$L() : o.get$L() != null) return false", key, key, capitalize(key), capitalize(key));
            } else {
                if ("object".equals(fieldData.chronicleType)) {
                    //if configured object then stateEquals
                    if (vos.get(fieldData.className) == null) {
                        hasSameStateBuilder.addStatement("if ($L != null ? !$L.equals(o.get$L()) : o.get$L() != null) return false", key, key, capitalize(key), capitalize(key));
                    } else {
                        hasSameStateBuilder.addStatement("if ($L != null ? !$L.stateEquals(o.get$L()) : o.get$L() != null) return false", key, key, capitalize(key), capitalize(key));
                    }
                } else if ("sequence".equals(fieldData.chronicleType)) {
//                    if (ric != null ? !ric.equals(o.getRic()) : o.getRic() != null) return false;
//                    if (tradeDate != null ? !tradeDate.equals(o.getTradeDate()) : o.getTradeDate()!= null) return false;
//                    if (divisor!=o.getDivisor()) return false;
//                    if (security != null ? !security.stateEquals(o.getSecurity()) : o.getSecurity() != null) return false;
//                    if (basketConstituents != null ? !StateEquals.listStateEquals(basketConstituents,o.getBasketConstituents()) : o.getBasketConstituents() != null) return false;
//                    if (basketConstituents2 != null ? !StateEquals.mapStateEquals(basketConstituents2,o.getBasketConstituents2()) : o.getBasketConstituents2()!=null) return false;


                    if ("java.util.Map".equals(fieldData.className)) {
                        hasSameStateBuilder.addStatement("if ($L != null ? !$T.mapStateEquals($L,o.get$L()) : o.get$L() != null) return false", key, StateEquals.class, key, capitalize(key), capitalize(key));
                    } else {
                        hasSameStateBuilder.addStatement("if ($L != null ? !$T.listStateEquals($L,o.get$L()) : o.get$L() != null) return false", key, StateEquals.class, key, capitalize(key), capitalize(key));
                    }

                } else {
                    hasSameStateBuilder.addStatement("if ($L != null ? !$L.equals(o.get$L()) : o.get$L() != null) return false", key, key, capitalize(key), capitalize(key));
                }
            }
        }
        hasSameStateBuilder.addStatement("return true");
        return hasSameStateBuilder.build();
    }


    private MethodSpec hashCodeBuilder(ClassVo vo, Type type) {

        MethodSpec.Builder hashCodeBuilder = MethodSpec.methodBuilder("hashCode")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.INT);

        if (Type.EVENT == type) {
            hashCodeBuilder.addStatement("int result = getAggregateId() != null ? getAggregateId().hashCode() : 0");
            hashCodeBuilder.addStatement("result = 31 * result +  (getClassName() != null ? getClassName().hashCode() : 0)");
            hashCodeBuilder.addStatement("result = 31 * result +  (getAggregateClassName() != null ? getAggregateClassName().hashCode() : 0)");
        }


        int count = 0;

        for (String key : vo.instanceVariables.keySet()) {
            FieldDataVo fieldData = vo.instanceVariables.get(key);
            if (count == 0 && Type.EVENT != type) {
                if (fieldData.type.isPrimitive()) {
                    hashCodeBuilder.addStatement("int result = $L", key);
                } else {
                    hashCodeBuilder.addStatement("int result = $L != null ? $L.hashCode() : 0", key, key);
                }
            } else {
                if (fieldData.type.isPrimitive()) {
                    hashCodeBuilder.addStatement("result = 31 * result + $L", key);
                } else {
                    hashCodeBuilder.addStatement("result = 31 * result + ($L != null ? $L.hashCode() : 0)", key, key);
                }
            }
            count++;
        }

        hashCodeBuilder.addStatement("return result");
        return hashCodeBuilder.build();
    }

    private void generateBuilder(ClassVo vo, ClassName interfaceClass, Type type) throws Exception {

        String className = vo.className + "Builder";
        TypeSpec.Builder builderBuilder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC);


        ParameterizedTypeName stateEquals = ParameterizedTypeName.get(ClassName.get(StateEquals.class), interfaceClass);
        builderBuilder.addSuperinterface(stateEquals);

        ClassName threadLocalName = ClassName.get("java.lang", "ThreadLocal");
        ClassName builderName = ClassName.get(vo.packageName, className);

        ParameterizedTypeName threadLocalBuilder = ParameterizedTypeName.get(threadLocalName, builderName);

        TypeSpec anonymousThreadLocal = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(threadLocalBuilder)
                .addMethod(MethodSpec.methodBuilder("initialValue")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PROTECTED)
                        .returns(builderName)
                        .addStatement("return new $T()", builderName)
                        .build())
                .build();

        FieldSpec THREAD_LOCAL = FieldSpec.builder(threadLocalBuilder, "THREAD_LOCAL", Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("$L", anonymousThreadLocal)
                .build();
        builderBuilder.addField(THREAD_LOCAL);


//        private static final ThreadLocal<BasketBuilder> THREAD_LOCAL = new ThreadLocal<BasketBuilder>(){
//            @Override protected BasketBuilder initialValue() {
//                return new BasketBuilder();
//            }
//        };

        builderBuilder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE).build());

        MethodSpec.Builder startBuilding = MethodSpec.methodBuilder("startBuilding")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .returns(builderName);

        if (Type.EVENT == type)
            startBuilding.addParameter(String.class, "id");

        startBuilding.beginControlFlow("if (THREAD_LOCAL.get() == null)");
        startBuilding.addStatement("THREAD_LOCAL.set(new $T())", builderName);
        startBuilding.endControlFlow();
        startBuilding.addStatement("$T builder = THREAD_LOCAL.get()", builderName);

        if (Type.EVENT == type)
            startBuilding.addStatement("builder.id = id");


        ClassName journalableClass = ClassName.get(vo.packageName, vo.className + "Journalable");
        ClassName immutableClass = ClassName.get(vo.packageName, vo.className + "Immutable");
        ClassName mutableClass = ClassName.get(vo.packageName, vo.className + "Mutable");

        MethodSpec.Builder buildJournalableMethod = MethodSpec.methodBuilder("buildJournalable")
                .addModifiers(Modifier.PUBLIC)
                .returns(interfaceClass);


        buildJournalableMethod.addStatement("$T journalable =  $T.newInstanceFactory().get()", journalableClass, journalableClass);

        CodeBlock.Builder journalableSet = CodeBlock.builder().add("journalable.set(");

        String cacheName = null;

        if (vo.cacheKey != null) {
            cacheName = uncapitalize(vo.className) + "Cache";
            TypeName keyType = vo.instanceVariables.get(vo.cacheKey).type;
            TypeName cacheMap = ParameterizedTypeName.get(ClassName.get(Map.class), keyType, immutableClass);
            TypeName concreteCacheMap = ParameterizedTypeName.get(ClassName.get(HashMap.class), keyType, immutableClass);
            builderBuilder.addField(
                    FieldSpec.builder(cacheMap, cacheName).initializer("new $T()", concreteCacheMap).build()
            );
        }

//        BasketImmutable basket = immutableCache.get(ric);
//
//        if (basket == null || !this.hasSameState(basket)) {
//            basket = new BasketImmutable(ric, tradeDate, divisor, security, basketConstituents, basketConstituents2);
//            immutableCache.put(ric, basket);
//        }
//        return basket;

        MethodSpec.Builder buildImmutableMethod = MethodSpec.methodBuilder("buildImmutable")
                .addModifiers(Modifier.PUBLIC)
                .returns(interfaceClass);

        CodeBlock.Builder immutableNew = CodeBlock.builder();

        if (vo.cacheKey != null) {

            immutableNew.addStatement("$T immutable = $L.get($L)", immutableClass, cacheName, vo.cacheKey);
            immutableNew.add("if (immutable == null || !this.stateEquals(immutable)) {");
            immutableNew.add("immutable = new $T(", immutableClass);
        } else {
            immutableNew.add("$T immutable = new $T(", immutableClass, immutableClass);
        }

        MethodSpec.Builder buildMutableMethod = MethodSpec.methodBuilder("buildMutable")
                .addModifiers(Modifier.PUBLIC)
                .returns(interfaceClass);
        CodeBlock.Builder mutableNew = CodeBlock.builder().add("$T mutable = new $T(", mutableClass, mutableClass);


        MethodSpec.Builder buildCopyBuilderMethod = MethodSpec.methodBuilder("copyBuilder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(interfaceClass, uncapitalize(vo.className));
        buildCopyBuilderMethod.addStatement("$T builder = THREAD_LOCAL.get()", builderName);
        buildCopyBuilderMethod.returns(builderName);

        int noOfParams = vo.instanceVariables.keySet().size();

        if (Type.EVENT == type) {
            journalableSet.add("id$L", noOfParams == 0 ? "" : ",");
            immutableNew.add("id$L", noOfParams == 0 ? "" : ",");
        }

        int i = 1;
        if (Type.EVENT == type) {
            builderBuilder.addField(String.class, "id", Modifier.PRIVATE);
        }


        for (String key : vo.instanceVariables.keySet()) {

            FieldDataVo fieldData = getFieldData(vo, key);

            FieldSpec.Builder field = FieldSpec.builder(fieldData.type, key, Modifier.PRIVATE);

            if ("sequence".equals(fieldData.chronicleType)) {
                field.initializer(CodeBlock.builder().add("new $T()", fieldData.concreteType).build());
            }
            builderBuilder.addField(field.build());

            if (fieldData.type.isPrimitive()) {
                if (TypeName.BOOLEAN == fieldData.type)
                    startBuilding.addStatement("builder.$N = false", key);
                else
                    startBuilding.addStatement("builder.$N = 0", key);
            } else {
                if ("sequence".equals(fieldData.chronicleType)) {
                    startBuilding.addStatement("builder.$N = new $T()", key, fieldData.concreteType);
                } else {
                    startBuilding.addStatement("builder.$N = null", key);
                }
            }

            MethodSpec parameterBuilder = MethodSpec.methodBuilder(key).addParameter(fieldData.type, key)
                    .returns(builderName)
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("THREAD_LOCAL.get().$L = $L", key, key)
                    .addStatement("return THREAD_LOCAL.get()").build();

            builderBuilder.addMethod(parameterBuilder);
            buildCopyBuilderMethod.addStatement("builder.$L($L.get$L())", key, uncapitalize(vo.className), capitalize(key));

            journalableSet.add("$L$L", key, i == noOfParams ? "" : ",");
            immutableNew.add("$L$L", key, i == noOfParams ? "" : ",");
            mutableNew.add("$L$L", key, i == noOfParams ? "" : ",");

            //add setter to builder for mutable

            i++;
        }
        journalableSet.add(");");

        if (vo.cacheKey != null) {
            immutableNew.add(");");
            immutableNew.add("$L.put($L, immutable);", cacheName, vo.cacheKey);
            immutableNew.add("}");
        } else {
            immutableNew.add(");");
        }


        mutableNew.add(");");

        buildJournalableMethod.addCode(journalableSet.build());
        buildJournalableMethod.addStatement("return journalable");


        buildImmutableMethod.addCode(immutableNew.build());
        buildImmutableMethod.addStatement("return immutable");

        if (vo.updatable) {
            buildMutableMethod.addCode(mutableNew.build());
            buildMutableMethod.addStatement("return mutable");
        }

        startBuilding.addStatement("return builder");

        builderBuilder.addMethod(startBuilding.build());
        builderBuilder.addMethod(buildJournalableMethod.build());
        builderBuilder.addMethod(buildImmutableMethod.build());

        if (vo.updatable)
            builderBuilder.addMethod(buildMutableMethod.build());

        builderBuilder.addMethod(stateEqualsBuilder(vo));

        buildCopyBuilderMethod.addStatement("return builder");
        builderBuilder.addMethod(buildCopyBuilderMethod.build());

        JavaFile javaFile = JavaFile.builder(vo.packageName, builderBuilder.build())
                .build();

        javaFile.writeTo(new File(this.generatedSourceDirectory));
    }


    private static class ClassVo {
        String packageName;
        String className;
        boolean updatable;
        String cacheKey;

        String getFullClassname() {
            return packageName + "." + className;
        }

        Map<String, FieldDataVo> instanceVariables = new LinkedHashMap<>();
    }


    private class FieldDataVo {
        String className;
        TypeName type;
        TypeName concreteType;

        ClassName parameterizedClass;

        String chronicleType;
        boolean updatable;

        String keyName;
        TypeName keyType;


//        int ptPos = fullName.lastIndexOf(".");
//
//        String ptPackageName = fullName.substring(0, ptPos);
//        String ptClassName = fullName.substring(ptPos + 1);
//
//        return ClassName.get(ptPackageName, ptClassName);

    }

    private FieldDataVo getFieldData(ClassVo vo, String key) {
        FieldDataVo fieldData = vo.instanceVariables.get(key);

        return fieldData;
    }


    private ClassName classFromFullName(String fullName) {
        int ptPos = fullName.lastIndexOf(".");

        String ptPackageName = fullName.substring(0, ptPos);
        String ptClassName = fullName.substring(ptPos + 1);

        return ClassName.get(ptPackageName, ptClassName);
    }

    private String fullNameFromClass(ClassName className) {

        return className.packageName() + "." + className.simpleName();
    }


    private String chronicleType(TypeName typeName) {


        if (typeName.isPrimitive()) {
            if (TypeName.BOOLEAN == typeName)
                return "bool";
            else if (TypeName.DOUBLE == typeName)
                return "float64";
            else if (TypeName.FLOAT == typeName)
                return "float32";
            else if (TypeName.INT == typeName || TypeName.BYTE == typeName || TypeName.SHORT == typeName)
                return "int32";
            else if (TypeName.LONG == typeName)
                return "int64";
        } else {

            String className;
            if (typeName instanceof ParameterizedTypeName) {
                className = ((ParameterizedTypeName) typeName).rawType.toString();
            } else {
                className = typeName.toString();
            }


            if ("java.lang.String".equals(className))
                return "text";
            else if ("java.time.LocalDate".equals(className))
                return "date";
            else if ("java.util.List".equals(className) || "java.util.Map".equals(className))
                return "sequence";
            else {
                //check for enum -> assuming not a configured domain object

                ClassVo domain = vos.get(fullNameFromClass((ClassName) typeName));


                boolean isEnum = false;

                if (domain == null) {
                    try {
                        isEnum = Class.forName(className).isEnum();
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(className + " not found", e);
                    }
                }

                if (isEnum)
                    return "enum";

                return "object";
            }
        }


        throw new RuntimeException("Type not configured: " + typeName);
    }


    private String capitalize(String str) {

        StringBuilder sb = new StringBuilder(str.length());

        sb.append(Character.toUpperCase(str.charAt(0)));

        sb.append(str.substring(1));
        return sb.toString();

    }


    private String uncapitalize(String str) {

        StringBuilder sb = new StringBuilder(str.length());

        sb.append(Character.toLowerCase(str.charAt(0)));

        sb.append(str.substring(1));
        return sb.toString();

    }

}