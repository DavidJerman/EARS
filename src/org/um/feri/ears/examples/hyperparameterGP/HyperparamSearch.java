package org.um.feri.ears.examples.hyperparameterGP;

import org.um.feri.ears.algorithms.GPAlgorithm;
import org.um.feri.ears.individual.representations.gp.Node;
import org.um.feri.ears.problems.gp.ProgramSolution;

import java.util.List;
import java.util.function.Supplier;

public class HyperparamSearch {
    private static String setToString(List<Class<? extends Node>> nodeSet) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Class<? extends Node> clazz : nodeSet) {
            sb.append(clazz.getSimpleName());
            sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static void gridSearch(List<GPProblemWrapper> problems,
                                  List<List<Class<? extends Node>>> functionSets,
                                  List<List<Class<? extends Node>>> terminalSets,
                                  Supplier<GPAlgorithm> algorithmSupplier,
                                  int repeats) {

        for (GPProblemWrapper problem : problems) {
            System.out.println("\n== Problem: " + problem.getName() + " ==");

            for (List<Class<? extends Node>> funcSet : functionSets) {
                for (List<Class<? extends Node>> terminalSet : terminalSets) {
                    System.out.println("\n> Function set: " + setToString(funcSet) + " Terminal set: " + setToString(terminalSet));

                    double[] trainResults = new double[repeats];
                    double[] testResults = new double[repeats];

                    for (int i = 0; i < repeats; i++) {
                        problem.setup(); // reset problem

                        GPAlgorithm alg = algorithmSupplier.get();

                        // If your problem allows dynamic operator replacement:
                        if (problem instanceof SyntheticProblem sp) {
                            sp.setFunctionNodes(funcSet);
                            sp.setTerminalNodes(terminalSet);
                        } else if (problem instanceof CSVProblem cp) {
                            cp.setFunctionNodes(funcSet);
                            cp.setTerminalNodes(terminalSet);
                        }

                        // Needs to be called after changing operators
                        problem.setup();

                        GPResult result = problem.runGP(alg, false);

                        trainResults[i] = result.trainEval;
                        testResults[i] = result.testEval;
                    }

                    double trainMean = mean(trainResults);
                    double trainStd = std(trainResults, trainMean);
                    double trainMin = min(trainResults);
                    double trainMax = max(trainResults);

                    double testMean = mean(testResults);
                    double testStd = std(testResults, testMean);
                    double testMin = min(testResults);
                    double testMax = max(testResults);

                    System.out.printf("Train: %.6f ± %.6f, Min %.6f, Max %.6f\nTest: %.6f ± %.6f, Min %.6f, Max %.6f\n",
                            trainMean, trainStd, trainMin, trainMax, testMean, testStd, testMin, testMax);
                }
            }
        }
    }

    private static double mean(double[] arr) {
        double sum = 0;
        for (double d : arr) sum += d;
        return sum / arr.length;
    }

    private static double std(double[] arr, double mean) {
        double sum = 0;
        for (double d : arr) sum += (d - mean) * (d - mean);
        return Math.sqrt(sum / arr.length);
    }

    private static double min(double[] arr) {
        double min = Double.POSITIVE_INFINITY;
        for (double d : arr) min = Math.min(min, d);
        return min;
    }

    private static double max(double[] arr) {
        double max = Double.NEGATIVE_INFINITY;
        for (double d : arr) max = Math.max(max, d);
        return max;
    }
}
