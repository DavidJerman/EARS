package org.um.feri.ears.examples.hyperparameterGP;

import org.um.feri.ears.algorithms.GPAlgorithm;
import org.um.feri.ears.individual.representations.gp.Node;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
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

    private static void writeCsv(String problem,
                                 List<Class<? extends Node>> funcSet,
                                 List<Class<? extends Node>> terminalSet,
                                 double trainMean, double trainStd, double trainMin, double trainMax,
                                 double testMean, double testStd, double testMin, double testMax) {

        File file = new File("grid_results.csv");
        boolean writeHeader = !file.exists();

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            if (writeHeader) {
                pw.println("Problem,FunctionSet,TerminalSet,TrainMean,TrainStd,TrainMin,TrainMax,TestMean,TestStd,TestMin,TestMax");
            }

            pw.printf("%s,\"%s\",\"%s\",%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f%n",
                    problem,
                    setToString(funcSet),
                    setToString(terminalSet),
                    trainMean, trainStd, trainMin, trainMax,
                    testMean, testStd, testMin, testMax);
        } catch (Exception ignored) {
        }
    }

    private static void computeStatistics(String problem,
                                          List<Class<? extends Node>> funcSet,
                                          List<Class<? extends Node>> terminalSet,
                                          double[] trainResults, double[] testResults
    ) {
        double[] tr = iqrFilter(trainResults);
        double[] te = iqrFilter(testResults);

        double trainMean = mean(tr);
        double trainStd = std(tr, trainMean);
        double trainMin = min(tr);
        double trainMax = max(tr);

        double testMean = mean(te);
        double testStd = std(te, testMean);
        double testMin = min(te);
        double testMax = max(te);

        System.out.printf("Train: %.6f ± %.6f, Min %.6f, Max %.6f\nTest: %.6f ± %.6f, Min %.6f, Max %.6f\n",
                trainMean, trainStd, trainMin, trainMax, testMean, testStd, testMin, testMax);

        writeCsv(problem,
                 funcSet, terminalSet,
                 trainMean, trainStd, trainMin, trainMax,
                 testMean, testStd, testMin, testMax);
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

                    computeStatistics(problem.getName(), funcSet, terminalSet, trainResults, testResults);
                }
            }
        }
    }

    // This is for outlier removal
    private static double[] iqrFilter(double[] arr) {
        double[] a = arr.clone();
        java.util.Arrays.sort(a);
        int n = a.length;

        double q1 = a[n / 4];
        double q3 = a[(3 * n) / 4];
        double iqr = q3 - q1;

        double lower = q1 - 1.5 * iqr;
        double upper = q3 + 1.5 * iqr;

        return java.util.Arrays.stream(a)
                .filter(v -> v >= lower && v <= upper)
                .toArray();
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
