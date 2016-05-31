package com.dmc.d1.algo.event;

import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.event.AbstractEventHandler;

/**
 * Created By davidclelland on 31/05/2016.
 */

public class WaveEventHandler extends AbstractEventHandler {

    @EventHandler
    public void handle(WaveCreatedEvent e){

    }
}
