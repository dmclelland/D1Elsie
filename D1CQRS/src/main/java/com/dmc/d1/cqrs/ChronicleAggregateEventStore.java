package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.event.JournalableAggregateEvent;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ChronicleQueueBuilder;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class ChronicleAggregateEventStore implements AggregateEventStore<JournalableAggregateEvent> {

    private final ChronicleQueue chronicle;
    private final ExcerptAppender appender;
    private final ExcerptTailer tailer;
    private final String path;

    private static Logger LOG = LoggerFactory.getLogger(ChronicleAggregateEventStore.class);

    public ChronicleAggregateEventStore(String path) throws IOException {
        chronicle = ChronicleQueueBuilder.single(path)
                .blockSize(128 << 22)
                .build();

        appender = chronicle.acquireAppender();
        tailer = chronicle.createTailer();
        this.path = path;
    }


    @Override
    public void add(JournalableAggregateEvent event) {
        appender.writeDocument(w -> w.write().object(event));
    }

    @Override
    public void add(List<JournalableAggregateEvent> events) {
        for (JournalableAggregateEvent event : events) {
            appender.writeDocument(w -> w.write().object(event));
        }
    }

    @Override
    public void replay(Map<String, AggregateRepository> repos) {
        tailer.toStart();

        AggregateRepository repo;
        Aggregate agg;
        int i = 0;
        long t0 = System.currentTimeMillis();
        while (true) {
            try (DocumentContext dc = tailer.readingDocument()) {
                if (!dc.isPresent())
                    break;

                //wireIn.read(() -> "basket").object(Basket2.class, this, (o,b) -> o.basket = b);

                JournalableAggregateEvent e = (JournalableAggregateEvent) dc.wire().read().object();

                repo = repos.get(e.getAggregateClassName());
                if (e instanceof AggregateInitialisedEvent) {
                    repo.handleAggregateInitialisedEvent((AggregateInitialisedEvent) e);
                } else {
                    agg = repo.find(e.getAggregateId());
                    agg.replay(e);
                }

                ++i;
            }
        }

        LOG.info("It took {} millisecs to replay {} events", (System.currentTimeMillis() - t0), i);
    }

    public String getChroniclePath() {
        return path;
    }

}
