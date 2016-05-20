package com.dmc.d1.cqrs;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by davidclelland on 16/05/2016.
 */
public final class Utils {

    public static Iterable<Method>  methodsOf(Class<?> clazz){
        List<Method> methods = new LinkedList<>();
        Class<?> currentClazz = clazz;
        do{
            methods.addAll(Arrays.asList(currentClazz.getDeclaredMethods()));
            currentClazz = currentClazz.getSuperclass();
        }while(currentClazz!=null);

        return Collections.unmodifiableList(methods);
    }

}
