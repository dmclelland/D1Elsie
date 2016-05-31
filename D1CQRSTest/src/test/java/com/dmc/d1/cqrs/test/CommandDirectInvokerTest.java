package com.dmc.d1.cqrs.test;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.AggregateEventStore;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.InMemoryAggregateEventStore;
import com.dmc.d1.cqrs.command.AbstractCommandHandler;
import com.dmc.d1.cqrs.command.CommandBus;
import com.dmc.d1.cqrs.command.SimpleCommandBus;
import com.dmc.d1.cqrs.event.AbstractEventHandler;
import com.dmc.d1.cqrs.event.EventBus;
import com.dmc.d1.cqrs.event.SimpleEventBus;
import com.dmc.d1.cqrs.test.command.*;
import com.dmc.d1.cqrs.test.commandhandler.MyCommandHandler1;
import com.dmc.d1.cqrs.test.commandhandler.MyCommandHandler2;
import com.dmc.d1.cqrs.test.commandhandler.MyNestedCommandHandler1;
import com.dmc.d1.cqrs.test.domain.*;
import com.dmc.d1.cqrs.test.event.Aggregate1EventHandler;
import com.dmc.d1.cqrs.test.event.Aggregate1EventHandler2;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 * Created by davidclelland on 17/05/2016.
 */
public class CommandDirectInvokerTest {

    CommandBus commandBus;

    AggregateEventStore aes = new InMemoryAggregateEventStore();
    AggregateRepository<Aggregate1> repo1;
    AggregateRepository<Aggregate2> repo2;
    AggregateRepository<NestedAggregate1> repo3;

    Aggregate1EventHandler agg1EventHandler;
    Aggregate1EventHandler2 agg1EventHandler2;

    @Before
    public void setup() {

        SimpleEventBus eventBus = new SimpleEventBus();

        repo1 = new AggregateRepository(aes, Aggregate1.class, eventBus);
        repo2 = new AggregateRepository(aes, Aggregate2.class, eventBus);
        repo3 = new AggregateRepository(aes, NestedAggregate1.class, eventBus);

        List<AbstractCommandHandler<? extends Aggregate>> lst = new ArrayList<>();

        lst.add(new MyCommandHandler1(repo1));
        lst.add(new MyCommandHandler2(repo2));
        lst.add(new MyNestedCommandHandler1(repo3));

        commandBus = new SimpleCommandBus(lst);

        agg1EventHandler = new Aggregate1EventHandler(commandBus);
        agg1EventHandler2 = new Aggregate1EventHandler2();

        List<? super AbstractEventHandler> eventHandlers = new ArrayList<>();
        eventHandlers.add(agg1EventHandler);
        eventHandlers.add(agg1EventHandler2);

        eventBus.registerEventHandlers(eventHandlers);

    }

    @Test
    public void testCreateAndUpdateCommand() {
        MyId id = new MyId("testId1");

        CreateAggregate1Command command = new CreateAggregate1Command(id, 3, 5);
        commandBus.dispatch(command);

        Aggregate1 aggregate = repo1.find(id.toString());

        assertEquals(aggregate.getI1(), 3);
        assertEquals(aggregate.getI2(), 5);

        UpdateAggregate1Command command2 = new UpdateAggregate1Command(id, 6, 9);
        commandBus.dispatch(command2);

        aggregate = repo1.find(id.toString());

        assertEquals(aggregate.getI1(), 6);
        assertEquals(aggregate.getI2(), 9);

        //aggregate  committed ->  events should be in AggregateEventStore
        //should be 4 all together 2 for the create and 2 for the update

        assertEquals(4, aes.getAll().size());
    }



    @Test
    public void testNestedCommandTriggeredCorrectly() {
        MyId id = new MyId("testId1");

        CreateAggregate1Command command = new CreateAggregate1Command(id, 3, 5);
        commandBus.dispatch(command);


        UpdateAggregate1Command2 command2 = new UpdateAggregate1Command2(id, "NestedTest");
        commandBus.dispatch(command2);

        Aggregate1 aggregate = repo1.find(id.toString());

        assertEquals(aggregate.getStr(),"NestedTest");
        assertEquals(aggregate.getI1(), 3);
        assertEquals(aggregate.getI2(), 5);

        MyNestedId nestedId = new MyNestedId("testId1Nested");

        NestedAggregate1 nested = repo3.find(nestedId.toString());

        assertEquals("NestedTest", nested.getNestedProperty());

        assertEquals("NestedTest", agg1EventHandler2.getString(id));
    }

    @Test
    public void rollbackTest() {

        MyId id = new MyId("testId2");

        CreateAggregate2Command command = new CreateAggregate2Command(id, "Hello", "Goodbye");
        commandBus.dispatch(command);

        Aggregate2 aggregate = repo2.find(id.toString());

        assertEquals(aggregate.getS1(), "Hello");
        assertEquals(aggregate.getS2(), "Goodbye");

        //command2 handler throws an exception - should rollback
        UpdateAggregate2Command command2 = new UpdateAggregate2Command(id, "Blimey", "Where am I");
        commandBus.dispatch(command2);

        aggregate = repo2.find(id.toString());

        //events which have been stored in the event store get replayed, overwriting the old values
        assertEquals(aggregate.getS1(), "Hello");
        assertEquals(aggregate.getS2(), "Goodbye");
    }
}