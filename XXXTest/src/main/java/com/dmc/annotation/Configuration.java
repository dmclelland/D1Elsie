package com.dmc.annotation;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created By davidclelland on 12/07/2016.
 */
@org.springframework.context.annotation.Configuration
public class Configuration implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @StatsLoggable(enabled = true)
    @Stoppable(enabled = true)
    ConcreteProcessor1 processor1WithStatsLogging = new ConcreteProcessor1();

    @Stoppable(enabled = true)
    ConcreteProcessor2 processor2WithStopping = new ConcreteProcessor2();

    @Bean
    @Qualifier("chain1")
    ProcessingChain chain1() {

        ProcessingChain chain = new ProcessingChain();

        chain.addProcessor(processor1WithStatsLogging);
        chain.addProcessor(processor2WithStopping);
        chain.addProcessor(new ConcreteProcessor3());

        return chain;
    }


    @Bean
    @StatsLoggable(enabled = true)
    @Qualifier("chain2")
    ProcessingChain chain2() {
        ConcreteProcessor1 processor1A = new ConcreteProcessor1();
        ConcreteProcessor2 processor2A = new ConcreteProcessor2();
        ConcreteProcessor3 processor3A = new ConcreteProcessor3();

        ProcessingChain chain = new ProcessingChain();

        chain.addProcessors(processor1A, processor2A, processor3A);
        return chain;
    }


    @PostConstruct
    public void injectConcerns() throws Exception {

        Reflections reflections = new Reflections("com.dmc.annotation", new FieldAnnotationsScanner(), new MethodAnnotationsScanner());

        Set<Field> statsLoggableFields = reflections.getFieldsAnnotatedWith(StatsLoggable.class);
        Set<Field> stoppableFields = reflections.getFieldsAnnotatedWith(Stoppable.class);


        Set<Object> statsLoggableFieldProcessors = new HashSet<>();
        for (Field field : statsLoggableFields) {
            Object obj = field.get(this);
            statsLoggableFieldProcessors.add(obj);
        }

        Set<Method> statsLoggableMethod = reflections.getMethodsAnnotatedWith(StatsLoggable.class);
        Set<Method> stoppableMethod = reflections.getMethodsAnnotatedWith(Stoppable.class);

        Map<String, ProcessingChain> beans = applicationContext.getBeansOfType(ProcessingChain.class);

        for (Map.Entry<String, ProcessingChain> entry : beans.entrySet()) {
            //if chain is configured with stats loggable then apply

            for (Object statsLoggableProcessor : statsLoggableFieldProcessors) {
                if (entry.getValue().containsProcessor((Processor) statsLoggableProcessor)) {
                    System.out.println(entry.getKey() + " contains stats loggable processor " + statsLoggableProcessor);
                }

            }
            //check if entry has the fields
        }
        //1 get all method annotations
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
