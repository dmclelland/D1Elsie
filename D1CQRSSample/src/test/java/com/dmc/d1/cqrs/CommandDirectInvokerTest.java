package com.dmc.d1.cqrs;

import com.dmc.d1.algo.event.Configuration;
import com.dmc.d1.cqrs.command.CommandBus;
import com.dmc.d1.cqrs.command.SimpleCommandBus;
import com.dmc.d1.cqrs.event.AbstractEventHandler;
import com.dmc.d1.cqrs.event.SimpleEventBus;
import com.dmc.d1.cqrs.sample.aggregate.Aggregate1;
import com.dmc.d1.cqrs.sample.aggregate.Aggregate2;
import com.dmc.d1.cqrs.sample.aggregate.NestedAggregate1;
import com.dmc.d1.cqrs.sample.command.*;
import com.dmc.d1.cqrs.sample.commandhandler.MyCommandHandler1;
import com.dmc.d1.cqrs.sample.commandhandler.MyCommandHandler2;
import com.dmc.d1.cqrs.sample.commandhandler.MyNestedCommandHandler1;
import com.dmc.d1.cqrs.sample.event.Aggregate1EventHandler;
import com.dmc.d1.cqrs.sample.event.Aggregate1EventHandler2;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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


    @Before
    public void setup() throws Exception {

        SimpleEventBus eventBus = new SimpleEventBus();

        aes = new ChronicleAggregateEventStore(Configuration.getChroniclePath());

        repo1 = new AggregateRepository(aes, eventBus, Aggregate1.newInstanceFactory());
        repo2 = new AggregateRepository(aes, eventBus, Aggregate2.newInstanceFactory());
        repo3 = new AggregateRepository(aes, eventBus, NestedAggregate1.newInstanceFactory());

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
        long id = 1;

        CreateAggregate1Command command = new CreateAggregate1Command(id, 3, 5);
        commandBus.dispatch(command);

        Aggregate1 aggregate = repo1.find(id);

        assertEquals(aggregate.getI1(), 3);
        assertEquals(aggregate.getI2(), 5);

        UpdateAggregate1Command command2 = new UpdateAggregate1Command(id, 6, 9);
        commandBus.dispatch(command2);

        aggregate = repo1.find(id);

        assertEquals(aggregate.getI1(), 6);
        assertEquals(aggregate.getI2(), 9);

        //aggregate  committed ->  events should be in AggregateEventStore
        //should be 4 all together 2 for the create and 2 for the update

        //assertEquals(4, aes.getAll().size());
    }


    @Test
    public void testNestedCommandTriggeredCorrectly() {
        long id = 1;

        CreateAggregate1Command command = new CreateAggregate1Command(id, 3, 5);
        commandBus.dispatch(command);


        UpdateAggregate1Command2 command2 = new UpdateAggregate1Command2(id, "NestedTest");
        commandBus.dispatch(command2);

        Aggregate1 aggregate = repo1.find(id);


        assertEquals("NestedTest", aggregate.getStr());
        assertEquals(3, aggregate.getI1());
        assertEquals(5, aggregate.getI2());

        long nestedId = 3;

        NestedAggregate1 nested = repo3.find(nestedId);

        assertEquals("NestedTest", nested.getNestedProperty());
        assertEquals("NestedTest", agg1EventHandler2.getString(id));
    }

    @Test
    public void rollbackTest() {

        long id = 1;

        CreateAggregate2Command command = new CreateAggregate2Command(id, "Hello", "Goodbye");
        commandBus.dispatch(command);

        Aggregate2 aggregate = repo2.find(id);

        assertEquals("Hello", aggregate.getS1());
        assertEquals("Goodbye", aggregate.getS2());

        //command2 handler throws an exception - should rollback
        ExceptionTriggeringAggregate2Command command2 = new ExceptionTriggeringAggregate2Command(id, "Blimey", "Where am I");
        commandBus.dispatch(command2);

        aggregate = repo2.find(id);

        //events which have been stored in the event store get replayed, overwriting the old values
        assertEquals("Hello", aggregate.getS1());
        assertEquals("Goodbye", aggregate.getS2());
    }


    @Test
    public void parentAggregateRollsBackWithNestedAggregateFailure() {

        long id = 1;
        long nestedId = 3;

        CreateAggregate1Command command = new CreateAggregate1Command(id, 3, 5);
        commandBus.dispatch(command);

        UpdateAggregate1Command2 command2 = new UpdateAggregate1Command2(id, "NestedTest");
        commandBus.dispatch(command2);

        Aggregate1 aggregate = repo1.find(id);

        assertEquals("NestedTest", aggregate.getStr());
        assertEquals(3, aggregate.getI1());
        assertEquals(5, aggregate.getI2());
        NestedAggregate1 nested = repo3.find(nestedId);
        assertEquals("NestedTest", nested.getNestedProperty());

        NestedExceptionTriggeringAggregate1Command command3 = new NestedExceptionTriggeringAggregate1Command(id,
                "Should not update");
        commandBus.dispatch(command3);
        //values should stay as they were
        aggregate = repo1.find(id);

        assertEquals("NestedTest", aggregate.getStr());
        assertEquals(3, aggregate.getI1());
        assertEquals(5, aggregate.getI2());
        nested = repo3.find(nestedId);
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