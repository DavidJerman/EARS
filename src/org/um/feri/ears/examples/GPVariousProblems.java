package org.um.feri.ears.examples;

import org.um.feri.ears.algorithms.GPAlgorithm;
import org.um.feri.ears.algorithms.gp.ElitismGPAlgorithm;
import org.um.feri.ears.individual.representations.gp.Node;
import org.um.feri.ears.individual.representations.gp.Target;
import org.um.feri.ears.individual.representations.gp.symbolic.regression.*;
import org.um.feri.ears.problems.StopCriterion;
import org.um.feri.ears.problems.StopCriterionException;
import org.um.feri.ears.problems.Task;
import org.um.feri.ears.problems.gp.ProgramProblem;
import org.um.feri.ears.problems.gp.ProgramSolution;
import org.um.feri.ears.problems.gp.SymbolicRegressionProblem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static java.lang.Math.abs;
import static java.lang.Math.random;

public class GPVariousProblems {

    //Define base function node types
    private static final List<Class<? extends Node>> baseFunctionNodeTypes = Arrays.asList(
            AddNode.class,
            DivNode.class,
            MulNode.class,
            SubNode.class
    );

    //Define base terminal node types
    private static final List<Class<? extends Node>> baseTerminalNodeTypes = Arrays.asList(
            ConstNode.class,
            PiNode.class,
            VarNode.class
    );

    //Define base function node types
    private static final List<Class<? extends Node>> expandedFunctionNodeTypes = Arrays.asList(
            AddNode.class,
            DivNode.class,
            MulNode.class,
            SubNode.class,
            // Additional nodes
            SafePowNode.class,
            SqrtNode.class,
            Log10Node.class,
            AbsNode.class
    );

    //Define base terminal node types
    private static final List<Class<? extends Node>> minimalTerminalNodeTypes = Arrays.asList(
            ConstNode.class,
            // PiNode.class,
            VarNode.class
    );


    static class SyntheticProblem {
        String name;
        Function<Double, Double> func;
        double start;
        double end;
        double step;

        List<Target> trainData;
        List<Target> testData;
        SymbolicRegressionProblem trainProblem;
        SymbolicRegressionProblem testProblem;

        public SyntheticProblem(String name, Function<Double, Double> func, double start, double end, double step, Boolean randomSampling) {
            this.name = name;
            this.func = func;
            this.start = start;
            this.end = end;
            this.step = step;

            generateData(randomSampling);
        }

        private void generateData(Boolean randomSampling)
        {
            trainData = new ArrayList<>();
            testData = new ArrayList<>();

            if (randomSampling)
            {
                int count = (int) (abs(end - start) / step);

                // Training and test data
                for (int i = 0; i < count; i++)
                {
                    double x = random() * (end - start) + start;
                    double y = func.apply(x);
                    trainData.add(new Target().when("x", x).targetIs(y));

                    // More data for test
                    for (int j = 0; j < 10; ++j)
                    {
                        x = random() * (end - start) + start;
                        y = func.apply(x);
                        testData.add(new Target().when("x", x).targetIs(y));
                    }
                }

            }
            else
            {
                double x = start;

                // Training data
                while (x <= end) {
                    double y = func.apply(x);
                    trainData.add(new Target().when("x", x).targetIs(y));
                    x += step;
                }

                // Test data
                x = start;
                double testStep = step / 10.0;
                while (x <= end) {
                    double y = func.apply(x);
                    testData.add(new Target().when("x", x).targetIs(y));
                    x += testStep;
                }

            }

            trainProblem = new SymbolicRegressionProblem(baseFunctionNodeTypes, baseTerminalNodeTypes, trainData);
            testProblem = new SymbolicRegressionProblem(baseFunctionNodeTypes, baseTerminalNodeTypes, testData);
        }

        public void runGP(GPAlgorithm algorithm, Boolean visualize)
        {
            Task<ProgramSolution, ProgramProblem> task = new Task<>(trainProblem, StopCriterion.EVALUATIONS, 10000, 0, 0);
            try {
                ProgramSolution solution = algorithm.execute(task);
                System.out.println("=== " + name + " ===");
                System.out.println("Fitness on Training -> " + solution.getEval());
                if (visualize) solution.getTree().displayTree(this.name, true);
                System.out.println(solution);
                testProblem.evaluate(solution);
                System.out.println("Fitness on Testing -> " + solution.getEval());
            } catch (StopCriterionException e) {
                e.printStackTrace();
            }
        }
    }

    static class CSVProblem {
        String name;
        List<String> variables;
        List<Target> trainData = new ArrayList<>();
        List<Target> testData = new ArrayList<>();
        SymbolicRegressionProblem trainProblem;
        SymbolicRegressionProblem testProblem;

        public CSVProblem(String name, String csvPath, List<String> variables, double testFraction) throws IOException {
            this.name = name;
            this.variables = variables;

            List<Map<String, Double>> rows = readCSV(csvPath, variables);

            // Split into train/test
            int splitIndex = (int) (rows.size() * (1 - testFraction));
            for (int i = 0; i < rows.size(); i++) {
                Map<String, Double> row = rows.get(i);
                Target t = new Target();
                for (String var : variables) t.when(var, row.get(var));
                t.targetIs(row.get("y")); // assuming last column is target named "y"
                if (i < splitIndex) trainData.add(t);
                else testData.add(t);
            }

            trainProblem = new SymbolicRegressionProblem(expandedFunctionNodeTypes, baseTerminalNodeTypes, trainData);
            testProblem = new SymbolicRegressionProblem(expandedFunctionNodeTypes, baseTerminalNodeTypes, testData);
        }

        private List<Map<String, Double>> readCSV(String path, List<String> vars) throws IOException {
            List<Map<String, Double>> data = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            // skip header
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                Map<String, Double> row = new HashMap<>();
                for (int i = 0; i < vars.size(); i++) {
                    row.put(vars.get(i), Double.parseDouble(tokens[i]));
                }
                row.put("y", Double.parseDouble(tokens[tokens.length - 1]));
                data.add(row);
            }
            br.close();
            return data;
        }

        public void runGP(GPAlgorithm algorithm, Boolean visualize) {
            Task<ProgramSolution, ProgramProblem> task = new Task<>(trainProblem, StopCriterion.EVALUATIONS, 50000, 0, 0);
            try {
                ProgramSolution solution = algorithm.execute(task);
                System.out.println("=== " + name + " ===");
                System.out.println("Fitness on Training -> " + solution.getEval());
                if (visualize) solution.getTree().displayTree(this.name, true);
                System.out.println(solution);
                testProblem.evaluate(solution);
                System.out.println("Fitness on Testing -> " + solution.getEval());
            } catch (StopCriterionException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        GPAlgorithm alg = new ElitismGPAlgorithm();

        // Sintetični problemi (iz primera)
        VarNode.variables = List.of("x");

        List<SyntheticProblem> problems = Arrays.asList(
                // Primeri iz SymbolicRegressionExample.java
                new SyntheticProblem("Eq4", x -> Math.pow(x,3) + Math.pow(x,2) + x, -1, 1, 0.1, true),
                new SyntheticProblem("Eq10", Math::sqrt, 0, 4, 0.2, false),
                new SyntheticProblem("Eq29", x -> 0.3 * x * Math.sin(2 * Math.PI * x), -1, 1, 0.001, false)
        );

        for (SyntheticProblem p : problems) {
            p.runGP(alg, false);
        }

        // Realni problem - Real Estate
        VarNode.variables = List.of("X1","X2","X3","X4","X5","X6");

        CSVProblem realEstate;
        try {
            realEstate = new CSVProblem(
                    "RealEstate",
                    "test_data/realEstate.csv",
                    List.of("X1","X2","X3","X4","X5","X6"),
                    0.2                      // 20% test
            );
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        alg = new ElitismGPAlgorithm();
        realEstate.runGP(alg, true);
    }
}