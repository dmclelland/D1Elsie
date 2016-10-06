package com.dmc.d1.cqrs.event;

/**
 * Created by davidclelland on 16/05/2016.
 */
public abstract class AggregateEventAbstract implements AggregateEvent {

    private long aggregateId;
    private String className;

    public final long getAggregateId() {
        return aggregateId;
    }

    public final void setAggregateId(long aggregateId) {
        this.aggregateId = aggregateId;
    }

    public final String getClassName() {
        return className;
    }

    public final void setClassName(String className) {
        this.className = className;
    }

}
