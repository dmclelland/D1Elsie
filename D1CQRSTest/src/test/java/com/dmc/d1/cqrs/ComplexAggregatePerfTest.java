package com.dmc.d1.cqrs;

import com.dmc.d1.algo.event.Configuration;
import com.dmc.d1.cqrs.command.CommandBus;
import com.dmc.d1.cqrs.command.SimpleCommandBus;
import com.dmc.d1.cqrs.event.SimpleEventBus;
import com.dmc.d1.cqrs.event.store.AggregateEventStore;
import com.dmc.d1.cqrs.event.store.ChronicleAggregateEventStore;
import com.dmc.d1.cqrs.test.aggregate.ComplexAggregate;
import com.dmc.d1.cqrs.test.command.CreateComplexAggregateCommand;
import com.dmc.d1.cqrs.test.commandhandler.ComplexCommandHandler;
import com.dmc.d1.cqrs.test.domain.MyId;
import com.dmc.d1.cqrs.util.ThreadLocalObjectPool;
import com.dmc.d1.test.domain.*;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.TextWire;
import net.openhft.chronicle.wire.Wire;
import org.HdrHistogram.Histogram;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StopWatch;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created By davidclelland on 02/06/2016.
 */

@Ignore
public class ComplexAggregatePerfTest {

    CommandBus commandBus;

    AggregateEventReplayer replayer;
    InitialisationEventFactory initialisationEventFactory = Configuration.initialisationEventFactoryChronicle();


    private static final Histogram CREATE_HISTOGRAM =
            new Histogram(TimeUnit.SECONDS.toNanos(30), 2);

    AggregateEventStore chronicleAES;
    AggregateRepository<ComplexAggregate> repo1;

    @Before
    public void before() throws Exception {
        ThreadLocalObjectPool.initialise();
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

        ThreadLocalObjectPool.initialise();
        chronicleAES = new ChronicleAggregateEventStore(Configuration.getChroniclePath());

        SimpleEventBus eventBus = new SimpleEventBus();

        repo1 = new AggregateRepository(chronicleAES, ComplexAggregate.class, eventBus, new ComplexAggregate.Factory(), initialisationEventFactory);

        List<AbstractCommandHandler<? extends Aggregate>> lst = new ArrayList<>();

        lst.add(new ComplexCommandHandler(repo1));


        commandBus = new SimpleCommandBus(lst);

        List<AggregateRepository> repos = Arrays.asList(repo1);

        replayer = new AggregateEventReplayer(chronicleAES, repos);

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
        replayer.replay();
        watch.stop();
        System.out.println("It took " + watch.getTotalTimeSeconds() + " to replay");
        CREATE_HISTOGRAM.getHistogramData().outputPercentileDistribution(System.out, 10d);

    }


    private int createAggregates(CommandBus commandBus, int rnd,  int iterations, boolean warmup) {

        for (int i = 0; i < iterations; i++) {
            busyWaitMicros(50);
            rnd = xorShift(rnd);
            MyId id = MyId.from("" + rnd);

            long t0 = System.nanoTime();
            Basket basket = createBasket(rnd);
            commandBus.dispatch(new CreateComplexAggregateCommand(id, basket));

            if (!warmup)
                CREATE_HISTOGRAM.recordValue((System.nanoTime() - t0) / 1000);


        }

        return rnd;
    }


    private Basket createBasket(int rnd) {

        String ric = securities[Math.abs(rnd) % 4];

        return BasketBuilder.startBuilding()
                .tradeDate(LocalDate.now())
                .divisor(divisor(rnd))
                .ric(ric)
                .security(security(rnd))
                .basketConstituents(constituents(ric))
                .buildJournalable();
    }


    static int[] divisors = new int[4];

    static {
        divisors[0] = 50000;
        divisors[1] = 1000;
        divisors[2] = 5000;
        divisors[3] = 10000;

    }


    static String[] securities = new String[4];

    static {
        securities[0] = "X8PS.DE";
        securities[1] = "X5PS.DE";
        securities[2] = "X6PS.DE";
        securities[3] = "X7PS.DE";
    }

    String ric(int rnd) {
        return securities[Math.abs(rnd) % 4];
    }

    int divisor(int rnd) {

        return divisors[Math.abs(rnd) % 4];
    }


    Security security(int rnd) {

        return SecurityBuilder.startBuilding().name(ric(rnd)).adv20Day(12000).buildJournalable();
    }

    Random rnd = new Random();

    Map<String, List<BasketConstituent>> constituentsMap = new HashMap<>();

    private static String[] constituents = new String[100];

    {
        for (int i = 0; i < constituents.length; i++) {
            constituents[i] = "ric" + i;
        }
    }

    private List<BasketConstituent> constituents(String ric) {
        if (constituentsMap.containsKey(ric))
            return constituentsMap.get(ric);

        int rnd = this.rnd.nextInt(99) + 1;
        List<BasketConstituent> lst = new ArrayList<>();
        for (int i = 0; i < rnd; i++) {
            String constituentRic = constituents[i];
            lst.add(BasketConstituentBuilder.startBuilding().adjustedShares(i).ric(constituentRic).buildJournalable());
        }

        constituentsMap.put(ric, lst);

        return lst;

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
