package com.dmc.d1.cqrs.event.store;

import com.dmc.d1.cqrs.event.JournalableAggregateEvent;
import com.dmc.d1.cqrs.util.ThreadLocalObjectPool;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ChronicleQueueBuilder;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class ChronicleAggregateEventStore implements AggregateEventStore<JournalableAggregateEvent> {

    private final ChronicleQueue chronicle;
    private final ExcerptAppender appender;
    private final ExcerptTailer tailer;
    private final String path;

    private final ChronicleIterator iterator;


    public ChronicleAggregateEventStore(String path) throws IOException {
        chronicle = ChronicleQueueBuilder.single(path)
                .blockSize(128 << 20)
                .build();

        appender = chronicle.createAppender();
        tailer = chronicle.createTailer();
        iterator = new ChronicleIterator(tailer);
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
    public Iterator<List<JournalableAggregateEvent>> iterator() {
        iterator.reset();
        return iterator;
    }

    private static class ChronicleIterator implements Iterator<List<JournalableAggregateEvent>> {
        private final ExcerptTailer tailer;
        private final List<JournalableAggregateEvent> lst = new ArrayList();

        private boolean hasNext = true;


        ChronicleIterator(ExcerptTailer tailer) {
            this.tailer = tailer;
            tailer.toStart();
        }

        public void reset() {
            this.hasNext = true;
            tailer.toStart();
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public List<JournalableAggregateEvent> next() {
            //iterate until ANY of the thread local pools are filled
            lst.clear();
            ThreadLocalObjectPool.clear();

            String classIdentifier;
            while ((classIdentifier = tailer.readText()) != null) {
                try (DocumentContext documentContext = tailer.readingDocument()) {
                    JournalableAggregateEvent e = ThreadLocalObjectPool.allocateObject(classIdentifier);
                    e.readMarshallable(documentContext.wire());
                    lst.add(e);

                    if (lst.size() == ThreadLocalObjectPool.slotSize(classIdentifier)) {
                        break;
                    }
                }
            }

            if (classIdentifier == null)
                this.hasNext = false;

            return lst;
        }
    }


    public String getChroniclePath() {
        return path;
    }

}
