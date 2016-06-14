package com.dmc.d1.cqrs.util;

/**
 * Created By davidclelland on 09/06/2016.
 */
public interface InstanceAllocator<E> {

    E allocateInstance(String className);
}
