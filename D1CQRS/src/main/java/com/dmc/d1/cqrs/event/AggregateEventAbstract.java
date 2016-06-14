package com.dmc.d1.cqrs.event;

/**
 * Created by davidclelland on 16/05/2016.
 */
public abstract class AggregateEventAbstract implements AggregateEvent {

    private String aggregateId;
    private String className;
    private String aggregateClassName;

    public final String getAggregateId() {
        return aggregateId;
    }

    public final void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public final String getClassName() {
        return className;
    }

    public final void setClassName(String className) {
        this.className = className;
    }

    public final String getAggregateClassName() {
        return aggregateClassName;
    }

    public final void setAggregateClassName(String aggregateClassName) {
        this.aggregateClassName = aggregateClassName;
    }
}
