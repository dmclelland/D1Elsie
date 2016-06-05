package com.dmc.d1.cqrs.event.store;

import com.dmc.d1.cqrs.event.ChronicleAggregateEvent;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;

import java.io.IOException;
import java.util.List;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class ChronicleAggregateEventStore implements AggregateEventStore<ChronicleAggregateEvent> {

    private final static String BASE_PATH = System.getProperty("java.io.tmpdir") + "/d1-events";

    private final Chronicle chronicle;
    private final ExcerptAppender appender;
    private final ExcerptTailer tailer;

    ChronicleAggregateEventStore() throws IOException {
        chronicle = ChronicleQueueBuilder.vanilla(BASE_PATH)
                .indexBlockSize(32 << 20)
                .dataBlockSize(128 << 20)
                .build();

        appender = chronicle.createAppender();
        tailer = chronicle.createTailer();

    }

    @Override
    public void add(ChronicleAggregateEvent event) {
        writeMessage(event);

    }

    @Override
    public void add(List<ChronicleAggregateEvent> events) {
        for(ChronicleAggregateEvent event: events){
            writeMessage(event);
        }

    }

    @Override
    public ChronicleAggregateEvent get() {
        ChronicleAggregateEvent event = (ChronicleAggregateEvent)tailer.readObject();

        return event;
    }

    @Override
    public List<ChronicleAggregateEvent> getAll() {
        return null;
    }

    @Override
    public List<ChronicleAggregateEvent> get(String id) {

        return null;
    }



    private void writeMessage(ChronicleAggregateEvent event) {
        appender.startExcerpt(128);
        event.writeMarshallable(appender);
        appender.finish();
    }

}
