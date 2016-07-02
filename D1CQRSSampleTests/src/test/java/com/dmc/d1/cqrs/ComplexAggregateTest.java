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
import com.dmc.d1.cqrs.util.ThreadLocalObjectPool;
import com.dmc.d1.test.domain.Basket;
import com.dmc.d1.test.event.TestAggregateInitialisedEventBuilder;
import com.lmax.disruptor.*;
import org.HdrHistogram.Histogram;
import org.junit.After;
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

    AggregateEventReplayer replayer;

    Function<String, AggregateInitialisedEvent> initialisationFactory =
            (ID) -> TestAggregateInitialisedEventBuilder.startBuilding(ID).buildJournalable();

    private static final Histogram HISTOGRAM =
            new Histogram(TimeUnit.SECONDS.toNanos(30), 2);

    AggregateEventStore chronicleAES;
    AggregateRepository<ComplexAggregate> repo1;

    @After
    public void reset() {
        HISTOGRAM.reset();
    }

    static ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final int SENDER_THREAD_POOL_SIZE = 4;
    private static final int BUFFER_SIZE = 1024;
    private static final long ITERATIONS = 100;//_000;
    private static final long PAUSE_NANOS = 1000L;

    private final RingBuffer<EmptyEvent> exchangeBuffer =
            createSingleProducer(EmptyEvent.EVENT_FACTORY, BUFFER_SIZE, new BlockingWaitStrategy());


    private Pinger pinger;
    private BatchEventProcessor<EmptyEvent> pingProcessor;

    private Ponger ponger;

    @Before
    public void setup() throws Exception {

        //force initialisation of thread pool
        ThreadLocalObjectPool.clear();

        chronicleAES = new ChronicleAggregateEventStore(Configuration.getChroniclePath());

        SimpleEventBus eventBus = new SimpleEventBus();


        repo1 = new AggregateRepository(chronicleAES, ComplexAggregate.class, eventBus,
                ComplexAggregate.newInstanceFactory(), initialisationFactory);

        List<AbstractCommandHandler<? extends Aggregate>> lst = new ArrayList<>();

        lst.add(new ComplexCommandHandler(repo1));

        this.ponger = new Ponger(exchangeBuffer);
        commandBus = new DisruptorCommandBus(new SimpleCommandBus(lst),
                Arrays.asList(ponger));

        List<AggregateRepository> repos = Arrays.asList(repo1);

        replayer = new AggregateEventReplayer(chronicleAES, repos);

        this.pinger = new Pinger(commandBus, ITERATIONS, PAUSE_NANOS, false);

        this.pingProcessor =
                new BatchEventProcessor<>(exchangeBuffer, exchangeBuffer.newBarrier(), pinger);
    }


    @Test
    public void testCreateAndReplayComplexEvents() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        final CyclicBarrier barrier = new CyclicBarrier(2);
        pinger.reset(barrier, latch, HISTOGRAM);

        EXECUTOR.submit(pingProcessor);

        barrier.await();
        latch.await();

        pingProcessor.halt();

        replayAndCompare();

        HISTOGRAM.getHistogramData().outputPercentileDistribution(System.out, 10d);

    }

    private void replayAndCompare() {
        Map<String, ComplexAggregate> aggregate1Repo = (Map<String, ComplexAggregate>) ReflectionTestUtils.getField(repo1, "cache");
        Map<String, ComplexAggregate> aggregate1RepoCopy = new HashMap<>(aggregate1Repo);
        aggregate1Repo.clear();
        StopWatch watch = new StopWatch();
        watch.start();
        replayer.replay();
        watch.stop();
        System.out.println("It took " + watch.getTotalTimeSeconds() + " to replay");

        assertEquals(aggregate1RepoCopy.size(), aggregate1Repo.size());

        checkAssertions(aggregate1Repo, aggregate1RepoCopy);
    }


    private void checkAssertions(Map<String, ComplexAggregate> aggregate1Repo, Map<String, ComplexAggregate> aggregate1RepoCopy) {
        for (String key : aggregate1Repo.keySet()) {
            ComplexAggregate agg = aggregate1Repo.get(key);
            ComplexAggregate aggExpected = aggregate1RepoCopy.get(key);
            assertNotSame(aggExpected, agg);
            assertEquals(aggExpected.getId(), agg.getId());

            assertTrue(aggExpected.getBasket().getDivisor() > 0);
            assertEquals(aggExpected.getBasket().getDivisor(), agg.getBasket().getDivisor());

            assertTrue(aggExpected.getBasket().getRic().length() > 0);
            assertEquals(aggExpected.getBasket().getRic(), agg.getBasket().getRic());

            assertTrue(aggExpected.getBasket().getSecurity().getName().length() > 0);
            assertEquals(aggExpected.getBasket().getSecurity().getName(), agg.getBasket().getSecurity().getName());
            assertEquals(aggExpected.getBasket().getSecurity().getAdv20Day(), agg.getBasket().getSecurity().getAdv20Day());

            assertTrue(aggExpected.getBasket().getSecurity().getAssetType()!=null);
            assertEquals(aggExpected.getBasket().getSecurity().getAssetType(), agg.getBasket().getSecurity().getAssetType());


            assertTrue(agg.getBasket().getBasketConstituents().size() > 0);

            assertEquals(aggExpected.getBasket().getBasketConstituents().size(), agg.getBasket().getBasketConstituents().size());

            for (int i = 0; i < aggExpected.getBasket().getBasketConstituents().size(); i++) {

                assertTrue(aggExpected.getBasket().getBasketConstituents().get(i).getRic().length() > 0);
                assertEquals(aggExpected.getBasket().getBasketConstituents().get(i).getRic(),
                        agg.getBasket().getBasketConstituents().get(i).getRic());

                assertTrue(aggExpected.getBasket().getBasketConstituents().get(i).getAdjustedShares() > 0);
                assertEquals(aggExpected.getBasket().getBasketConstituents().get(i).getAdjustedShares(),
                        agg.getBasket().getBasketConstituents().get(i).getAdjustedShares());

            }
        }
    }

    private static class Pinger implements EventHandler<EmptyEvent>, LifecycleAware {
        private final long maxEvents;
        private final long pauseTimeNs;

        private CyclicBarrier barrier;
        private CountDownLatch latch;
        private Histogram histogram;
        private volatile long t0;

        int rnd = ((this.hashCode() ^ (int) System.nanoTime()));
        private final CommandBus commandBus;

        private final boolean multiThreaded;
        private final ExecutorService   senderExecutor = Executors.newFixedThreadPool(SENDER_THREAD_POOL_SIZE);;


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

            if (sequence > maxEvents)
                histogram.recordValueWithExpectedInterval((t1 - t0) / 1000, pauseTimeNs);

            if (sequence < maxEvents * 2) {
                while (pauseTimeNs > (System.nanoTime() - t1)) {
                    Thread.yield();
                }
                send();
            } else {
                latch.countDown();
            }
        }

        void send() {

            rnd = xorShift(rnd);
            MyId id = MyId.from("" + rnd);
            Basket basket = TestBasketBuilder.createBasket(rnd);

            Command command = new CreateComplexAggregateCommand(id, basket);


            if(multiThreaded){
                senderExecutor.submit(()->{
                    t0 = System.nanoTime();
                    commandBus.dispatch(command);});
            }else {
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

        public void reset(final CyclicBarrier barrier, final CountDownLatch latch, final Histogram histogram) {
            this.histogram = histogram;
            this.barrier = barrier;
            this.latch = latch;
        }
    }

    //when disruptor command has completed handling he command this handler subsequently notifies the
    //pinger that it is done.
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
