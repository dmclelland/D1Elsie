package com.dmc.d1.cqrs;

import com.dmc.d1.algo.event.Configuration;
import com.dmc.d1.cqrs.command.CommandBus;
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
import org.HdrHistogram.Histogram;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created By davidclelland on 02/06/2016.
 */

@Ignore
public class ComplexAggregatePerfTest {

    CommandBus commandBus;

    Function<String, AggregateInitialisedEvent> initialisationFactory =
            (ID) -> TestAggregateInitialisedEventBuilder.startBuilding(ID).buildJournalable();

    private static final Histogram CREATE_HISTOGRAM =
            new Histogram(TimeUnit.SECONDS.toNanos(30), 2);

    AggregateEventStore chronicleAES;
    AggregateRepository<ComplexAggregate> repo1;

    @Before
    public void before() throws Exception {
//        try {
//            Thread.sleep(15000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @After
    public void reset() {
        CREATE_HISTOGRAM.reset();
    }


    private void setup() throws Exception {


        SimpleEventBus eventBus = new SimpleEventBus();

        repo1 = new AggregateRepository(chronicleAES, ComplexAggregate.class, eventBus, ComplexAggregate.newInstanceFactory(), initialisationFactory);

        List<AbstractCommandHandler<? extends Aggregate>> lst = new ArrayList<>();

        lst.add(new ComplexCommandHandler(repo1));

        chronicleAES = new ChronicleAggregateEventStore(Configuration.getChroniclePath());
        commandBus = new SimpleCommandBus(lst);

    }


    @Test
    public void testCreateAndReplayComplexEventsNoAssertions() throws Exception {

        int noOfCreatesWarmup = 100_000;
        int noOfCreates = 100_000;

//         int noOfCreatesWarmup = 100;
//        int noOfCreates = 100;

        setup();
        int rnd = ((this.hashCode() ^ (int) System.nanoTime()));

        rnd = createAggregates(commandBus, rnd, noOfCreatesWarmup, true);
        rnd = createAggregates(commandBus, rnd, noOfCreates, false);

        Map<String, ComplexAggregate> aggregate1Repo = (Map<String, ComplexAggregate>) ReflectionTestUtils.getField(repo1, "cache");
        aggregate1Repo.clear();
        StopWatch watch = new StopWatch();
        watch.start();
        chronicleAES.replay(Collections.singletonMap(repo1.getAggregateClassName(), repo1));
        watch.stop();
        System.out.println("It took " + watch.getTotalTimeSeconds() + " to replay");
        CREATE_HISTOGRAM.getHistogramData().outputPercentileDistribution(System.out, 10d);

    }


    private int createAggregates(CommandBus commandBus, int rnd, int iterations, boolean warmup) {

        for (int i = 0; i < iterations; i++) {
            busyWaitMicros(50);
            rnd = xorShift(rnd);
            MyId id = MyId.from("" + rnd);

            long t0 = System.nanoTime();
            Basket basket = TestBasketBuilder.createBasket(rnd);
            commandBus.dispatch(new CreateComplexAggregateCommand(id, basket));

            if (!warmup)
                CREATE_HISTOGRAM.recordValue((System.nanoTime() - t0) / 1000);


        }

        return rnd;
    }


    private int xorShift(int x) {
        x ^= x << 6;
        x ^= x >>> 21;
        x ^= (x << 7);
        return x;
    }


    private static void busyWaitMicros(long micros) {
        long waitUntil = System.nanoTime() + micros * 1000L;

        while (waitUntil > System.nanoTime()) {
            ;
        }

    }

}
