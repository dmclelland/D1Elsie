package com.dmc.d1.cqrs;

/**
 * Created By davidclelland on 12/06/2016.
 */
public interface AggregateFactory<A extends Aggregate> {

    A create(String id, String typeName);



}
