package org.um.feri.ears.examples;

import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.algorithms.so.abc.ABC;
import org.um.feri.ears.algorithms.so.ba.BA;
import org.um.feri.ears.algorithms.so.fpa.FPA;
import org.um.feri.ears.algorithms.so.lo.LO;
import org.um.feri.ears.algorithms.so.random.RandomWalkAlgorithm;
import org.um.feri.ears.benchmark.Benchmark;
import org.um.feri.ears.benchmark.LOBenchmark;

import java.util.ArrayList;

public class LOBenchmarkCustom {

    public static void main(String[] args) {
        Benchmark.printInfo = true; //prints one on one results
        //add algorithms to a list

        ArrayList<NumberAlgorithm> algorithms = new ArrayList<NumberAlgorithm>();
        algorithms.add(new RandomWalkAlgorithm());  // Dodan za referenco
        algorithms.add(new LO());
        algorithms.add(new ABC());
        algorithms.add(new FPA());
        algorithms.add(new BA());
        // Ostali algoritmi: LEP-MPA, JAYA, SCA, SSA 0 jih nisem našel v EARS

        LOBenchmark loBenchmark = new LOBenchmark(); // benchmark with prepared tasks and settings

        loBenchmark.addAlgorithms(algorithms);  // register the algorithms in the benchmark

        loBenchmark.run(10); //start the tournament with 10 runs/repetitions
    }
}
