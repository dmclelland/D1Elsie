package com.dmc.d1.cqrs;

import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Created By davidclelland on 14/06/2016.
 */
@Ignore
public class ReflectionVsDirectMethodComparison {

    int i = 0;

    public void a() {
        i += 1;
    }

    private static int noOfIterations = 1_000_000_000;

    @Test
    public void directCalcTest() {
        for (int i = 0; i < noOfIterations; i++) {
            a();
        }
    }

    @Test
    public void reflectiveCalcTest() throws Exception {


        Method f = this.getClass().getMethod("a");
        for (int i = 0; i < noOfIterations; i++) {
            f.invoke(this);
        }
    }


}
