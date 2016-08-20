package com.dmc.d1.cqrs;

import com.dmc.d1.algo.event.Configuration;
import com.dmc.d1.cqrs.command.CommandBus;
import com.dmc.d1.cqrs.command.SimpleCommandBus;
import com.dmc.d1.cqrs.event.AggregateInitialisedEvent;
import com.dmc.d1.cqrs.event.SimpleEventBus;
import com.dmc.d1.cqrs.event.store.AggregateEventStore;
import com.dmc.d1.cqrs.event.store.ChronicleAggregateEventStore;
import com.dmc.d1.cqrs.sample.aggregate.Aggregate1;
import com.dmc.d1.cqrs.sample.command.CreateAggregate1Command;
import com.dmc.d1.cqrs.sample.command.UpdateAggregate1Command;
import com.dmc.d1.cqrs.sample.commandhandler.MyCommandHandler1;
import com.dmc.d1.cqrs.sample.commandhandler.ReflectiveAnnotatedCommandHandlerInvoker;
import com.dmc.d1.cqrs.sample.domain.MyId;
import com.dmc.d1.sample.event.TestAggregateInitialisedEventBuilder;
import org.HdrHistogram.Histogram;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

/**
 * Created By davidclelland on 02/06/2016.
 */

@Ignore
public class PerfTest2 {

    private static int REPEAT_TEST = 1;

    private static int ITERATIONS = 10_000;

    CommandBus commandBus;

    CommandBus commandBusWithReflectiveCommandHandler;

    AggregateEventStore aes;
    AggregateRepository<Aggregate1> repo1;

    private static final Histogram CREATE_HISTOGRAM =
            new Histogram(TimeUnit.SECONDS.toNanos(30), 2);

    private static final Histogram UPDATE_HISTOGRAM =
            new Histogram(TimeUnit.SECONDS.toNanos(30), 2);

    @Before
    public void before() {
        sleep();
    }

    private void sleep() {
//        try {
//            Thread.sleep(30000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    Function<String, AggregateInitialisedEvent> initialisationFactory =
            (ID) -> TestAggregateInitialisedEventBuilder.startBuilding(ID).buildJournalable();

    private void setup() throws Exception {
        SimpleEventBus eventBus = new SimpleEventBus();

        repo1 = new AggregateRepository(aes, Aggregate1.class, eventBus, Aggregate1.newInstanceFactory(), initialisationFactory);


        aes = new ChronicleAggregateEventStore(Configuration.getChroniclePath());


        List<AbstractCommandHandler<? extends Aggregate>> lst = new ArrayList<>();
        lst.add(new MyCommandHandler1(repo1));

        commandBus = new SimpleCommandBus(lst);

        lst = new ArrayList<>();
        lst.add(new MyCommandHandler1(repo1,
                new ReflectiveAnnotatedCommandHandlerInvoker(MyCommandHandler1.class)));

        commandBusWithReflectiveCommandHandler = new SimpleCommandBus(lst);
    }


    @Test
    public void testCreateAndUpdateCommandDirect() throws Exception {
        for (int j = 0; j < REPEAT_TEST; j++) {
            setup();
            int rnd = ((this.hashCode() ^ (int) System.nanoTime()));
            StopWatch watch = new StopWatch();
            watch.start();

            MyId id = MyId.from("" + rnd);
            String aggregateIdentifier = id.asString();

            CreateAggregate1Command command = new CreateAggregate1Command(id, rnd, rnd + 2);
            commandBus.dispatch(command);

            Aggregate1 aggregate = repo1.find(aggregateIdentifier);

            assertEquals(rnd, aggregate.getI1());
            assertEquals(rnd + 2, aggregate.getI2());


            for (int i = 0; i < ITERATIONS; i++) {
                rnd = xorShift(rnd);

                UpdateAggregate1Command command2 = new UpdateAggregate1Command(id, rnd - 5, rnd - 7);
                commandBus.dispatch(command2);

                aggregate = repo1.find(aggregateIdentifier);

                assertEquals(rnd - 5, aggregate.getI1());
                assertEquals(rnd - 7, aggregate.getI2());


            }
            watch.stop();
            System.out.println("It took " + watch.getTotalTimeSeconds() + " to run");
            //assertEquals(3 + 2 * ITERATIONS, aes.getAll().size());
        }
        sleep();
    }


    @Test
    public void testCreateAndUpdateCommandReflection() throws Exception {

        for (int j = 0; j < REPEAT_TEST; j++) {
            setup();
            int rnd = ((this.hashCode() ^ (int) System.nanoTime()));

            StopWatch watch = new StopWatch();
            watch.start();

            rnd = xorShift(rnd);

            MyId id = MyId.from("" + rnd);

            String aggregateIdentifier = id.asString();

            CreateAggregate1Command command = new CreateAggregate1Command(id, rnd, rnd + 2);
            commandBusWithReflectiveCommandHandler.dispatch(command);

            Aggregate1 aggregate = repo1.find(aggregateIdentifier);

            assertEquals(aggregate.getI1(), rnd);
            assertEquals(aggregate.getI2(), rnd + 2);


            for (int i = 0; i < ITERATIONS; i++) {

                UpdateAggregate1Command command2 = new UpdateAggregate1Command(id, rnd - 5, rnd - 7);
                commandBusWithReflectiveCommandHandler.dispatch(command2);

                aggregate = repo1.find(aggregateIdentifier);

                assertEquals(aggregate.getI1(), rnd - 5);
                assertEquals(aggregate.getI2(), rnd - 7);

                //aggregate  committed ->  events should be in AggregateEventStore
                //should be 4 all together 2 for the create and 2 for the update
            }

            watch.stop();
            System.out.println("It took " + watch.getTotalTimeSeconds() + " to run reflectively");
            //assertEquals(3 + 2 * ITERATIONS, aes.getAll().size());


        }

        sleep();

    }

    private int xorShift(int x) {
        x ^= x << 6;
        x ^= x >>> 21;
        x ^= (x << 7);
        return x;
    }


}
