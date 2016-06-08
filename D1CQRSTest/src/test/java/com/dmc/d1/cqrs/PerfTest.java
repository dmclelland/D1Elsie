package com.dmc.d1.cqrs;

import com.dmc.d1.algo.event.EventFactoryChronicle;
import com.dmc.d1.cqrs.command.CommandBus;
import com.dmc.d1.cqrs.command.SimpleCommandBus;
import com.dmc.d1.cqrs.event.SimpleEventBus;
import com.dmc.d1.cqrs.event.store.AggregateEventStore;
import com.dmc.d1.cqrs.event.store.InMemoryAggregateEventStore;
import com.dmc.d1.cqrs.test.aggregate.Aggregate1;
import com.dmc.d1.cqrs.test.command.CreateAggregate1Command;
import com.dmc.d1.cqrs.test.command.UpdateAggregate1Command;
import com.dmc.d1.cqrs.test.commandhandler.MyCommandHandler1;
import com.dmc.d1.cqrs.test.commandhandler.ReflectiveAnnotatedCommandHandlerInvoker;
import com.dmc.d1.cqrs.test.domain.MyId;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created By davidclelland on 02/06/2016.
 */

@Ignore
public class PerfTest {

    private static int REPEAT_TEST = 10;

    private static int ITERATIONS = 1_000_000;

    CommandBus commandBus;

    CommandBus commandBusWithReflectiveCommandHandler;

    AggregateEventStore aes;
    AggregateRepository<Aggregate1> repo1;


    @Before
    public void before(){
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setup() {
        aes = new InMemoryAggregateEventStore();
        SimpleEventBus eventBus = new SimpleEventBus();

        repo1 = new AggregateRepository(aes, Aggregate1.class, eventBus,  new EventFactoryChronicle());

        List<AbstractCommandHandler<? extends Aggregate>> lst = new ArrayList<>();
        lst.add(new MyCommandHandler1(repo1));


        commandBus = new SimpleCommandBus(lst);

        lst = new ArrayList<>();
        lst.add(new MyCommandHandler1(repo1,
                new ReflectiveAnnotatedCommandHandlerInvoker(MyCommandHandler1.class)));

        commandBusWithReflectiveCommandHandler = new SimpleCommandBus(lst);
    }


    @Test
    public void testCreateAndUpdateCommandDirect() {
        for (int j = 0; j < REPEAT_TEST; j++) {
            setup();
            int rnd = ((this.hashCode() ^ (int) System.nanoTime()));
            StopWatch watch = new StopWatch();
            watch.start();
            for (int i = 0; i < ITERATIONS; i++) {
                rnd = xorShift(rnd);

                MyId id = new MyId("" + rnd);

                CreateAggregate1Command command = new CreateAggregate1Command(id, rnd, rnd + 2);
                commandBus.dispatch(command);

                Aggregate1 aggregate = repo1.find(id.toString());

                assertEquals(aggregate.getI1(), rnd);
                assertEquals(aggregate.getI2(), rnd + 2);

                UpdateAggregate1Command command2 = new UpdateAggregate1Command(id, rnd - 5, rnd - 7);
                commandBus.dispatch(command2);

                aggregate = repo1.find(id.toString());

                assertEquals(aggregate.getI1(), rnd - 5);
                assertEquals(aggregate.getI2(), rnd - 7);

                //aggregate  committed ->  events should be in AggregateEventStore
                //should be 4 all together 2 for the create and 2 for the update

            }
            watch.stop();
            System.out.println("It took " + watch.getTotalTimeSeconds() + " to run");
            //assertEquals(4 * ITERATIONS, aes.getAll().size());
        }
    }


    @Test
    public void testCreateAndUpdateCommandReflection() {

        for (int j = 0; j < REPEAT_TEST; j++) {
            setup();
            int rnd = ((this.hashCode() ^ (int) System.nanoTime()));

            StopWatch watch = new StopWatch();
            watch.start();
            for (int i = 0; i < ITERATIONS; i++) {
                rnd = xorShift(rnd);

                MyId id = new MyId("" + rnd);

                CreateAggregate1Command command = new CreateAggregate1Command(id, rnd, rnd + 2);
                commandBusWithReflectiveCommandHandler.dispatch(command);

                Aggregate1 aggregate = repo1.find(id.toString());

                assertEquals(aggregate.getI1(), rnd);
                assertEquals(aggregate.getI2(), rnd + 2);

                UpdateAggregate1Command command2 = new UpdateAggregate1Command(id, rnd - 5, rnd - 7);
                commandBusWithReflectiveCommandHandler.dispatch(command2);

                aggregate = repo1.find(id.toString());

                assertEquals(aggregate.getI1(), rnd - 5);
                assertEquals(aggregate.getI2(), rnd - 7);

                //aggregate  committed ->  events should be in AggregateEventStore
                //should be 4 all together 2 for the create and 2 for the update
            }

            watch.stop();
            System.out.println("It took " + watch.getTotalTimeSeconds() + " to run reflectively");
            //assertEquals(4 * ITERATIONS, aes.getAll().size());
        }

    }

    private int xorShift(int x) {
        x ^= x << 6;
        x ^= x >>> 21;
        x ^= (x << 7);
        return x;
    }


}
