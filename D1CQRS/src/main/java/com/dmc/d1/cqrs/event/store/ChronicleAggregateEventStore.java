package com.dmc.d1.cqrs.event.store;

import com.dmc.d1.cqrs.event.ChronicleAggregateEvent;
import com.dmc.d1.cqrs.util.InstanceAllocator;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ChronicleQueueBuilder;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class ChronicleAggregateEventStore implements AggregateEventStore<ChronicleAggregateEvent>{

    private final ChronicleQueue chronicle;
    private final ExcerptAppender appender;
    private final ExcerptTailer tailer;
    private final InstanceAllocator<ChronicleAggregateEvent> instanceAllocator;
    private final String path;


    public ChronicleAggregateEventStore(InstanceAllocator<ChronicleAggregateEvent> instanceAllocator, String path) throws IOException {

        chronicle = ChronicleQueueBuilder.single(path)
                .build();

        appender = chronicle.createAppender();
        tailer = chronicle.createTailer();
        this.instanceAllocator = instanceAllocator;
        this.path = path;
    }

    @Override
    public void add(ChronicleAggregateEvent event) {
        appender.writeText(event.getClassName());
        appender.writeDocument(event);
    }

    @Override
    public void add(List<ChronicleAggregateEvent> events) {
        for (ChronicleAggregateEvent event : events) {
            appender.writeText(event.getClassName());
            appender.writeDocument(event);
        }
    }

//    @Override
//    public AggregateEventAbstract get() {
//         tailer.readDocument(w -> System.out.println("msg: " + w.read(()->"msg").text()));
//
//        AggregateEventAbstract event = (AggregateEventAbstract)tailer.readDocument(event.)
//
//        return event;
//    }
//
//    @Override
//    public List<AggregateEventAbstract> getAll() {
//        return null;
//    }


    @Override
    public List<ChronicleAggregateEvent> get(String id) {

        tailer.toStart();

        List<ChronicleAggregateEvent> lst = new ArrayList<>();

        String classIdentifier;
        while ((classIdentifier = tailer.readText()) != null) {
            try (DocumentContext documentContext = tailer.readingDocument()) {
                ChronicleAggregateEvent e = instanceAllocator.allocateInstance(classIdentifier);
                e.readMarshallable(documentContext.wire());

                if (id.equals(e.getAggregateId())) {
                    lst.add(e);
                }
            }
        }
        return lst;
    }

    @Override
    public List<ChronicleAggregateEvent> getAll() {
        tailer.toStart();

        List<ChronicleAggregateEvent> lst = new ArrayList<>();

        String classIdentifier;
        while ((classIdentifier = tailer.readText()) != null) {
            try (DocumentContext documentContext = tailer.readingDocument()) {
                ChronicleAggregateEvent e = instanceAllocator.allocateInstance(classIdentifier);
                e.readMarshallable(documentContext.wire());
                lst.add(e);
            }
        }
        return lst;
    }



    public String getChroniclePath() {
        return path;
    }

}
