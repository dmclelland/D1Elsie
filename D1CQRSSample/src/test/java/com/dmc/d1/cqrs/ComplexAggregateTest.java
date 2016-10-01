package com.dmc.d1.cqrs;

import com.dmc.d1.algo.event.Configuration;
import com.dmc.d1.cqrs.event.SimpleEventBus;
import com.dmc.d1.cqrs.sample.aggregate.ComplexAggregate;
import com.dmc.d1.cqrs.sample.commandhandler.ComplexCommandHandler;
import com.dmc.d1.sample.domain.Basket;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StopWatch;

import java.util.*;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;


/**
 * Created By davidclelland on 02/06/2016.
 */

@Ignore
public class ComplexAggregateTest extends RoundTripBaseTest {

    AggregateEventStore chronicleAES;
    AggregateRepository<ComplexAggregate> repo1;

    final int senderPoolThreadSize = 1;
    final int bufferSize = 1024;

    final long pauseInNanos = 1000;


    @Before
    public void setup() throws Exception {
        SimpleEventBus eventBus = new SimpleEventBus();
        chronicleAES = new ChronicleAggregateEventStore(Configuration.getChroniclePath());

        repo1 = new AggregateRepository(chronicleAES, ComplexAggregate.class, eventBus,
                ComplexAggregate.newInstanceFactory());

        this.commandBuilder = new CommandBuilders.CreateComplexAggregateCommandSupplier(100);
        super.setup();
    }

    @Override
    protected List<AbstractCommandHandler<? extends Aggregate>> getCommandHandlers() {
        List<AbstractCommandHandler<? extends Aggregate>> lst = new ArrayList<>();
        lst.add(new ComplexCommandHandler(repo1));

        return lst;
    }


    @Test
    public void testCreateAndReplayComplexEvents() throws Exception {

        int noOfCreateCommands = 10000;
        long t0 = System.currentTimeMillis();
        startSending(noOfCreateCommands);

        System.out.println("It took " + (System.currentTimeMillis() - t0) + " to process " + noOfCreateCommands * 2 + " commands");
        replayAndCompare();
    }

    private void replayAndCompare() {
        Map<String, ComplexAggregate> aggregate1Repo = (Map<String, ComplexAggregate>) ReflectionTestUtils.getField(repo1, "cache");
        Map<String, ComplexAggregate> aggregate1RepoCopy = new HashMap<>(aggregate1Repo);
        aggregate1Repo.clear();

        chronicleAES.replay(Collections.singletonMap(repo1.getAggregateClassName(), repo1));

        assertEquals(aggregate1RepoCopy.size(), aggregate1Repo.size());
        checkAssertions(aggregate1Repo, aggregate1RepoCopy);
    }


    private void checkAssertions(Map<String, ComplexAggregate> aggregate1Repo, Map<String, ComplexAggregate> aggregate1RepoCopy) {
        for (String key : aggregate1Repo.keySet()) {
            ComplexAggregate agg = aggregate1Repo.get(key);
            ComplexAggregate aggExpected = aggregate1RepoCopy.get(key);
            assertNotSame(aggExpected, agg);
            assertEquals(aggExpected.getId(), agg.getId());

            Basket expectedBasket = aggExpected.getBasket();
            Basket actualBasket = agg.getBasket();

            assertTrue(expectedBasket.getDivisor() > 0);
            assertEquals(expectedBasket.getDivisor(), actualBasket.getDivisor());

            assertTrue(expectedBasket.getRic().length() > 0);
            assertEquals(expectedBasket.getRic(), actualBasket.getRic());

            assertTrue(expectedBasket.getSecurity().getName().length() > 0);
            assertEquals(expectedBasket.getSecurity().getName(), actualBasket.getSecurity().getName());
            assertEquals(expectedBasket.getSecurity().getAdv20Day(), actualBasket.getSecurity().getAdv20Day());

            assertTrue(expectedBasket.getSecurity().getAssetType() != null);
            assertEquals(expectedBasket.getSecurity().getAssetType(), actualBasket.getSecurity().getAssetType());

            assertTrue(actualBasket.getBasketConstituents().size() > 0);
            assertEquals(expectedBasket.getBasketConstituents().size(), actualBasket.getBasketConstituents().size());


            for (int i = 0; i < expectedBasket.getBasketConstituents().size(); i++) {

                assertTrue(expectedBasket.getBasketConstituents().get(i).getRic().length() > 0);
                assertEquals(expectedBasket.getBasketConstituents().get(i).getRic(),
                        actualBasket.getBasketConstituents().get(i).getRic());

                assertTrue(expectedBasket.getBasketConstituents().get(i).getAdjustedShares() > 0);
                assertEquals(expectedBasket.getBasketConstituents().get(i).getAdjustedShares(),
                        actualBasket.getBasketConstituents().get(i).getAdjustedShares());

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
