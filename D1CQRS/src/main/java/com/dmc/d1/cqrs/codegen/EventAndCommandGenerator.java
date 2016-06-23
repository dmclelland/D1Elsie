package com.dmc.d1.cqrs.codegen;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.event.AggregateEventAbstract;
import com.dmc.d1.cqrs.event.AggregateInitialisedEvent;
import com.dmc.d1.cqrs.event.ChronicleAggregateEvent;
import com.dmc.d1.cqrs.util.NewInstanceFactory;
import com.dmc.d1.cqrs.util.Poolable;
import com.dmc.d1.cqrs.util.Resettable;
import com.dmc.d1.cqrs.util.ThreadLocalObjectPool;
import com.squareup.javapoet.*;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
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

    private Map<String, ClassVo> vos = new HashMap<>();

    public void generate() throws Exception {
        URL fileName = getClass().getClassLoader().getResource("CodeGen.xml");

        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(fileName);

        Element root = doc.getRootElement();


        //TODO apply ordering
        for (Element elem : root.getChild("domain").getChildren()) {
            ClassVo vo = parseClassElem(elem);

            ClassName interfaceName = generateInterface(vo, Type.DOMAIN);
            generateImmutableClass(vo, interfaceName, Type.DOMAIN);
            generateChronicleClass(vo, interfaceName, Type.DOMAIN);
            generateBuilder(vo, interfaceName, Type.DOMAIN);
            vos.put(vo.getFullClassname(), vo);
        }

        //create chronicle initialised event
        for (Element elem : root.getChild("event").getChildren()) {
            ClassVo vo = parseClassElem(elem);

            ClassName interfaceName = generateInterface(vo, Type.EVENT);
            generateImmutableClass(vo, interfaceName, Type.EVENT);
            generateChronicleClass(vo, interfaceName, Type.EVENT);
            generateBuilder(vo, interfaceName, Type.EVENT);
            vos.put(vo.getFullClassname(), vo);
        }

        for (Element elem : root.getChild("command").getChildren()) {
//            ClassVo vo = parseClassElem(elem);
//
//            ClassName interfaceName = generateInterface(vo);
//
//            generateImmutableClass(vo, interfaceName);
//            generateChronicleClass(vo, interfaceName, Command.class);
//            generateBuilder(vo, interfaceName);
        }
    }


    private ClassVo parseClassElem(Element element) throws Exception {


        ClassVo vo = new ClassVo();
        String fullClass = element.getAttributeValue("name");

        if (element.getAttribute("initialisationEvent") != null && "true".equals(element.getAttributeValue("initialisationEvent"))) {
            vo.initialisationEvent = true;
        }

        int pos = fullClass.lastIndexOf(".");

        vo.packageName = fullClass.substring(0, pos);
        vo.className = fullClass.substring(pos + 1);

        if (element.getAttribute("updatable") != null && "true".equals(element.getAttributeValue("updatable"))) {
            vo.updatable = true;
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

        ClassName interfaceClass =  ClassName.get((vo.packageName), interfaceName);

        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(interfaceName)
                .addModifiers(Modifier.PUBLIC);

        if (Type.EVENT == type) {
            if (vo.initialisationEvent)
                interfaceBuilder.addSuperinterface(AggregateInitialisedEvent.class);
            else
                interfaceBuilder.addSuperinterface(AggregateEvent.class);
        }
        for (String key : vo.instanceVariables.keySet()) {

            FieldDataVo fieldData = getFieldData(vo, key);

            interfaceBuilder.addMethod(
                    MethodSpec.methodBuilder("get" + capitalize(key))
                            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                            .returns(fieldData.type)
                            .build()
            );
        }


        interfaceBuilder.addMethod(  MethodSpec.methodBuilder("deepClone")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(interfaceClass)
                .build());

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

            constructorBuilder.addParameter(fieldData.type, key)
                    .addStatement("this.$N = $N", key, key);

            eventBuilder.addMethod(
                    MethodSpec.methodBuilder("get" + capitalize(key))
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(Override.class)
                            .addStatement("return $N", key)
                            .returns(fieldData.type)
                            .build()
            );
        }

        eventBuilder.addMethod(constructorBuilder.build());

        eventBuilder.addMethod(deepCloneBuilder(vo, immutableClass, type));
        eventBuilder.addMethod(equalsBuilder(vo, immutableClass, type));
        eventBuilder.addMethod(hashCodeBuilder(vo, type));

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
                .addMethod(newInstanceFactoryMethod);


        if (Type.EVENT == type) {
            eventBuilder.superclass(ChronicleAggregateEvent.class);
        } else if (Type.DOMAIN == type) {
            eventBuilder.addSuperinterface(Resettable.class);
            eventBuilder.addSuperinterface(Marshallable.class);
            eventBuilder.addSuperinterface(Poolable.class);
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
                        readMarshallableMethod.addCode("$T constituent = v.object($T.class);", parameterizedType, parameterizedType);
                        readMarshallableMethod.addCode("l.put(constituent.get$L(), constituent);}", capitalize(fieldData.keyName));
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
                } else if ("object".equals(fieldData.chronicleType)) {
                    readMarshallableMethod.addStatement("wireIn.read(()-> $S).object($T.class,this, (o,b) -> o.$L = b)", key, fieldData.type, key);
                    writeMarshallableMethod.addStatement("wireOut.write(()-> $S).object($T.class, this.$L)", key, fieldData.type, key);
                } else {
                    readMarshallableMethod.addStatement("wireIn.read(()-> $S).$L(this, (o, b) -> o.$L = b)", key, fieldData.chronicleType, key);
                    writeMarshallableMethod.addStatement("wireOut.write(()-> $S).$L($L)", key, fieldData.chronicleType, key);
                }
            }

            FieldSpec.Builder field = FieldSpec.builder(fieldData.type, key, Modifier.PRIVATE);
            if ("sequence".equals(fieldData.chronicleType)) {
                field.initializer(CodeBlock.builder().add("new $T()", fieldData.concreteType).build());
            }

            eventBuilder.addField(field.build());


            setBuilder.addParameter(fieldData.type, key)
                    .addStatement("this.$N = $N", key, key);

            if (Type.EVENT == type)
                resetBuilder.addStatement("setAggregateId(null)");

            if (fieldData.type.isPrimitive()) {
                if (TypeName.BOOLEAN == fieldData.type)
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
                            .returns(fieldData.type)
                            .build()
            );
        }

        eventBuilder.addMethod(readMarshallableMethod.build());
        eventBuilder.addMethod(writeMarshallableMethod.build());

        eventBuilder.addMethod(setBuilder.build());
        eventBuilder.addMethod(resetBuilder.build());
        eventBuilder.addMethod(deepCloneBuilder(vo, chronicleClass, type));
        eventBuilder.addMethod(equalsBuilder(vo, chronicleClass, type));
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

        if(Type.EVENT == type){

            equalsBuilder.addStatement("if (getAggregateId() != null ? !getAggregateId().equals(that.getAggregateId()) : that.getAggregateId() != null) return false");
            equalsBuilder.addStatement("if (getClassName() != null ? !getClassName().equals(that.getClassName()) : that.getClassName() != null) return false");
            equalsBuilder.addStatement("if (getAggregateClassName() != null ? !getAggregateClassName().equals(that.getAggregateClassName()) : that.getAggregateClassName() != null) return false");
        }


        for (String key : vo.instanceVariables.keySet()) {
            FieldDataVo fieldData = vo.instanceVariables.get(key);
            if (fieldData.type.isPrimitive()) {
                equalsBuilder.addStatement("if ($L!=that.$L) return false", key, key);
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


    private MethodSpec hashCodeBuilder(ClassVo vo, Type type) {

        MethodSpec.Builder hashCodeBuilder = MethodSpec.methodBuilder("hashCode")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.INT);

        if(Type.EVENT == type){
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


    private MethodSpec deepCloneBuilder(ClassVo vo, ClassName className, Type type) {

        MethodSpec.Builder deepCloneBuilder = MethodSpec.methodBuilder("deepClone")
                .addModifiers(Modifier.PUBLIC)
                .returns(className);

        //if immutable then just return this
        if(className.toString().endsWith("Immutable")){
            deepCloneBuilder.addStatement("return this");
            return deepCloneBuilder.build();
        }

        CodeBlock.Builder cloneSet = CodeBlock.builder().add("clone.set(");



        int i = 1;
        int noOfParams = vo.instanceVariables.size();

        if (Type.EVENT == type)
            cloneSet.add("getAggregateId()$L",noOfParams==0 ? "" : ",");


        for (String key : vo.instanceVariables.keySet()) {
            FieldDataVo fieldData = vo.instanceVariables.get(key);
            if ("sequence".equals(fieldData.chronicleType)) {
                String colName = "col" + i;

                deepCloneBuilder.addStatement("$T $L = new $T<>()", fieldData.type, colName, fieldData.concreteType);
                if ("java.util.Map".equals(fieldData.className)) {
                    deepCloneBuilder.beginControlFlow("for($T entry : this.$L.values())", fieldData.parameterizedClass, key);
                    deepCloneBuilder.addStatement("$L.put(entry.get$L(),entry.deepClone())", colName, capitalize(fieldData.keyName));
                    deepCloneBuilder.endControlFlow();
                } else {
                    deepCloneBuilder.beginControlFlow("for($T entry : this.$L)", fieldData.parameterizedClass, key);
                    deepCloneBuilder.addStatement("$L.add(entry.deepClone())", colName);
                    deepCloneBuilder.endControlFlow();
                }

                key = colName;
            }

            cloneSet.add("$L$L", key, i == noOfParams ? "" : ",");
            i++;
        }

        cloneSet.add(");");

        deepCloneBuilder.addStatement("$T clone = new $T()", className, className);

        deepCloneBuilder.addCode(cloneSet.build());
        deepCloneBuilder.addStatement("return clone");


//
//        BasketChronicle deepClone() {
//            //make deep copy of security
//            //make deep copy of basket
//            Security security = SecurityBuilder.copyBuilder(this.security).buildJournalable();
//            List<BasketConstituent> constituents = new ArrayList<>();
//            for (BasketConstituent constituent : this.basketConstituents) {
//                constituents.add(BasketConstituentBuilder.copyBuilder(constituent).buildJournalable());
//            }
//            BasketChronicle clone = new BasketChronicle();
//            clone.set(ric, tradeDate, divisor, security, constituents, new HashMap<>());
//
//            return clone;
//        }


        return deepCloneBuilder.build();
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


        ClassName chronicleClass = ClassName.get(vo.packageName, vo.className + "Chronicle");
        ClassName immutableClass = ClassName.get(vo.packageName, vo.className + "Immutable");

        MethodSpec.Builder buildJournalableMethod = MethodSpec.methodBuilder("buildJournalable")
                .addModifiers(Modifier.PUBLIC)
                .returns(vo.initialisationEvent ? ClassName.get(AggregateInitialisedEvent.class) : interfaceClass);


        MethodSpec.Builder buildPooledJournalableMethod = MethodSpec.methodBuilder("buildPooledJournalable")
                .addModifiers(Modifier.PUBLIC)
                .returns(vo.initialisationEvent ? ClassName.get(AggregateInitialisedEvent.class) : interfaceClass);


        buildJournalableMethod.addStatement("$T chronicle =  $T.newInstanceFactory().newInstance()", chronicleClass, chronicleClass);
        buildPooledJournalableMethod.addStatement("$T chronicle = $T.allocateObject($T.CLASS_NAME)", chronicleClass, ThreadLocalObjectPool.class, chronicleClass);


        CodeBlock.Builder chronicleSet = CodeBlock.builder().add("chronicle.set(");


        MethodSpec.Builder buildImmutableMethod = MethodSpec.methodBuilder("buildImmutable")
                .addModifiers(Modifier.PUBLIC)
                .returns(vo.initialisationEvent ? ClassName.get(AggregateInitialisedEvent.class) : interfaceClass);
        CodeBlock.Builder immutableNew = CodeBlock.builder().add("$T immutable = new $T(", immutableClass, immutableClass);


        MethodSpec.Builder buildCopyBuilderMethod = MethodSpec.methodBuilder("copyBuilder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(interfaceClass, uncapitalize(vo.className));
        buildCopyBuilderMethod.addStatement("$T builder = THREAD_LOCAL.get()", builderName);
        buildCopyBuilderMethod.returns(builderName);

        int noOfParams = vo.instanceVariables.keySet().size();


        if (Type.EVENT == type) {
            chronicleSet.add("id$L", noOfParams == 0 ? "" : ",");
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


//            if ("sequence".equals(fieldData.chronicleType)) {
//
//                ClassName argClassName = fieldData.parameterizedClass;
//
//                String prefix = "java.util.Map".equals(fieldData.className) ? "addOrReplace" : "add";
//
//                MethodSpec.Builder addToCollectionBuilder = MethodSpec.methodBuilder(prefix + argClassName.simpleName()).addParameter(argClassName, "entry")
//                        .returns(builderName)
//                        .addModifiers(Modifier.PUBLIC);
//
//
//                if ("java.util.Map".equals(fieldData.className)) {
//                    addToCollectionBuilder.addStatement("THREAD_LOCAL.get().$L.put(entry.get$L(), entry)", key, capitalize(fieldData.keyName));
//                } else {
//                    addToCollectionBuilder.addStatement("THREAD_LOCAL.get().$L.add(entry)", key);
//                }
//                addToCollectionBuilder.addStatement("return THREAD_LOCAL.get()");
//                builderBuilder.addMethod(addToCollectionBuilder.build());
//
//            }

//            if ("sequence".equals(fieldData.chronicleType)) {
//                ClassName argClassName = fieldData.parameterizedClass;
//
//                ClassName nestedBuilder = ClassName.get(argClassName.packageName(), argClassName.simpleName() + "Builder");
//
//
//                buildCopyBuilderMethod.addStatement("$T $L = new $T<>($L.get$Ts().size());", fieldData.type, key, fieldData.concreteType, uncapitalize(vo.className), argClassName);
//
//                buildCopyBuilderMethod.beginControlFlow("for($T var: $L.get$Ts())", argClassName, uncapitalize(vo.className), argClassName);
//
//
//                String parameterizedClass = fullNameFromClass(fieldData.parameterizedClass);
//                //boolean updatable = vos.get(parameterizedClass).updatable;
//                //TODO if the collection is not updatable then just pass a copy of the collection
////                if ("java.util.Map".equals(fieldData.className)) {
////
////
////                    // if the parameterized class is not updatable then just pass the reference
////                    if (updatable) {
////                        buildCopyBuilderMethod.addStatement("$L.put(var.get$L(), $T.copyBuilder(var).buildJournalable(pooled))", key, capitalize(fieldData.keyName), nestedBuilder);
////                    } else {
////                        buildCopyBuilderMethod.addStatement("$L.put(var.get$L(), var)", key, capitalize(fieldData.keyName));
////
////                    }
////
////                } else {
////                    //simply copy reference if not pooled and not updatable
////                    if (updatable) {
////                        buildCopyBuilderMethod.addStatement("$L.add($T.copyBuilder(var).buildJournalable(pooled))", key, nestedBuilder);
////                    } else {
////                        buildCopyBuilderMethod.addStatement("$L.add(var)", key);
////                    }
////                }
//
//                buildCopyBuilderMethod.endControlFlow();
//                buildCopyBuilderMethod.addStatement("builder.$L($L)", key, key);
//
////                List<BasketConstituent> basketConstituents = new ArrayList<>(basket.getBasketConstituents().size());
////                for (BasketConstituent constituent : basket.getBasketConstituents()) {
////                    basketConstituents.add(BasketConstituentBuilder.copyBuilder(var).buildJournalable(pooled));
////                }
//
//            } else if ("object".equals(fieldData.chronicleType)) {
//                ClassName argClassName = (ClassName) fieldData.type;
//                ClassName nestedBuilder = ClassName.get(argClassName.packageName(), argClassName.simpleName() + "Builder");
//
//                buildCopyBuilderMethod.addStatement("builder.$L($T.copyBuilder($L.get$T()).buildJournalable(pooled))", key, nestedBuilder, uncapitalize(vo.className), fieldData.type);
//                //builder.security(SecurityBuilder.copyBuilder(basket.getSecurity()).buildJournalable(pooled));
//            } else {
//                //builder.divisor(basket.getDivisor());
//                buildCopyBuilderMethod.addStatement("builder.$L($L.get$L())", key, uncapitalize(vo.className), capitalize(key));
//            }

            chronicleSet.add("$L$L", key, i == noOfParams ? "" : ",");
            immutableNew.add("$L$L", key, i == noOfParams ? "" : ",");
            i++;
        }
        chronicleSet.add(");");
        immutableNew.add(");");

        buildJournalableMethod.addCode(chronicleSet.build());
        buildJournalableMethod.addStatement("return chronicle");

        buildPooledJournalableMethod.addCode(chronicleSet.build());
        buildPooledJournalableMethod.addStatement("return chronicle");

        buildImmutableMethod.addCode(immutableNew.build());
        buildImmutableMethod.addStatement("return immutable");

        startBuilding.addStatement("return builder");

        builderBuilder.addMethod(startBuilding.build());
        builderBuilder.addMethod(buildJournalableMethod.build());
        builderBuilder.addMethod(buildPooledJournalableMethod.build());
        builderBuilder.addMethod(buildImmutableMethod.build());

        buildCopyBuilderMethod.addStatement("return builder");
        builderBuilder.addMethod(buildCopyBuilderMethod.build());

        JavaFile javaFile = JavaFile.builder(vo.packageName, builderBuilder.build())
                .build();

        javaFile.writeTo(new File(this.generatedSourceDirectory));
    }


    private static class ClassVo {
        String packageName;
        String className;
        boolean initialisationEvent;
        boolean updatable;

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
            else
                return "object";
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