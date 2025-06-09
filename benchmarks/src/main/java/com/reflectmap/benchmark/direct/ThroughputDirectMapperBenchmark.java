package com.reflectmap.benchmark.direct;

import com.reflectmap.benchmark.AbstractBenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ThroughputDirectMapperBenchmark extends AbstractBenchmark {

    @Benchmark
    public void map() {
        DirectMapper.map(src, dst);
    }

}