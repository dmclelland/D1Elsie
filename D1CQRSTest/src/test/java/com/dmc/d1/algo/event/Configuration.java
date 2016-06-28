package com.dmc.d1.algo.event;

/**
 * Created By davidclelland on 10/06/2016.
 */

public class Configuration {

    public static final String getChroniclePath() {
        return System.getProperty("java.io.tmpdir") + "/d1-events-" + System.currentTimeMillis();
    }


}
