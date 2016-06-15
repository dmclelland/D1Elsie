package com.dmc.d1.cqrs;

import com.dmc.d1.algo.event.Configuration;
import com.dmc.d1.cqrs.command.CommandBus;
import com.dmc.d1.cqrs.command.SimpleCommandBus;
import com.dmc.d1.cqrs.event.EventFactory;
import com.dmc.d1.cqrs.event.SimpleEventBus;
import com.dmc.d1.cqrs.event.store.AggregateEventStore;
import com.dmc.d1.cqrs.event.store.ChronicleAggregateEventStore;
import com.dmc.d1.cqrs.test.aggregate.Aggregate1;
import com.dmc.d1.cqrs.test.aggregate.Aggregate2;
import com.dmc.d1.cqrs.test.aggregate.AggregateFactoryImpl;
import com.dmc.d1.cqrs.test.command.*;
import com.dmc.d1.cqrs.test.commandhandler.MyCommandHandler1;
import com.dmc.d1.cqrs.test.commandhandler.MyCommandHandler2;
import com.dmc.d1.cqrs.test.commandhandler.ReflectiveAnnotatedCommandHandlerInvoker;
import com.dmc.d1.cqrs.test.domain.MyId;
import com.dmc.d1.cqrs.util.InstanceAllocator;
import org.HdrHistogram.Histogram;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Created By davidclelland on 02/06/2016.
 */

//TODO migrate performance tests to JMH

@Ignore
public class PerfTest {

    private static int REPEAT_TEST = 1;

    EventFactory basicEventFactory = Configuration.getEventFactoryBasic();
    EventFactory chronicleEventFactory = Configuration.getEventFactoryChronicle();
    InstanceAllocator instanceAllocator = Configuration.getInstanceAllocatorChronicle();

    CommandBus commandBus;

    CommandBus commandBusWithReflectiveCommandHandler;

    AggregateEventStore chronicleAES;
    AggregateRepository<Aggregate1> repo1;
    AggregateRepository<Aggregate2> repo2;

    AggregateFactory aggregateFactory = new AggregateFactoryImpl();


    private static final Histogram CREATE_HISTOGRAM =
            new Histogram(TimeUnit.SECONDS.toNanos(30), 2);

    private static final Histogram UPDATE_HISTOGRAM =
            new Histogram(TimeUnit.SECONDS.toNanos(30), 2);

    @Before
    public void before() {
//        try {
//            Thread.sleep(15000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @After
    public void reset() {
        CREATE_HISTOGRAM.reset();
        UPDATE_HISTOGRAM.reset();
    }

    CommandFactoryPooled pooled = new CommandFactoryPooled();

    private void setup() throws Exception {
        chronicleAES = new ChronicleAggregateEventStore(instanceAllocator, Configuration.getChroniclePath());

        SimpleEventBus eventBus = new SimpleEventBus();

        repo1 = new AggregateRepository(chronicleAES, Aggregate1.class, eventBus, chronicleEventFactory, aggregateFactory);
        repo2 = new AggregateRepository(chronicleAES, Aggregate2.class, eventBus, chronicleEventFactory, aggregateFactory);

        List<AbstractCommandHandler<? extends Aggregate>> lst = new ArrayList<>();

        lst.add(new MyCommandHandler1(repo1));
        lst.add(new MyCommandHandler2(repo2));

        commandBus = new SimpleCommandBus(lst);

        lst = new ArrayList<>();

        lst.add(new MyCommandHandler1(repo1,
                new ReflectiveAnnotatedCommandHandlerInvoker(MyCommandHandler1.class)));
        lst.add(new MyCommandHandler2(repo2,
                new ReflectiveAnnotatedCommandHandlerInvoker(MyCommandHandler2.class)));

        commandBusWithReflectiveCommandHandler = new SimpleCommandBus(lst);
    }


    @Test
    public void testCreateAndUpdateCommandDirect() throws Exception {
        for (int j = 0; j < REPEAT_TEST; j++) {
            setup();
            testCreateAndUpdateCommand(this.commandBus);
        }
    }


    @Test
    public void testCreateAndUpdateCommandReflection() throws Exception {

        for (int j = 0; j < REPEAT_TEST; j++) {
            setup();
            testCreateAndUpdateCommand(this.commandBusWithReflectiveCommandHandler);
        }

    }



    private void testCreateAndUpdateCommand(CommandBus commandBus) throws Exception {

            int rnd = ((this.hashCode() ^ (int) System.nanoTime()));
            StopWatch watch = new StopWatch();
            watch.start();
            List<MyId> ids = new ArrayList<>();
            List<Aggregate> aggregates = new ArrayList<>();
            rnd = createAggregates(commandBus, rnd, ids, aggregates, 1000, true);
            rnd = createAggregates(commandBus, rnd, ids, aggregates, 25000, false);

            rnd = updateAggregates(commandBus, rnd, ids, 1000, true);
            rnd = updateAggregates(commandBus, rnd, ids, 750000, false);
            watch.stop();
            System.out.println("It took " + watch.getTotalTimeSeconds() + " to run");
            //assertEquals(4 * ITERATIONS, aes.getAll().size());

            CREATE_HISTOGRAM.getHistogramData().outputPercentileDistribution(System.out, 10d);
            UPDATE_HISTOGRAM.getHistogramData().outputPercentileDistribution(System.out, 10d);


        System.out.println("EVENTS: " + chronicleAES.getAll().size());

    }



    private int updateAggregates(CommandBus commandBus, int rnd, List<MyId> ids, int iterations, boolean warmup) {
        Random randomSelect = new Random();
        //send a whole bunch of updates

        int noOfIds = ids.size();
        for (int i = 0; i < iterations; i++) {
            busyWaitMicros(20);

            MyId id = ids.get(randomSelect.nextInt(noOfIds));
            rnd = xorShift(rnd);

            int idAsInt = Integer.parseInt(id.asString());

            if (idAsInt % 2 == 0) {
                UpdateAggregate1Command command = pooled.createUpdateAggregate1Command(id, rnd-5, rnd-7);

                long t0 = System.nanoTime();
                commandBus.dispatch(command);
                if (!warmup)
                    UPDATE_HISTOGRAM.recordValue((System.nanoTime() - t0) / 1000);
                Aggregate1 aggregate = repo1.find(id.asString());

                assertEquals(aggregate.getI1(), rnd - 5);
                assertEquals(aggregate.getI2(), rnd - 7);
            } else if (Math.abs(idAsInt % 2) == 1) {
                UpdateAggregate2Command command = pooled.createUpdateAggregate2Command(id, "" + (rnd - 5), "" + (rnd - 7));

                long t0 = System.nanoTime();
                commandBus.dispatch(command);
                if (!warmup)
                    UPDATE_HISTOGRAM.recordValue((System.nanoTime() - t0) / 1000);
                Aggregate2 aggregate = repo2.find(id.asString());

                assertEquals(aggregate.getS1(), "" + (rnd - 5));
                assertEquals(aggregate.getS2(), "" + (rnd - 7));
            }
            //aggregate  committed ->  events should be in AggregateEventStore
            //should be 4 all together 2 for the create and 2 for the update
        }

        return rnd;
    }

    private int createAggregates(CommandBus commandBus, int rnd, List<MyId> ids, List<Aggregate> aggregates, int iterations, boolean warmup) {
        //create aggregates
        for (int i = 0; i < iterations; i++) {
            busyWaitMicros(20);
            rnd = xorShift(rnd);
            MyId id = MyId.from("" + rnd);
            ids.add(id);

            Aggregate aggregate = null;
            if (rnd % 2 == 0) {
                long t0 = System.nanoTime();
                CreateAggregate1Command command = pooled.createCreateAggregate1Command(id, rnd, rnd+2);
                commandBus.dispatch(command);

                if (!warmup)
                    CREATE_HISTOGRAM.recordValue((System.nanoTime() - t0) / 1000);
                aggregate = repo1.find(id.asString());
            } else if (Math.abs(rnd % 2) == 1) {
                long t0 = System.nanoTime();

                CreateAggregate2Command command = pooled.createCreateAggregate2Command(id, "" + rnd, "" + (rnd + 2));
                commandBus.dispatch(command);
                if (!warmup)
                    CREATE_HISTOGRAM.recordValue((System.nanoTime() - t0) / 1000);
                aggregate = repo2.find(id.asString());
            }
            aggregates.add(aggregate);
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
