package com.dmc.d1.cqrs.command;

import com.dmc.d1.cqrs.AbstractCommandHandler;
import com.dmc.d1.cqrs.Aggregate;
import com.google.common.util.concurrent.Striped;

import java.util.concurrent.locks.Lock;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class StripedLockCommandBus<T extends AbstractCommandHandler<? extends Aggregate>> implements CommandBus {

    private final Striped<Lock> striped = Striped.lock(50);

    private final SimpleCommandBus<T> simpleCommandBus;

    public StripedLockCommandBus(SimpleCommandBus<T> simpleCommandBus) {
        this.simpleCommandBus = checkNotNull(simpleCommandBus);
    }

    @Override
    public void dispatch(Command command) {
        Lock lock = striped.get(command.getAggregateId());

        try {
            lock.lock();
            simpleCommandBus.dispatch(command);
        } finally {
            lock.unlock();
        }
    }
}