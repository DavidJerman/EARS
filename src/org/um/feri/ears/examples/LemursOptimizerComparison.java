package org.um.feri.ears.examples;

import org.um.feri.ears.algorithms.so.lo.LO;
import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.StopCriterion;
import org.um.feri.ears.problems.Task;
import org.um.feri.ears.problems.unconstrained.Sphere;
import org.um.feri.ears.util.random.RNG;

public class LemursOptimizerComparison {
    public static void main(String[] args) {

        DoubleProblem problem = new Sphere(2);

        // Debug Mode ON
        LO lo = new LO(5, true);

        Task task = new Task(problem, StopCriterion.ITERATIONS, 50, 10000, 10000);

        // Using predefined random numbers for comparison
        RNG.setSelectedRandomGenerator(RNG.RngType.PREDEFINED_RANDOM);

        try {
            System.out.println("Starting Java LO with Predefined Randoms...");
            lo.execute(task);
            System.out.println("Final Java Result: " + lo.bestSolution.getObjective(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
