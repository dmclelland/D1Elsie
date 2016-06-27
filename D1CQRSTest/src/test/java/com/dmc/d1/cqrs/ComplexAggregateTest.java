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
public class ComplexAggregateTest {

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
    public void testBytesMarshallable() {

        //ClassAliasPool.CLASS_ALIASES.addAlias(SecurityChronicle.class);
        //ClassAliasPool.CLASS_ALIASES.addAlias(BasketConstituentChronicle.class);
        Wire wire = new TextWire(Bytes.elasticByteBuffer());
        Basket basket = createBasket(111);
        ((Marshallable) basket).writeMarshallable(wire);

        System.out.println(wire);


        Basket basket2 = BasketBuilder.startBuilding()
                .security(SecurityBuilder.startBuilding().buildJournalable()).buildJournalable();
        ((Marshallable) basket2).readMarshallable(wire);

        assertEquals(basket.getDivisor(), basket2.getDivisor());
        assertEquals(basket.getSecurity().getName(), basket2.getSecurity().getName());


    }


    @Test
    public void testCreateAndReplayComplexEvents() throws Exception {

        int noOfCreatesWarmup = 100_000;
        int noOfCreates = 100_000;

//         int noOfCreatesWarmup = 100;
//        int noOfCreates = 100;

        setup();
        int rnd = ((this.hashCode() ^ (int) System.nanoTime()));

        List<MyId> ids = new ArrayList<>();
        List<Aggregate> aggregates = new ArrayList<>();

        rnd = createAggregates(commandBus, rnd, ids, aggregates, noOfCreatesWarmup, true);
        rnd = createAggregates(commandBus, rnd, ids, aggregates, noOfCreates, false);

        Map<String, ComplexAggregate> aggregate1Repo = (Map<String, ComplexAggregate>) ReflectionTestUtils.getField(repo1, "cache");
        Map<String, ComplexAggregate> aggregate1RepoCopy = new HashMap<>(aggregate1Repo);
        aggregate1Repo.clear();
        StopWatch watch = new StopWatch();
        watch.start();
        replayer.replay();
        watch.stop();
        System.out.println("It took " + watch.getTotalTimeSeconds() + " to replay");

        assertEquals(aggregate1RepoCopy.size(), aggregate1Repo.size());


        for (String key : aggregate1Repo.keySet()) {
            ComplexAggregate agg = aggregate1Repo.get(key);
            ComplexAggregate aggExpected = aggregate1RepoCopy.get(key);

            assertEquals(aggExpected.getId(), agg.getId());
            assertEquals(aggExpected.getBasket().getDivisor(), agg.getBasket().getDivisor());
            assertEquals(aggExpected.getBasket().getRic(), agg.getBasket().getRic());
            assertEquals(aggExpected.getBasket().getSecurity().getName(), agg.getBasket().getSecurity().getName());
            assertEquals(aggExpected.getBasket().getSecurity().getAdv20Day(), agg.getBasket().getSecurity().getAdv20Day());

            assertTrue(agg.getBasket().getBasketConstituents().size() > 0);

            assertEquals(aggExpected.getBasket().getBasketConstituents().size(), agg.getBasket().getBasketConstituents().size());

            for (int i = 0; i < aggExpected.getBasket().getBasketConstituents().size(); i++) {
                assertEquals(aggExpected.getBasket().getBasketConstituents().get(i).getRic(),
                        agg.getBasket().getBasketConstituents().get(i).getRic());

                assertEquals(aggExpected.getBasket().getBasketConstituents().get(i).getAdjustedShares(),
                        agg.getBasket().getBasketConstituents().get(i).getAdjustedShares());

            }
        }

        CREATE_HISTOGRAM.getHistogramData().outputPercentileDistribution(System.out, 10d);

    }

    @Test
    public void testCreateAndReplayComplexEventsNoAssertions() throws Exception {

        int noOfCreatesWarmup = 100_000;
        int noOfCreates = 100_000;

//         int noOfCreatesWarmup = 100;
//        int noOfCreates = 100;

        setup();
        int rnd = ((this.hashCode() ^ (int) System.nanoTime()));

        List<MyId> ids = new ArrayList<>();
        List<Aggregate> aggregates = new ArrayList<>();

        rnd = createAggregates(commandBus, rnd, ids, aggregates, noOfCreatesWarmup, true);
        rnd = createAggregates(commandBus, rnd, ids, aggregates, noOfCreates, false);

        Map<String, ComplexAggregate> aggregate1Repo = (Map<String, ComplexAggregate>) ReflectionTestUtils.getField(repo1, "cache");
        aggregate1Repo.clear();
        StopWatch watch = new StopWatch();
        watch.start();
        replayer.replay();
        watch.stop();
        System.out.println("It took " + watch.getTotalTimeSeconds() + " to replay");
        CREATE_HISTOGRAM.getHistogramData().outputPercentileDistribution(System.out, 10d);

    }



    private int createAggregates(CommandBus commandBus, int rnd, List<MyId> ids, List<Aggregate> aggregates, int iterations, boolean warmup) {

        for (int i = 0; i < iterations; i++) {
            busyWaitMicros(50);
            rnd = xorShift(rnd);
            MyId id = MyId.from("" + rnd);
            ids.add(id);


            long t0 = System.nanoTime();
            Basket basket = createBasket(rnd);
            commandBus.dispatch(new CreateComplexAggregateCommand(id, basket));

            if (!warmup)
                CREATE_HISTOGRAM.recordValue((System.nanoTime() - t0) / 1000);

            Aggregate aggregate = repo1.find(id.asString());

            aggregates.add(aggregate);


        }

        return rnd;
    }

    private Basket createBasket(int rnd) {

        String ric = ric(rnd);
        return BasketBuilder.startBuilding()
                .tradeDate(LocalDate.now())
                .divisor(divisor(rnd))
                .ric(ric)
                .security(security(rnd))
                .basketConstituents(new ArrayList(constituents(ric)))
                .buildJournalable();
    }


    int divisor(int rnd) {

        if (rnd % 4 == 0)
            return 50000;
        else if (rnd % 4 == 3)
            return 10000;
        else if (rnd % 4 == 2)
            return 5000;
        else if (rnd % 4 == 1)
            return 1000;
        else
            return 100;
    }


    String ric(int rnd) {

        if (rnd % 4 == 0)
            return "X8PS.DE";
        else if (rnd % 4 == 3)
            return "X7PS.DE";
        else if (rnd % 4 == 2)
            return "X6PS.DE";
        else if (rnd % 4 == 1)
            return "X5PS.DE";
        else
            return "X4PS.DE";

    }


    Security security(int rnd) {
        String name;

        if (rnd % 4 == 0)
            name = "X8PS.DE";
        else if (rnd % 4 == 3)
            name = "X7PS.DE";
        else if (rnd % 4 == 2)
            name = "X6PS.DE";
        else if (rnd % 4 == 1)
            name = "X5PS.DE";
        else
            name = "X4PS.DE";


        return SecurityBuilder.startBuilding().name(name).adv20Day(12000).buildJournalable();
    }

    Random rnd = new Random();

    Map<String, List<BasketConstituent>> constituentsMap = new HashMap<>();

    private List<BasketConstituent> constituents(String ric) {
        if (constituentsMap.containsKey(ric))
            return constituentsMap.get(ric);

        int rnd = this.rnd.nextInt(99) + 1;
        List<BasketConstituent> lst = new ArrayList<>();
        for (int i = 0; i < rnd; i++) {
            String constituentRic = ("ric" + i).intern();
            lst.add(BasketConstituentBuilder.startBuilding().adjustedShares(i).ric(constituentRic).buildPooledJournalable());
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
