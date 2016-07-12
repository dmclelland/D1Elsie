package com.dmc.annotation;

import java.util.LinkedList;

/**
 * Created By davidclelland on 12/07/2016.
 */
public class ProcessingChain {

    private LinkedList<Processor> chain = new LinkedList<>();

    public void addProcessor(Processor processor) {
        chain.add(processor);
    }

    public void addProcessors(Processor processor, Processor... additional) {
        chain.add(processor);
        for (Processor p : additional) chain.add(p);
    }

    public void process(Request request) {

        for (Processor processor : chain) {
            processor.doProcess(request);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        chain.forEach(p -> {
            builder.append(p.getClass().getSimpleName()).append(",");
        });

        //remove final ,
        builder.deleteCharAt(builder.length()-1);

        return builder.toString();
    }

    boolean containsProcessor(Processor processor) {

        for (Processor chainProcessor : chain) {
            if (chainProcessor == processor)
                return true;

        }
        return false;
    }

}
