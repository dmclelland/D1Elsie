package com.dmc.d1.cqrs;

import com.dmc.d1.algo.event.Configuration;
import com.dmc.d1.algo.event.EmptyEvent;
import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.command.CommandBus;
import com.dmc.d1.cqrs.command.DisruptorCommandBus;
import com.dmc.d1.cqrs.command.SimpleCommandBus;
import com.dmc.d1.cqrs.event.AggregateInitialisedEvent;
import com.dmc.d1.cqrs.event.SimpleEventBus;
import com.dmc.d1.cqrs.event.store.AggregateEventStore;
import com.dmc.d1.cqrs.event.store.ChronicleAggregateEventStore;
import com.dmc.d1.cqrs.test.aggregate.ComplexAggregate;
import com.dmc.d1.cqrs.test.command.CreateComplexAggregateCommand;
import com.dmc.d1.cqrs.test.commandhandler.ComplexCommandHandler;
import com.dmc.d1.cqrs.test.domain.MyId;
import com.dmc.d1.test.domain.Basket;
import com.dmc.d1.test.event.TestAggregateInitialisedEventBuilder;
import com.lmax.disruptor.*;
import org.HdrHistogram.Histogram;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;


/**
 * Created By davidclelland on 02/06/2016.
 */

@Ignore
public class ComplexAggregateTest {

    CommandBus commandBus;

    Function<String, AggregateInitialisedEvent> initialisationFactory =
            (ID) -> TestAggregateInitialisedEventBuilder.startBuilding(ID).buildJournalable();


    AggregateEventStore chronicleAES;
    AggregateRepository<ComplexAggregate> repo1;


    static ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final int SENDER_THREAD_POOL_SIZE = 4;
    private static final int BUFFER_SIZE = 1024;
    private static final long ITERATIONS = 20;
    private static final long PAUSE_NANOS = 1000;

    private final RingBuffer<EmptyEvent> exchangeBuffer =
            createSingleProducer(EmptyEvent.EVENT_FACTORY, BUFFER_SIZE, new BlockingWaitStrategy());


    private Pinger pinger;
    private BatchEventProcessor<EmptyEvent> pingProcessor;

    private Ponger ponger;

    @Before
    public void setup() throws Exception {

        SimpleEventBus eventBus = new SimpleEventBus();
        chronicleAES = new ChronicleAggregateEventStore(Configuration.getChroniclePath());

        repo1 = new AggregateRepository(chronicleAES, ComplexAggregate.class, eventBus,
                ComplexAggregate.newInstanceFactory(), initialisationFactory);

        List<AbstractCommandHandler<? extends Aggregate>> lst = new ArrayList<>();
        lst.add(new ComplexCommandHandler(repo1));

        this.ponger = new Ponger(exchangeBuffer);
        commandBus = new DisruptorCommandBus(new SimpleCommandBus(lst),
                Arrays.asList(ponger));

        this.pinger = new Pinger(commandBus, ITERATIONS, PAUSE_NANOS, false);

        this.pingProcessor =
                new BatchEventProcessor<>(exchangeBuffer, exchangeBuffer.newBarrier(), pinger);
    }


    @Test
    public void testCreateAndReplayComplexEvents() throws Exception {

        long t0 = System.currentTimeMillis();

        final CountDownLatch latch = new CountDownLatch(1);
        final CyclicBarrier barrier = new CyclicBarrier(2);
        pinger.reset(barrier, latch);

        EXECUTOR.submit(pingProcessor);

        barrier.await();
        latch.await();


        pingProcessor.halt();
        System.out.println("It took " + (System.currentTimeMillis() - t0) + " to process " + ITERATIONS * 2 + " commands");
        replayAndCompare();

    }

    private void replayAndCompare() {
        Map<String, ComplexAggregate> aggregate1Repo = (Map<String, ComplexAggregate>) ReflectionTestUtils.getField(repo1, "cache");
        Map<String, ComplexAggregate> aggregate1RepoCopy = new HashMap<>(aggregate1Repo);
        aggregate1Repo.clear();
        StopWatch watch = new StopWatch();
        watch.start();
        chronicleAES.replay(Collections.singletonMap(repo1.getAggregateClassName(), repo1));
        watch.stop();

        assertEquals(aggregate1RepoCopy.size(), aggregate1Repo.size());
        //checkAssertions(aggregate1Repo, aggregate1RepoCopy);
    }


    private void checkAssertions(Map<String, ComplexAggregate> aggregate1Repo, Map<String, ComplexAggregate> aggregate1RepoCopy) {
        for (String key : aggregate1Repo.keySet()) {
            ComplexAggregate agg = aggregate1Repo.get(key);
            ComplexAggregate aggExpected = aggregate1RepoCopy.get(key);
            assertNotSame(aggExpected, agg);
            assertEquals(aggExpected.getId(), agg.getId());

            Basket expectedBasket = aggExpected.getBasket();
            Basket actualBasket = agg.getBasket();

            assertTrue(expectedBasket.getDivisor() > 0);
            assertEquals(expectedBasket.getDivisor(), actualBasket.getDivisor());

            assertTrue(expectedBasket.getRic().length() > 0);
            assertEquals(expectedBasket.getRic(), actualBasket.getRic());

            assertTrue(expectedBasket.getSecurity().getName().length() > 0);
            assertEquals(expectedBasket.getSecurity().getName(), actualBasket.getSecurity().getName());
            assertEquals(expectedBasket.getSecurity().getAdv20Day(), actualBasket.getSecurity().getAdv20Day());

            assertTrue(expectedBasket.getSecurity().getAssetType() != null);
            assertEquals(expectedBasket.getSecurity().getAssetType(), actualBasket.getSecurity().getAssetType());

            assertTrue(actualBasket.getBasketConstituents().size() > 0);
            assertEquals(expectedBasket.getBasketConstituents().size(), actualBasket.getBasketConstituents().size());


            for (int i = 0; i < expectedBasket.getBasketConstituents().size(); i++) {

                assertTrue(expectedBasket.getBasketConstituents().get(i).getRic().length() > 0);
                assertEquals(expectedBasket.getBasketConstituents().get(i).getRic(),
                        actualBasket.getBasketConstituents().get(i).getRic());

                assertTrue(expectedBasket.getBasketConstituents().get(i).getAdjustedShares() > 0);
                assertEquals(expectedBasket.getBasketConstituents().get(i).getAdjustedShares(),
                        actualBasket.getBasketConstituents().get(i).getAdjustedShares());

            }
        }
    }

    private static class Pinger implements EventHandler<EmptyEvent>, LifecycleAware {
        private final long maxEvents;
        private final long pauseTimeNs;

        private CyclicBarrier barrier;
        private CountDownLatch latch;

        private static final Histogram HISTOGRAM =
                new Histogram(TimeUnit.SECONDS.toNanos(1), 1);

        private volatile long t0;

        int rnd = ((this.hashCode() ^ (int) System.nanoTime()));
        private final CommandBus commandBus;

        private final boolean multiThreaded;
        private final ExecutorService senderExecutor = Executors.newFixedThreadPool(SENDER_THREAD_POOL_SIZE);


        public Pinger(final CommandBus commandBus, final long maxEvents, final long pauseTimeNs, boolean multiThreaded) {
            //this.buffer = buffer;
            this.maxEvents = maxEvents;
            this.pauseTimeNs = pauseTimeNs;
            this.commandBus = commandBus;
            this.multiThreaded = multiThreaded;

        }


        @Override
        public void onEvent(final EmptyEvent event, final long sequence, final boolean endOfBatch) throws Exception {
            final long t1 = System.nanoTime();

            //only store after warm up
            if (sequence > maxEvents) {
                HISTOGRAM.recordValueWithExpectedInterval(t1 - t0, pauseTimeNs);

            }

            if (sequence < maxEvents * 2) {
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

            rnd = xorShift(rnd);
            MyId id = MyId.from("" + rnd);
            Basket basket = TestBasketBuilder.createBasket(rnd);

            Command command = new CreateComplexAggregateCommand(id, basket);


            if (multiThreaded) {
                senderExecutor.submit(() -> {
                    t0 = System.nanoTime();
                    commandBus.dispatch(command);
                });
            } else {
                t0 = System.nanoTime();
                commandBus.dispatch(command);
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

        public void reset(final CyclicBarrier barrier, final CountDownLatch latch) {
            HISTOGRAM.reset();
            this.barrier = barrier;
            this.latch = latch;
        }
    }

    //when disruptor command has completed handling he command this handler subsequently notifies the
    //pinger that it is
    // .
    private static class Ponger implements EventHandler<DisruptorCommandBus.CommandHolder> {
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

    private static int xorShift(int x) {
        x ^= x << 6;
        x ^= x >>> 21;
        x ^= (x << 7);
        return x;
    }
}
