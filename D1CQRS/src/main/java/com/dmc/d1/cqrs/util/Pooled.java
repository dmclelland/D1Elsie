package com.dmc.d1.cqrs.util;

/**
 * Created By davidclelland on 17/06/2016.
 */
public interface Pooled {
    //accessed reflectively
    <T extends Pooled> NewInstanceFactory<T> getNewInstanceFactory();
}
