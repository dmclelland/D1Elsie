package com.dmc.annotation;

/**
 * Created By davidclelland on 12/07/2016.
 */
class StatsLoggingProcessor extends Processor {

    private ProcessingChain chain;

    StatsLoggingProcessor(ProcessingChain chain){
        this.chain = chain;
    }

    @Override
    protected void doProcess(Request request) {
        long t0 = System.nanoTime();
        chain.process(request);
        System.out.println("It took " + (System.nanoTime() - t0) + " to process the chain " + chain.toString());

    }
}
