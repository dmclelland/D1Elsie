package com.dmc.d1.cqrs;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Created By davidclelland on 14/06/2016.
 */
@Ignore
public class ClassNameAccessComparison {


    private static int noOfIterations = 100_000_000;

    static class TestClass {
        static String CLASS_NAME = TestClass.class.getName();

        private String getClassNam() {
            return CLASS_NAME;
        }
    }

    @Test
    public void directAccess() {
        TestClass test = new TestClass();

        for (int i = 0; i < noOfIterations; i++) {
            test.getClassNam();
        }
    }

    @Test
    public void reflectiveCalcTest() throws Exception {
        TestClass test = new TestClass();
        for (int i = 0; i < noOfIterations; i++) {
            test.getClass().getName();
        }
    }


}
