package com.dmc.annotation;

/**
 * Created By davidclelland on 12/07/2016.
 */
class ConcreteProcessor1 extends Processor {

    @Override
    protected void doProcess(Request request) {
        Utils.simulateWork(50);

        System.out.println(request.getName() + " processed by " + this.getClass().getSimpleName());
    }
}
