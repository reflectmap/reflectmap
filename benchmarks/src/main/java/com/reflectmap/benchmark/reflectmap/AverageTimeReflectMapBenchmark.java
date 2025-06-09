package com.reflectmap.benchmark.reflectmap;

import com.reflectmap.ReflectMap;
import com.reflectmap.benchmark.AbstractBenchmark;
import com.reflectmap.mock.Destination25;
import com.reflectmap.mock.Source25;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class AverageTimeReflectMapBenchmark extends AbstractBenchmark {

    @Benchmark
    public void map() {
        ReflectMap.map(src, Source25.class, dst, Destination25.class);
    }

}