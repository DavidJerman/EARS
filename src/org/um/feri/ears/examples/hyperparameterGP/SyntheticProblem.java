package org.um.feri.ears.examples.hyperparameterGP;

import org.um.feri.ears.algorithms.GPAlgorithm;
import org.um.feri.ears.individual.representations.gp.Node;
import org.um.feri.ears.individual.representations.gp.Target;
import org.um.feri.ears.individual.representations.gp.symbolic.regression.VarNode;
import org.um.feri.ears.problems.StopCriterion;
import org.um.feri.ears.problems.StopCriterionException;
import org.um.feri.ears.problems.Task;
import org.um.feri.ears.problems.gp.ProgramProblem;
import org.um.feri.ears.problems.gp.ProgramSolution;
import org.um.feri.ears.problems.gp.SymbolicRegressionProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.lang.Math.abs;
import static java.lang.Math.random;

class SyntheticProblem implements GPProblemWrapper {
    String name;

    private List<String> variables;
    Function<Double, Double> func;

    double start;
    double end;
    double step;

    List<Target> trainData;
    List<Target> testData;
    SymbolicRegressionProblem trainProblem;
    SymbolicRegressionProblem testProblem;

    // Dynamic function nodes
    List<Class<? extends Node>> functionNodes;
    List<Class<? extends Node>> terminalNodes;

    public SyntheticProblem(String name, Function<Double, Double> func, double start, double end, double step, Boolean randomSampling) {
        this.name = name;
        this.func = func;
        this.start = start;
        this.end = end;
        this.step = step;
        this.variables = List.of("x");
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
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setup() {
        VarNode.variables = variables;
        generateData(true); // or false depending on your needs
        trainProblem = new SymbolicRegressionProblem(functionNodes, terminalNodes, trainData);
        testProblem = new SymbolicRegressionProblem(functionNodes, terminalNodes, testData);
    }

    public void setFunctionNodes(List<Class<? extends Node>> nodes) {
        this.functionNodes = nodes;
    }

    public void setTerminalNodes(List<Class<? extends Node>> nodes) {
        this.terminalNodes = nodes;
    }

    @Override
    public GPResult runGP(GPAlgorithm algorithm, Boolean visualize)
    {
        Task<ProgramSolution, ProgramProblem> task = new Task<>(trainProblem, StopCriterion.EVALUATIONS, 10000, 0, 0);
        try {
            ProgramSolution solution = algorithm.execute(task);

            double trainEval = solution.getEval();  // fitness on training data
            testProblem.evaluate(solution);          // evaluate on test data
            double testEval = solution.getEval();   // fitness on test data

            if (visualize) solution.getTree().displayTree(this.name, true);

            return new GPResult(trainEval, testEval);
        } catch (StopCriterionException e) {
            e.printStackTrace();
            return new GPResult(Double.NaN, Double.NaN);
        }
    }
}