package com.dmc.d1.domain;

/**
 * Created by davidclelland on 16/05/2016.
 */
public abstract class Id {

    private String id;

    public Id(String id){
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
