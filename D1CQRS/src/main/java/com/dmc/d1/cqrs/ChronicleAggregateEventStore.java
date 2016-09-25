package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.event.JournalableAggregateEvent;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ChronicleQueueBuilder;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

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
                .blockSize(128 << 20)
                .build();

        appender = chronicle.createAppender();
        tailer = chronicle.createTailer();
        this.path = path;
    }


    @Override
    public void add(JournalableAggregateEvent event) {
        appender.writeText(event.getClassName());
        appender.writeDocument(event);
    }

    @Override
    public void add(List<JournalableAggregateEvent> events) {
        for (JournalableAggregateEvent event : events) {
            appender.writeText(event.getClassName());
            appender.writeDocument(event);
        }
    }

    @Override
    public void replay(Map<String, AggregateRepository> repos) {
        tailer.toStart();

        String classIdentifier;
        AggregateRepository repo;
        Aggregate agg;
        int i = 0;
        long t0 = System.currentTimeMillis();
        while ((classIdentifier = tailer.readText()) != null) {
            try (DocumentContext documentContext = tailer.readingDocument()) {

                JournalableAggregateEvent e = NEW_INSTANCE_FACTORIES.get(classIdentifier).get();

                e.readMarshallable(documentContext.wire());

                repo = repos.get(e.getAggregateClassName());
                if (e instanceof AggregateInitialisedEvent) {
                    repo.handleAggregateInitialisedEvent((AggregateInitialisedEvent) e);
                } else {
                    agg = repo.find(e.getAggregateId());
                    agg.replay(e);
                }
            }
            ++i;
        }

        LOG.info("It took {} millisecs to replay {} events", (System.currentTimeMillis() - t0), i);
    }

    public String getChroniclePath() {
        return path;
    }

    private static Map<String, Supplier<? extends JournalableAggregateEvent>> NEW_INSTANCE_FACTORIES
            = new HashMap<>();

    static {
        try {
            Reflections ref = new Reflections("com.dmc.d1");
            Set<Class<? extends JournalableAggregateEvent>> journalableSet = ref.getSubTypesOf(JournalableAggregateEvent.class);
            for (Class<? extends JournalableAggregateEvent> journalable : journalableSet) {
                if (!(Modifier.isAbstract(journalable.getModifiers()) || Modifier.isInterface(journalable.getModifiers()))) {
                    Method m = journalable.getDeclaredMethod("newInstanceFactory", null);
                    m.setAccessible(true);
                    Supplier<? extends JournalableAggregateEvent> newInstanceFactory = (Supplier<? extends JournalableAggregateEvent>) m.invoke(null);
                    NEW_INSTANCE_FACTORIES.put(newInstanceFactory.get().getClassName(), newInstanceFactory);

                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to set up thread local object pool", e);
        }
    }


}
