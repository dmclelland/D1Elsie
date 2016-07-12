package com.dmc.annotation;

/**
 * Created By davidclelland on 12/07/2016.
 */
public class Utils {

    static void simulateWork(long micros) {
        long waitUntil = System.nanoTime() + micros * 1000L;

        while (waitUntil > System.nanoTime()) {
            ;
        }

    }

}
