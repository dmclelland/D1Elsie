package com.dmc.d1.cqrs;

import com.dmc.d1.algo.event.Configuration;
import com.dmc.d1.cqrs.command.CommandBus;
import com.dmc.d1.cqrs.command.SimpleCommandBus;
import com.dmc.d1.cqrs.event.AbstractEventHandler;
import com.dmc.d1.cqrs.event.AggregateInitialisedEvent;
import com.dmc.d1.cqrs.event.SimpleEventBus;
import com.dmc.d1.cqrs.event.store.AggregateEventStore;
import com.dmc.d1.cqrs.event.store.ChronicleAggregateEventStore;
import com.dmc.d1.cqrs.sample.aggregate.Aggregate1;
import com.dmc.d1.cqrs.sample.aggregate.Aggregate2;
import com.dmc.d1.cqrs.sample.aggregate.NestedAggregate1;
import com.dmc.d1.cqrs.sample.command.*;
import com.dmc.d1.cqrs.sample.commandhandler.MyCommandHandler1;
import com.dmc.d1.cqrs.sample.commandhandler.MyCommandHandler2;
import com.dmc.d1.cqrs.sample.commandhandler.MyNestedCommandHandler1;
import com.dmc.d1.cqrs.sample.domain.MyId;
import com.dmc.d1.cqrs.sample.domain.MyNestedId;
import com.dmc.d1.cqrs.sample.event.Aggregate1EventHandler;
import com.dmc.d1.cqrs.sample.event.Aggregate1EventHandler2;
import com.dmc.d1.sample.event.TestAggregateInitialisedEventBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;


/**
 * Created by davidclelland on 17/05/2016.
 */
public class CommandDirectInvokerTest {

    CommandBus commandBus;

    AggregateEventStore aes;
    AggregateRepository<Aggregate1> repo1;
    AggregateRepository<Aggregate2> repo2;
    AggregateRepository<NestedAggregate1> repo3;

    Aggregate1EventHandler agg1EventHandler;
    Aggregate1EventHandler2 agg1EventHandler2;

    DeleteStatic deleteOnClose = DeleteStatic.INSTANCE;

    Function<String, AggregateInitialisedEvent> initialisationFactory =
            (ID) -> TestAggregateInitialisedEventBuilder.startBuilding(ID).buildJournalable();

    @Before
    public void setup() throws Exception {

        SimpleEventBus eventBus = new SimpleEventBus();

        aes = new ChronicleAggregateEventStore(Configuration.getChroniclePath());

        repo1 = new AggregateRepository(aes, Aggregate1.class, eventBus, Aggregate1.newInstanceFactory(), initialisationFactory);
        repo2 = new AggregateRepository(aes, Aggregate2.class, eventBus, Aggregate2.newInstanceFactory(), initialisationFactory);
        repo3 = new AggregateRepository(aes, NestedAggregate1.class, eventBus, NestedAggregate1.newInstanceFactory(), initialisationFactory);

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

        if (aes instanceof ChronicleAggregateEventStore) {

            File file = new File(((ChronicleAggregateEventStore) aes).getChroniclePath());
            deleteOnClose.add(file);
        }
    }

    @Test
    public void testCreateAndUpdateCommand() {
        MyId id = MyId.from("testId1");

        CreateAggregate1Command command = new CreateAggregate1Command(id, 3, 5);
        commandBus.dispatch(command);

        Aggregate1 aggregate = repo1.find(id.asString());

        assertEquals(aggregate.getI1(), 3);
        assertEquals(aggregate.getI2(), 5);

        UpdateAggregate1Command command2 = new UpdateAggregate1Command(id, 6, 9);
        commandBus.dispatch(command2);

        aggregate = repo1.find(id.asString());

        assertEquals(aggregate.getI1(), 6);
        assertEquals(aggregate.getI2(), 9);

        //aggregate  committed ->  events should be in AggregateEventStore
        //should be 4 all together 2 for the create and 2 for the update

        //assertEquals(4, aes.getAll().size());
    }


    @Test
    public void testNestedCommandTriggeredCorrectly() {
        MyId id = MyId.from("testId1");

        CreateAggregate1Command command = new CreateAggregate1Command(id, 3, 5);
        commandBus.dispatch(command);


        UpdateAggregate1Command2 command2 = new UpdateAggregate1Command2(id, "NestedTest");
        commandBus.dispatch(command2);

        Aggregate1 aggregate = repo1.find(id.asString());


        assertEquals("NestedTest", aggregate.getStr());
        assertEquals(3, aggregate.getI1());
        assertEquals(5, aggregate.getI2());

        MyNestedId nestedId = MyNestedId.from("testId1Nested");

        NestedAggregate1 nested = repo3.find(nestedId.asString());

        assertEquals("NestedTest", nested.getNestedProperty());
        assertEquals("NestedTest", agg1EventHandler2.getString(id.asString()));
    }

    @Test
    public void rollbackTest() {

        MyId id = MyId.from("testId2");

        CreateAggregate2Command command = new CreateAggregate2Command(id, "Hello", "Goodbye");
        commandBus.dispatch(command);

        Aggregate2 aggregate = repo2.find(id.asString());

        assertEquals("Hello", aggregate.getS1());
        assertEquals("Goodbye", aggregate.getS2());

        //command2 handler throws an exception - should rollback
        ExceptionTriggeringAggregate2Command command2 = new ExceptionTriggeringAggregate2Command(id, "Blimey", "Where am I");
        commandBus.dispatch(command2);

        aggregate = repo2.find(id.asString());

        //events which have been stored in the event store get replayed, overwriting the old values
        assertEquals("Hello", aggregate.getS1());
        assertEquals("Goodbye", aggregate.getS2());
    }


    @Test
    public void parentAggregateRollsBackWithNestedAggregateFailure() {

        MyId id = MyId.from("testId1");
        MyNestedId nestedId = MyNestedId.from("testId1Nested");

        CreateAggregate1Command command = new CreateAggregate1Command(id, 3, 5);
        commandBus.dispatch(command);

        UpdateAggregate1Command2 command2 = new UpdateAggregate1Command2(id, "NestedTest");
        commandBus.dispatch(command2);

        Aggregate1 aggregate = repo1.find(id.asString());

        assertEquals("NestedTest", aggregate.getStr());
        assertEquals(3, aggregate.getI1());
        assertEquals(5, aggregate.getI2());
        NestedAggregate1 nested = repo3.find(nestedId.asString());
        assertEquals("NestedTest", nested.getNestedProperty());

        NestedExceptionTriggeringAggregate1Command command3 = new NestedExceptionTriggeringAggregate1Command(id,
                "Should not update");
        commandBus.dispatch(command3);
        //values should stay as they were
        aggregate = repo1.find(id.asString());

        assertEquals("NestedTest", aggregate.getStr());
        assertEquals(3, aggregate.getI1());
        assertEquals(5, aggregate.getI2());
        nested = repo3.find(nestedId.asString());
        assertEquals("NestedTest", nested.getNestedProperty());
    }

    enum DeleteStatic {
        INSTANCE;
        final Set<File> toDeleteList = new LinkedHashSet<>();

        {
            Runtime.getRuntime().addShutdownHook(new Thread(
                    () -> toDeleteList.forEach(CommandDirectInvokerTest::delete
                    )));
        }

        synchronized void add(File file) {
            toDeleteList.add(file);
        }
    }

    static void delete(File folder) {
        String[] entries = folder.list();
        for (String s : entries) {
            File currentFile = new File(folder.getPath(), s);
            boolean deleted = currentFile.delete();

            System.out.print(deleted);
        }
    }


}