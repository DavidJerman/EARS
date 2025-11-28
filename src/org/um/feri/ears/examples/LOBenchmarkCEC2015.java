package org.um.feri.ears.examples;

import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.algorithms.so.abc.ABC;
import org.um.feri.ears.algorithms.so.gwo.GWO;
import org.um.feri.ears.algorithms.so.jade.JADE;
import org.um.feri.ears.algorithms.so.lo.LO;
import org.um.feri.ears.algorithms.so.pso.PSO;
import org.um.feri.ears.algorithms.so.random.RandomWalkAlgorithm;
import org.um.feri.ears.benchmark.Benchmark;
import org.um.feri.ears.benchmark.CEC2015Benchmark;

import java.util.ArrayList;

public class LOBenchmarkCEC2015 {

    public static void main(String[] args) {
        Benchmark.printInfo = true; //prints one on one results
        //add algorithms to a list

        ArrayList<NumberAlgorithm> algorithms = new ArrayList<NumberAlgorithm>();
        algorithms.add(new ABC());
        algorithms.add(new GWO());
        algorithms.add(new PSO());
        algorithms.add(new RandomWalkAlgorithm());
        algorithms.add(new JADE());
        algorithms.add(new LO());

        CEC2015Benchmark cec2015 = new CEC2015Benchmark(); // benchmark with prepared tasks and settings

        cec2015.addAlgorithms(algorithms);  // register the algorithms in the benchmark

        cec2015.run(10); //start the tournament with 10 runs/repetitions
    }
}
