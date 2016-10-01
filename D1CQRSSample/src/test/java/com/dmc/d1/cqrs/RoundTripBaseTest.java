package com.dmc.d1.cqrs;

import com.dmc.d1.algo.event.EmptyEvent;
import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.command.CommandBus;
import com.dmc.d1.cqrs.command.DisruptorCommandBus;
import com.dmc.d1.cqrs.command.SimpleCommandBus;
import com.lmax.disruptor.*;
import org.HdrHistogram.Histogram;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;


/**
 * Created By davidclelland on 02/06/2016.
 */

public abstract class RoundTripBaseTest {

    static ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private final RingBuffer<EmptyEvent> exchangeBuffer =
            createSingleProducer(EmptyEvent.EVENT_FACTORY, bufferSize(), new BlockingWaitStrategy());

    private Pinger pinger;
    private BatchEventProcessor<EmptyEvent> pingProcessor;

    private Ponger ponger;

    private CommandBus commandBus;

    protected volatile Supplier<Command> commandBuilder;

    public void setup() throws Exception {

        this.ponger = new Ponger(exchangeBuffer);
        this.commandBus = new DisruptorCommandBus(new SimpleCommandBus(getCommandHandlers()),
                Arrays.asList(ponger));

        this.pinger = new Pinger(commandBus, pauseNanos());
        this.pingProcessor =
                new BatchEventProcessor<>(exchangeBuffer, exchangeBuffer.newBarrier(), pinger);
    }

    protected abstract List<AbstractCommandHandler<? extends Aggregate>> getCommandHandlers();

    protected abstract int senderThreadPoolSize();

    protected abstract int bufferSize();

    protected abstract long pauseNanos();

    protected void startSending(int noOfCommands) throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        final CyclicBarrier barrier = new CyclicBarrier(2);

        pinger.reset(barrier, latch, noOfCommands);

        EXECUTOR.submit(pingProcessor);

        barrier.await();
        latch.await();

        pingProcessor.halt();
    }

    protected class Pinger implements EventHandler<EmptyEvent>, LifecycleAware {

        private final long pauseTimeNs;

        private CyclicBarrier barrier;
        private CountDownLatch latch;

        private final Histogram HISTOGRAM =
                new Histogram(TimeUnit.SECONDS.toNanos(1), 1);

        private volatile long maxEvents;
        private volatile long t0;

        private final CommandBus commandBus;

        private final ExecutorService senderExecutor;

        private final boolean multiThreaded;

        public Pinger(final CommandBus commandBus,final long pauseTimeNs) {
            this.pauseTimeNs = pauseTimeNs;
            this.commandBus = commandBus;
            this.senderExecutor = Executors.newFixedThreadPool(senderThreadPoolSize());
            this.multiThreaded = senderThreadPoolSize() > 1;
        }

        @Override
        public void onEvent(final EmptyEvent event, final long sequence, final boolean endOfBatch) throws Exception {
            final long t1 = System.nanoTime();

            //only store after warm up
            if (sequence > maxEvents) {
                try {
                    //System.out.println((t1-t0)/1000);
                    HISTOGRAM.recordValueWithExpectedInterval(t1 - t0, pauseTimeNs);
                }catch(Exception e){
                    System.out.println("Value " + (t1-t0) + " not recorded:" + e.getMessage());
                }
            }

            //1 already sent in onStart
            if (sequence < (maxEvents * 2)-1) {
                while (pauseTimeNs > (System.nanoTime() - t1)) {
                    Thread.yield();
                }
                send();
            } else {
                latch.countDown();
                HISTOGRAM.getHistogramData().outputPercentileDistribution(System.out, 1, 1000.0);
            }
        }


        void send() {
            if (multiThreaded) {
                senderExecutor.submit(() -> {
                    t0 = System.nanoTime();
                    commandBus.dispatch(RoundTripBaseTest.this.commandBuilder.get());
                });
            } else {
                t0 = System.nanoTime();
                commandBus.dispatch(RoundTripBaseTest.this.commandBuilder.get());
            }
        }


        @Override
        public void onStart() {
            try {
                barrier.await();
                Thread.sleep(1000);
                send();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onShutdown() {
        }

        public void reset(final CyclicBarrier barrier, final CountDownLatch latch, int maxEvents) {
            HISTOGRAM.reset();
            this.maxEvents+=maxEvents;
            this.barrier = barrier;
            this.latch = latch;
        }
    }

    //when disruptor command has completed handling he command this handler subsequently notifies the
    //pinger that it is
    // .
    protected class Ponger implements EventHandler<DisruptorCommandBus.CommandHolder> {
        private final RingBuffer<EmptyEvent> exchangeBuffer;

        public Ponger(final RingBuffer<EmptyEvent> exchangeBuffer) {
            this.exchangeBuffer = exchangeBuffer;
        }

        @Override
        public void onEvent(final DisruptorCommandBus.CommandHolder event,
                            final long sequence, final boolean endOfBatch) throws Exception {

            exchangeBuffer.publish(exchangeBuffer.next());
        }
    }
}
