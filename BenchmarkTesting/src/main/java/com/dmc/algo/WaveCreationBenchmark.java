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

import com.dmc.d1.algo.domain.SecurityBuilder;
import com.dmc.d1.algo.domain.Wave;
import com.dmc.d1.algo.domain.WaveBuilder;
import com.dmc.d1.domain.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 2, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(value = 5)
public class WaveCreationBenchmark {

    @Benchmark
    public Wave testImmutable(State state) {

        //Blackhole.consumeCPU(10);
        // This is a demo/sample template for building your JMH benchmarks. Edit as needed.
        // Put your benchmark code here.
        return WaveBuilder.startBuilding()
                .orderId(state.orderId)
                .quantity(state.quantity)
                .tradeDate(state.tradeDate)
                .tradeDirection(state.tradeDirection)
                .userId(state.userId)
                .waveId(state.waveId)
                .security(
                        SecurityBuilder.startBuilding().assetType(state.assetType)
                                .instrumentId(state.instrumentId).name(state.securityName).buildImmutable())

                .buildImmutable();
    }


    @Benchmark
    public Wave testJournalable(State state) {
        // This is a demo/sample template for building your JMH benchmarks. Edit as needed.
        // Put your benchmark code here.
        return WaveBuilder.startBuilding()
                .orderId(state.orderId)
                .quantity(state.quantity)
                .tradeDate(state.tradeDate)
                .tradeDirection(state.tradeDirection)
                .userId(state.userId)
                .waveId(state.waveId)
                .security(
                        SecurityBuilder.startBuilding().assetType(state.assetType)
                                .instrumentId(state.instrumentId).name(state.securityName).buildJournalable())

                .buildJournalable();
    }

    int poolSize = 0;


    @org.openjdk.jmh.annotations.State(Scope.Thread)
    public static class State {
        OrderId orderId = OrderId.from("test");
        int quantity = 12;
        LocalDate tradeDate = LocalDate.now();
        TradeDirection tradeDirection = TradeDirection.BUY;
        UserId userId = UserId.from("testUser");
        WaveId waveId = WaveId.from("testWave");
        AssetType assetType = AssetType.ETF;
        InstrumentId instrumentId = InstrumentId.from("inst");
        String securityName = "test";
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + WaveCreationBenchmark.class.getSimpleName() + ".*")
                .addProfiler(GCProfiler.class)
                .jvmArgsAppend("-XX:+PrintCompilation", "-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
                .build();

        new org.openjdk.jmh.runner.Runner(opt).run();
    }


}
