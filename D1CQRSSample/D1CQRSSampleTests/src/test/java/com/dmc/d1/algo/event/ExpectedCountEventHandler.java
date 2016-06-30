/*
 * Copyright 2011 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dmc.d1.algo.event;

import com.dmc.d1.cqrs.command.DisruptorCommandBus;
import com.lmax.disruptor.EventHandler;

import java.util.concurrent.CountDownLatch;

public final class ExpectedCountEventHandler implements EventHandler<DisruptorCommandBus.CommandHolder> {

    private long count;
    private CountDownLatch latch;

    public void reset(final CountDownLatch latch, final long expectedCount) {

        this.latch = latch;
        //first sequence is 0
        count = expectedCount-1;
    }

    @Override
    public void onEvent(final DisruptorCommandBus.CommandHolder event, final long sequence, final boolean endOfBatch) throws Exception {


        if (count == sequence) {
            latch.countDown();
        }
    }
}
