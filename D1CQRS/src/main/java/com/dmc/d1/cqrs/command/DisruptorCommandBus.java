package com.dmc.d1.cqrs.command;

import com.dmc.d1.cqrs.AbstractCommandHandler;
import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.util.ThreadLocalObjectPool;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class DisruptorCommandBus<T extends AbstractCommandHandler<? extends Aggregate>> implements CommandBus {


    private final Disruptor<CommandHolder> disruptor;


    public DisruptorCommandBus(SimpleCommandBus<T> simpleCommandBus) {
        this(simpleCommandBus, Collections.emptyList());
    }



    public DisruptorCommandBus(SimpleCommandBus<T> simpleCommandBus, List<EventHandler<CommandHolder>> additionalHandlers) {


        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 2 << 15;

        // Construct the Disruptor
        this.disruptor = new Disruptor(new CommandFactory(), bufferSize, DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI, new BusySpinWaitStrategy());


        // Connect the handler
        EventHandlerGroup<CommandHolder> group =  disruptor.handleEventsWith(new DisruptorCommandHandler(simpleCommandBus));

        additionalHandlers.forEach(h -> group.then(h));

        // Start the Disruptor, starts all threads running
        disruptor.start();

    }

    private static class DisruptorCommandHandler<T extends AbstractCommandHandler<? extends Aggregate>> implements EventHandler<CommandHolder> {

        private final SimpleCommandBus<T> simpleCommandBus;

        private DisruptorCommandHandler(SimpleCommandBus<T> simpleCommandBus) {
            this.simpleCommandBus = checkNotNull(simpleCommandBus);
        }

        @Override
        public void onEvent(CommandHolder holder, long l, boolean b) throws Exception {
            simpleCommandBus.dispatch(holder.command);
        }
    }

    public static class CommandHolder {
        private Command command;

        void set(Command command) {
            this.command = command;
        }

        public Command getCommand(){
            return command;
        }

    }

    private static class CommandFactory implements EventFactory<CommandHolder> {

        @Override
        public CommandHolder newInstance() {
            return new CommandHolder();
        }
    }


    @Override
    public void dispatch(Command command) {
        RingBuffer<CommandHolder> ringBuffer = disruptor.getRingBuffer();

        long sequence = ringBuffer.next();  // Grab the next sequence
        try {
            CommandHolder holder = ringBuffer.get(sequence); // Get the entry in the Disruptor
            // for the sequence
            holder.set(command);  // Fill with data
        } finally {
            ringBuffer.publish(sequence);
        }
    }
}