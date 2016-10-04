package com.dmc.d1.cqrs;

import com.dmc.d1.algo.event.Configuration;
import com.dmc.d1.cqrs.event.SimpleEventBus;
import com.dmc.d1.cqrs.sample.aggregate.ComplexMutableAggregate;
import com.dmc.d1.cqrs.sample.commandhandler.ComplexMutableCommandHandler;
import com.dmc.d1.sample.domain.Basket2;
import com.dmc.d1.sample.domain.BasketConstituent2;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;


/**
 * Created By davidclelland on 02/06/2016.
 */

@Ignore
public class ComplexMutableAggregateTest extends RoundTripBaseTest {

    AggregateEventStore chronicleAES;
    AggregateRepository<ComplexMutableAggregate> repo1;

    final int senderPoolThreadSize = 1;
    final int bufferSize = 1024;

    final long pauseInNanos = 1000;


    @Before
    public void setup() throws Exception {
        SimpleEventBus eventBus = new SimpleEventBus();
        chronicleAES = new ChronicleAggregateEventStore(Configuration.getChroniclePath());

        repo1 = new AggregateRepository(chronicleAES, ComplexMutableAggregate.class, eventBus,
                ComplexMutableAggregate.newInstanceFactory());

        this.commandBuilder = new CommandBuilders.CreateMutableComplexAggregateCommandSupplier(50);
        super.setup();
    }

    @Override
    protected List<AbstractCommandHandler<? extends Aggregate>> getCommandHandlers() {
        List<AbstractCommandHandler<? extends Aggregate>> lst = new ArrayList<>();
        lst.add(new ComplexMutableCommandHandler(repo1));
        return lst;
    }

    @Test
    public void testCreateAndReplayComplexEvents() throws Exception {

        long t0 = System.currentTimeMillis();
        int noOfCreateCommands = 20000;
        startSending(noOfCreateCommands);
        System.out.println("It took " + (System.currentTimeMillis() - t0) + " to process " + noOfCreateCommands * 2 + " commands");

        Map<String, ComplexMutableAggregate> aggregate1Repo = (Map<String, ComplexMutableAggregate>) ReflectionTestUtils.getField(repo1, "cache");
        this.commandBuilder = new CommandBuilders.UpdateBasketConstituentCommandSupplier(new ArrayList<>(aggregate1Repo.values()));

        t0 = System.currentTimeMillis();
        int noOfUpdateCommands = 500000;
        startSending(noOfUpdateCommands);
        System.out.println("It took " + (System.currentTimeMillis() - t0) + " to process " + noOfUpdateCommands * 2 + " commands");
        replayAndCompare();
    }


    @Test
    public void testRollback() throws Exception {

        long t0 = System.currentTimeMillis();
        int noOfCreateCommands = 500;
        startSending(noOfCreateCommands);
        System.out.println("It took " + (System.currentTimeMillis() - t0) + " to process " + noOfCreateCommands * 2 + " commands");

        Map<String, ComplexMutableAggregate> aggregate1Repo = (Map<String, ComplexMutableAggregate>) ReflectionTestUtils.getField(repo1, "cache");

        this.commandBuilder = new CommandBuilders.UpdateBasketConstituentWithDeterministicExceptionCommandSupplier(
                new ArrayList<>(aggregate1Repo.values()));
        t0 = System.currentTimeMillis();
        int noOfUpdateCommands = 50000;
        startSending(noOfUpdateCommands);

        System.out.println("No of exceptions " + ComplexMutableAggregate.noOfExceptions);
        System.out.println("It took " + (System.currentTimeMillis() - t0) + " to process " + noOfUpdateCommands * 2 + " commands");

        //check all adjusted shares
        for (ComplexMutableAggregate agg : aggregate1Repo.values()) {

            for (BasketConstituent2 constituent : agg.getBasket().getBasketConstituents2().values()) {

                //57 specified in updateBasketConstituentWithDeterministicException gets rolled back
                if (constituent.getInitialAdjustedShares() % 57 == 0) {
                    assertEquals(constituent.getInitialAdjustedShares(), constituent.getAdjustedShares());
                }
            }
        }

        replayAndCompare();
    }

    @Test
    public void testRollbackFollowedByCommitsReplayCorrectly() throws Exception {

        long t0 = System.currentTimeMillis();
        int noOfCreateCommands = 500;
        startSending(noOfCreateCommands);
        System.out.println("It took " + (System.currentTimeMillis() - t0) + " to process " + noOfCreateCommands * 2 + " commands");

        Map<String, ComplexMutableAggregate> aggregate1Repo = (Map<String, ComplexMutableAggregate>) ReflectionTestUtils.getField(repo1, "cache");

        this.commandBuilder = new CommandBuilders.UpdateBasketConstituentWithDeterministicExceptionCommandSupplier(
                new ArrayList<>(aggregate1Repo.values()));

        int noOfUpdateCommands = 50000;
        startSending(noOfUpdateCommands);

        replayAndCompare();

        this.commandBuilder = new CommandBuilders.UpdateBasketConstituentCommandSupplier(
                new ArrayList<>(aggregate1Repo.values()));

        noOfUpdateCommands = 50000;
        startSending(noOfUpdateCommands);


        replayAndCompare();
    }


    private void replayAndCompare() {

        //remove ponger from disruptor bus, then re-add at the end

        Map<Long, ComplexMutableAggregate> aggregate1Repo = (Map<Long, ComplexMutableAggregate>) ReflectionTestUtils.getField(repo1, "cache");
        Map<Long, ComplexMutableAggregate> aggregate1RepoCopy = new HashMap<>(aggregate1Repo);
        aggregate1Repo.clear();
        chronicleAES.replay(Collections.singletonMap(repo1.getAggregateClassName(), repo1));
        assertEquals(aggregate1RepoCopy.size(), aggregate1Repo.size());
        checkAssertions(aggregate1Repo, aggregate1RepoCopy);
    }

    private void checkAssertions(Map<Long, ComplexMutableAggregate> aggregate1Repo, Map<Long, ComplexMutableAggregate> aggregate1RepoCopy) {
        for (Long key : aggregate1Repo.keySet()) {
            ComplexMutableAggregate agg = aggregate1Repo.get(key);
            ComplexMutableAggregate aggExpected = aggregate1RepoCopy.get(key);
            assertNotSame(aggExpected, agg);
            assertEquals(aggExpected.getId(), agg.getId());

            Basket2 expectedBasket = aggExpected.getBasket();
            Basket2 actualBasket = agg.getBasket();

            assertTrue(expectedBasket.getDivisor() > 0);
            assertEquals(expectedBasket.getDivisor(), actualBasket.getDivisor());

            assertTrue(expectedBasket.getRic().length() > 0);
            assertEquals(expectedBasket.getRic(), actualBasket.getRic());

            assertTrue(expectedBasket.getSecurity().getName().length() > 0);
            assertEquals(expectedBasket.getSecurity().getName(), actualBasket.getSecurity().getName());
            assertEquals(expectedBasket.getSecurity().getAdv20Day(), actualBasket.getSecurity().getAdv20Day());

            assertTrue(expectedBasket.getSecurity().getAssetType() != null);
            assertEquals(expectedBasket.getSecurity().getAssetType(), actualBasket.getSecurity().getAssetType());

            assertTrue(actualBasket.getBasketConstituents2().size() > 0);
            assertEquals(expectedBasket.getBasketConstituents2().size(), actualBasket.getBasketConstituents2().size());


            for (BasketConstituent2 constituent2 : expectedBasket.getBasketConstituents2().values()) {
                String constituentRic = constituent2.getRic();

                assertTrue(constituentRic.length() > 0);


                assertTrue(expectedBasket.getBasketConstituents2().get(constituentRic).getAdjustedShares() > 0);
                assertEquals(expectedBasket.getBasketConstituents2().get(constituentRic).getAdjustedShares(),
                        actualBasket.getBasketConstituents2().get(constituentRic).getAdjustedShares());

            }
        }
    }

    @Override
    public int senderThreadPoolSize() {
        return senderPoolThreadSize;
    }

    @Override
    public int bufferSize() {
        return bufferSize;
    }

    @Override
    public long pauseNanos() {
        return pauseInNanos;
    }
}
