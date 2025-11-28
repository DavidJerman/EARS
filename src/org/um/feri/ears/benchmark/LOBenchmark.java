package org.um.feri.ears.benchmark;

import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.problems.*;
import org.um.feri.ears.problems.unconstrained.*;
import org.um.feri.ears.problems.unconstrained.cec2015.*;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class LOBenchmark extends SOBenchmark<NumberSolution<Double>, NumberSolution<Double>, DoubleProblem, NumberAlgorithm> {
    protected boolean calculateTime = false;
    protected int warmupIterations = 10000;
    private double optimumEpsilon = 0.000001;

    public LOBenchmark() {
        this(1e-7);
    }

    public LOBenchmark(double drawLimit) {
        super();
        name = "Custom LO Benchmark";
        this.drawLimit = drawLimit;
        maxEvaluations = 3000000;
        dimension = 30;
        timeLimit = 2500;
        maxIterations = 100000;
        stopCriterion = StopCriterion.EVALUATIONS;
    }

    @Override
    protected void addTask(DoubleProblem problem, StopCriterion stopCriterion, int maxEvaluations, long time, int maxIterations) {
        tasks.add(new Task<>(problem, stopCriterion, maxEvaluations, time, maxIterations));
    }

    @Override
    public int getNumberOfRuns() {
        //number of runs set to 10 to reduce server execution time
        return 10;
    }

    @Override
    public void initAllProblems() {

        ArrayList<DoubleProblem> problems = new ArrayList<>();

        // Problems added based on Get_Function_details.m (same as in the article)
        problems.add(new Sphere(dimension));
        problems.add(new Schwefel222(dimension));
        problems.add(new Schwefel12(dimension));
        // problems.add(new Schwefel221());   // V clanku je uporabljena dimenzija 30
        problems.add(new RosenbrockD2a(dimension));
        problems.add(new Step2(dimension));
        // Ne najdem noise funkcije
        problems.add(new Schwefel226(dimension));  // Generalised Schwefel
        problems.add(new Rastrigin(dimension));
        problems.add(new Ackley1(dimension));
        problems.add(new Griewank(dimension));
        problems.add(new Penalized(dimension));
        problems.add(new Penalized2(dimension));
        problems.add(new Foxholes());
        problems.add(new Kowalik());
        problems.add(new SixHumpCamelBack());
        problems.add(new Branin1());
        problems.add(new GoldsteinPrice());
        problems.add(new Hartman3());
        problems.add(new Hartman6());
        problems.add(new Shekel5());
        problems.add(new Shekel7());
        problems.add(new Shekel10());

        for (DoubleProblem p : problems) {
            if (stopCriterion == StopCriterion.CPU_TIME) {
                for (int i = 0; i < warmupIterations; i++) {
                    p.getRandomEvaluatedSolution();
                }
            }

            addTask(p, stopCriterion, maxEvaluations, timeLimit, maxIterations);
        }
    }

    private long calculateTime(DoubleProblem p) {

        long start = System.nanoTime();
        long duration;
        for (int i = 0; i < maxEvaluations; i++) {
            p.getRandomEvaluatedSolution();
        }
        duration = System.nanoTime() - start;
        // add algorithm runtime
        duration += (int) (duration * (10.0f / 100.0f));

        return TimeUnit.NANOSECONDS.toMillis(duration);
    }
}
