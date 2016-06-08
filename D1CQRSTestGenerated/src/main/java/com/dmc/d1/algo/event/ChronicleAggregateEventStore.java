package com.dmc.d1.algo.event;

import com.dmc.d1.cqrs.event.ChronicleAggregateEvent;
import com.dmc.d1.cqrs.event.store.AggregateEventStore;
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
public class ChronicleAggregateEventStore implements AggregateEventStore<ChronicleAggregateEvent> {

    private final static String BASE_PATH = System.getProperty("java.io.tmpdir") + "/d1-events";

    private final ChronicleQueue chronicle;
    private final ExcerptAppender appender;
    private final ExcerptTailer tailer;
    private final EventFactoryChronicle efc = new EventFactoryChronicle();


    public ChronicleAggregateEventStore() throws IOException {
        chronicle = ChronicleQueueBuilder.single(BASE_PATH)
                .build();

        appender = chronicle.createAppender();
        tailer = chronicle.createTailer();
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
//    public ChronicleAggregateEvent get() {
//         tailer.readDocument(w -> System.out.println("msg: " + w.read(()->"msg").text()));
//
//        ChronicleAggregateEvent event = (ChronicleAggregateEvent)tailer.readDocument(event.)
//
//        return event;
//    }
//
//    @Override
//    public List<ChronicleAggregateEvent> getAll() {
//        return null;
//    }

    @Override
    public List<ChronicleAggregateEvent> get(String id) {

        tailer.toStart();

        List<ChronicleAggregateEvent> lst =  new ArrayList<>();

        String classIdentifier;
        while ((classIdentifier = tailer.readText()) != null) {
            try (DocumentContext documentContext = tailer.readingDocument()) {
                ChronicleAggregateEvent e = efc.createEventPlaceholder(classIdentifier);
                e.readMarshallable(documentContext.wire());

                if(id.equals(e.getAggregateId())) {
                    lst.add(e);
                }
            }
        }
        return lst;
    }

    public String getChroniclePath() {
        return BASE_PATH;
    }

//    private ChronicleAggregateEvent deserialize(Wire wire){
//        ChronicleAggregateEvent event = new Person();
//        //This line deserialises the Bytes (created in the serialise method) using
//        //the wire implementation provided.
//        person.readMarshallable(wire);
//        System.out.println("Deserialised person: " + person);
//    }

}
