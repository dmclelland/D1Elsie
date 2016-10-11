package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.codegen.Journalable;
import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.event.JournalableAggregateEvent;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ChronicleQueueBuilder;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class ChronicleAggregateEventStore implements AggregateEventStore<JournalableAggregateEvent>, Closeable {

    private final ChronicleQueue chronicle;
    private final ExcerptAppender appender;
    private final ExcerptTailer tailer;
    private final String path;

    private static Logger LOG = LoggerFactory.getLogger(ChronicleAggregateEventStore.class);

    public ChronicleAggregateEventStore(String path) throws Exception {
        chronicle = ChronicleQueueBuilder.single(path)
                .blockSize(128 << 22)
                .build();
        appender = chronicle.acquireAppender();
        tailer = chronicle.createTailer();
        this.path = path;
        addAggregatesToEvents();
    }

    public void close(){
        chronicle.close();
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


    private void addAggregatesToEvents() throws Exception{
        //get aggregate event handlers so we can associate events with their aggregates

        Reflections reflections = new Reflections("com.dmc.d1");

        Set<Class<? extends Aggregate>> aggregates = reflections.getSubTypesOf(Aggregate.class);


        for (Class<? extends Aggregate> aggregateClass : aggregates) {
            for (Method m : Utils.methodsOf(aggregateClass)) {

                if (m.isAnnotationPresent(EventHandler.class)) {

                    if (m.getParameterTypes().length == 1 && AggregateEvent.class.isAssignableFrom(m.getParameterTypes()[0])) {

                        //gets the event interface
                        Class eventInterface = m.getParameterTypes()[0];
                        Set<Class<? extends AggregateEvent>> eventImplementations = reflections.getSubTypesOf(eventInterface);

                        for(Class<? extends AggregateEvent> event : eventImplementations){
                            if(Journalable.class.isAssignableFrom(event)){

                                Field field = event.getDeclaredField("aggregateClassName");
                                field.setAccessible(true);
                                field.set(null, aggregateClass.getName());
                            }
                        }
                    }
                }
            }
        }
    }
}
