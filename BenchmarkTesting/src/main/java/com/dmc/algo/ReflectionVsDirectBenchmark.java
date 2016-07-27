package com.dmc.algo;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Created By davidclelland on 14/06/2016.
 */
@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 2, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 2)
public class ReflectionVsDirectBenchmark {

    int i = 0;

    public int a() {
        i += 1;
        return i;
    }

    @Benchmark
    public int directCalcTest() {
        return a();

    }

    @Benchmark
    public Object reflectiveCalcTest() throws Exception {


        Method f = this.getClass().getMethod("a");

        return f.invoke(this);

    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + ReflectionVsDirectBenchmark.class.getSimpleName() + ".*")
                //.addProfiler(GCProfiler.class)
               // .jvmArgsAppend("-XX:+PrintCompilation", "-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
                .build();

        new org.openjdk.jmh.runner.Runner(opt).run();
    }



}
