package org.um.feri.ears.examples.hyperparameterGP;

import org.jetbrains.annotations.NotNull;
import org.um.feri.ears.algorithms.gp.ElitismGPAlgorithm;
import org.um.feri.ears.individual.representations.gp.Node;
import org.um.feri.ears.individual.representations.gp.symbolic.regression.*;

import java.io.IOException;
import java.util.*;

public class HyperParamExample {

    //Define function node types
    private static final List<Class<? extends Node>> baseFunctionNodeTypes = Arrays.asList(
            AddNode.class,
            DivNode.class,
            MulNode.class,
            SubNode.class
    );

    private static final List<Class<? extends Node>> extraFunctionNodeTypes = Arrays.asList(
            SafePowNode.class,
            SqrtNode.class,
            Log10Node.class,
            AbsNode.class,
            SinNode.class
    );

    //Define terminal node types
    private static final List<Class<? extends Node>> baseTerminalNodeTypes = Arrays.asList(
            ConstNode.class,
            VarNode.class
    );

    private static final List<Class<? extends Node>> extraTerminalNodeTypes = Arrays.asList(
            PiNode.class,
            ENode.class
    );

    public static void main(String[] args) {
        // Variables for synthetic problems
        List<GPProblemWrapper> problems = getGPProblems();

        NodeListGenerator functionsGenerator = new NodeListGenerator(baseFunctionNodeTypes, extraFunctionNodeTypes);
        NodeListGenerator terminalsGenerator = new NodeListGenerator(baseTerminalNodeTypes, extraTerminalNodeTypes);

        // For each combination of function and terminal nodes
        HyperparamSearch.gridSearch(
                new ArrayList<>(problems),
                functionsGenerator.combinations,
                terminalsGenerator.combinations,
                ElitismGPAlgorithm::new,
                20
        );
    }

    @NotNull
    private static List<GPProblemWrapper> getGPProblems() {
        List<GPProblemWrapper> problems;
        try {
            problems = List.of(
                    // Example problems
                    // new SyntheticProblem("Eq4", x -> Math.pow(x,3) + Math.pow(x,2) + x, -1, 1, 0.1, true),
                    // new SyntheticProblem("Eq10", Math::sqrt, 0, 4, 0.2, false),
                    // new SyntheticProblem("Eq29", x -> 0.3 * x * Math.sin(2 * Math.PI * x), -1, 1, 0.001, false),

                    // New synthetic problems
                    new SyntheticProblem("SinCosExp",
                            x -> Math.sin(3*x) * Math.cos(2*x) + Math.exp(0.5 * x),
                            -2, 2, 0.05, false),
                    new SyntheticProblem("GaussMixture",
                            x ->
                                    0.4 * Math.exp(-Math.pow(x - 1.0, 2) / (2 * 0.2*0.2))
                                            + 0.6 * Math.exp(-Math.pow(x + 1.0, 2) / (2 * 0.5*0.5)),
                            -3, 3, 0.05, false),
                    new SyntheticProblem("RationalPeriodic",
                            x -> (x / (1 + x*x)) + 0.5 * Math.sin(5*x),
                            -4, 4, 0.1, false),

                    // Real life problem
//                    new CSVProblem("Real Estate",
//                            "test_data/realEstate.csv",
//                            List.of("X1", "X2", "X3", "X4", "X5", "X6"),
//                            0.2),

                    // Another real life problem
                    new CSVProblem("Building Energy Efficiency",
                            "test_data/energy_efficiency.csv",
                            List.of("X1", "X2", "X3", "X4", "X5", "X6", "X7", "X8"),
                            0.2)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return problems;
    }
}
