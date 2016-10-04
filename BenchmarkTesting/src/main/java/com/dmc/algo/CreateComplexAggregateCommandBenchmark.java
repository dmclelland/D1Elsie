/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.dmc.algo;

import com.dmc.d1.cqrs.*;
import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.command.CommandBus;
import com.dmc.d1.cqrs.command.SimpleCommandBus;
import com.dmc.d1.cqrs.event.SimpleEventBus;
import com.dmc.d1.cqrs.sample.aggregate.ComplexAggregate;
import com.dmc.d1.cqrs.sample.command.CreateComplexAggregateCommand;
import com.dmc.d1.cqrs.sample.commandhandler.ComplexCommandHandler;
import com.dmc.d1.sample.domain.Basket;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 4, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3)
public class CreateComplexAggregateCommandBenchmark {

    CommandBus commandBus;


    AggregateEventStore chronicleAES;
    AggregateRepository<ComplexAggregate> repo1;


    @Setup
    public void setUp() throws Exception {

        String chroniclePath = System.getProperty("java.io.tmpdir") + "/d1-events-" + System.currentTimeMillis();


        SimpleEventBus eventBus = new SimpleEventBus();
        chronicleAES = new ChronicleAggregateEventStore(chroniclePath);

        repo1 = new AggregateRepository(chronicleAES, ComplexAggregate.class, eventBus,
                ComplexAggregate.newInstanceFactory());

        List<AbstractCommandHandler<? extends Aggregate>> lst = new ArrayList<>();
        lst.add(new ComplexCommandHandler(repo1));

        commandBus = new SimpleCommandBus<>(lst);
        //commandBus = new StripedLockCommandBus<>(new SimpleCommandBus<>(lst));
    }

    int rnd = ((this.hashCode() ^ (int) System.nanoTime()));


    @Benchmark
    public Command testDispatchCommand() {


        rnd = xorShift(rnd);

        Basket basket = TestBasketBuilder.createBasket(rnd, 100);
        Command command = new CreateComplexAggregateCommand(rnd, basket);

        commandBus.dispatch(command);

        return command;
    }


    private static int xorShift(int x) {
        x ^= x << 6;
        x ^= x >>> 21;
        x ^= (x << 7);
        return x;
    }

    //-Xmx3G -Xms3g -XX:NewRatio=1 -XX:+PrintGC -XX:+DoEscapeAnalysis -XX:=Inline
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + CreateComplexAggregateCommandBenchmark.class.getSimpleName() + ".*")
                .addProfiler(GCProfiler.class)
                //.jvmArgsAppend("-Xmx2G", "-Xms2G", "-XX:NewRatio=1", "-XX:+PrintGC", "-XX:+DoEscapeAnalysis", "-XX:+Inline")
                .jvmArgsAppend("-Xmx4G", "-Xms4G", "-XX:NewRatio=1", "-XX:+PrintGC")

                .build();

        new org.openjdk.jmh.runner.Runner(opt).run();
    }


}
