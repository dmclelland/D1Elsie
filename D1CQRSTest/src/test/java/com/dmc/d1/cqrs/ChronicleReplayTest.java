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
import com.dmc.d1.cqrs.test.command.CreateAggregate1Command;
import com.dmc.d1.cqrs.test.command.CreateAggregate2Command;
import com.dmc.d1.cqrs.test.command.UpdateAggregate1Command;
import com.dmc.d1.cqrs.test.command.UpdateAggregate2Command;
import com.dmc.d1.cqrs.test.commandhandler.MyCommandHandler1;
import com.dmc.d1.cqrs.test.commandhandler.MyCommandHandler2;
import com.dmc.d1.cqrs.test.domain.MyId;
import com.dmc.d1.cqrs.util.InstanceAllocator;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StopWatch;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created By davidclelland on 02/06/2016.
 */

@Ignore
public class ChronicleReplayTest {


    EventFactory chronicleEventFactory = Configuration.getEventFactoryChronicle();
    InstanceAllocator instanceAllocator = Configuration.getInstanceAllocatorChronicle();

    CommandBus commandBus;

    AggregateEventReplayer replayer;


    AggregateEventStore chronicleAES;
    AggregateRepository<Aggregate1> repo1;
    AggregateRepository<Aggregate2> repo2;

    AggregateFactory aggregateFactory = new AggregateFactoryImpl();


    private void setup() throws Exception {
        chronicleAES = new ChronicleAggregateEventStore(instanceAllocator, Configuration.getChroniclePath());

        SimpleEventBus eventBus = new SimpleEventBus();

        repo1 = new AggregateRepository(chronicleAES, Aggregate1.class, eventBus, chronicleEventFactory, aggregateFactory);
        repo2 = new AggregateRepository(chronicleAES, Aggregate2.class, eventBus, chronicleEventFactory, aggregateFactory);

        List<AbstractCommandHandler<? extends Aggregate>> lst = new ArrayList<>();

        lst.add(new MyCommandHandler1(repo1));
        lst.add(new MyCommandHandler2(repo2));

        commandBus = new SimpleCommandBus(lst);

        List<AggregateRepository> repos = Arrays.asList(repo1, repo2);

        replayer = new AggregateEventReplayer(chronicleAES,repos);

    }


    @Test
    public void testReplayEvents() throws Exception {
        int noOfCreates = 10000;
        int noOfUpdates = 100000;
        setup();
        int rnd = ((this.hashCode() ^ (int) System.nanoTime()));

        List<MyId> ids = new ArrayList<>();
        List<Aggregate> aggregates = new ArrayList<>();
        rnd = createAggregates(commandBus, rnd, ids, aggregates, noOfCreates);
        rnd = updateAggregates(commandBus, rnd, ids, noOfUpdates);

        Map<String,Aggregate1> aggregate1Repo = (Map<String,Aggregate1>)ReflectionTestUtils.getField(repo1, "cache");
        Map<String,Aggregate2> aggregate2Repo = (Map<String,Aggregate2>)ReflectionTestUtils.getField(repo2, "cache");

        Map<String,Aggregate1> aggregate1RepoCopy = new HashMap<>(aggregate1Repo);
        Map<String,Aggregate2> aggregate2RepoCopy = new HashMap<>(aggregate2Repo);

        aggregate1Repo.clear();
        aggregate2Repo.clear();

        StopWatch watch = new StopWatch();
        watch.start();
        replayer.replay();
        watch.stop();
        System.out.println("It took " + watch.getTotalTimeSeconds() + " to replay");

        assertEquals(aggregate1RepoCopy.size(),aggregate1Repo.size());
        assertEquals(aggregate2RepoCopy.size(),aggregate2Repo.size());

        for(String key : aggregate1Repo.keySet()){
            Aggregate1 agg = aggregate1Repo.get(key);
            Aggregate1 aggExpected = aggregate1RepoCopy.get(key);

            assertEquals(aggExpected.getI1(), agg.getI1());
            assertEquals(aggExpected.getI2(), agg.getI2());
            assertEquals(aggExpected.getStr(), agg.getStr());
            assertEquals(aggExpected.getId(), agg.getId());
        }

        for(String key : aggregate2Repo.keySet()){
            Aggregate2 agg = aggregate2Repo.get(key);
            Aggregate2 aggExpected = aggregate2RepoCopy.get(key);

            assertEquals(aggExpected.getS1(), agg.getS1());
            assertEquals(aggExpected.getS2(), agg.getS2());
            assertEquals(aggExpected.getId(), agg.getId());
        }


    }


    private int updateAggregates(CommandBus commandBus, int rnd, List<MyId> ids, int iterations) {
        Random randomSelect = new Random();
        //send a whole bunch of updates
        for (int i = 0; i < iterations; i++) {
            busyWaitMicros(50);

            MyId id = ids.get(randomSelect.nextInt(1000));
            rnd = xorShift(rnd);

            if (Integer.parseInt(id.toString()) % 2 == 0) {

                UpdateAggregate1Command command = new UpdateAggregate1Command(id, rnd - 5, rnd - 7);
                long t0 = System.nanoTime();
                commandBus.dispatch(command);

                Aggregate1 aggregate = repo1.find(id.toString());

                assertEquals(aggregate.getI1(), rnd - 5);
                assertEquals(aggregate.getI2(), rnd - 7);
            } else {

                UpdateAggregate2Command command = new UpdateAggregate2Command(id, "" + (rnd - 5), "" + (rnd - 7));
                long t0 = System.nanoTime();
                commandBus.dispatch(command);

                Aggregate2 aggregate = repo2.find(id.toString());

                assertEquals(aggregate.getS1(), "" + (rnd - 5));
                assertEquals(aggregate.getS2(), "" + (rnd - 7));
            }
            //aggregate  committed ->  events should be in AggregateEventStore
            //should be 4 all together 2 for the create and 2 for the update
        }

        return rnd;
    }

    private int createAggregates(CommandBus commandBus, int rnd, List<MyId> ids, List<Aggregate> aggregates, int iterations) {
        //create 1000 different aggregates
        for (int i = 0; i < iterations; i++) {
            busyWaitMicros(50);
            rnd = xorShift(rnd);
            MyId id = new MyId("" + rnd);
            ids.add(id);


            Aggregate aggregate = null;
            if (rnd % 2 == 0) {
                long t0 = System.nanoTime();
                commandBus.dispatch(new CreateAggregate1Command(id, rnd, rnd + 2));

                aggregate = repo1.find(id.toString());
            } else if (Math.abs(rnd % 2) == 1) {
                long t0 = System.nanoTime();
                commandBus.dispatch(new CreateAggregate2Command(id, "" + rnd, "" + (rnd + 2)));

                aggregate = repo2.find(id.toString());
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
