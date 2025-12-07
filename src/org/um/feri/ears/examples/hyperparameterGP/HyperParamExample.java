package org.um.feri.ears.examples.hyperparameterGP;

import org.um.feri.ears.algorithms.GPAlgorithm;
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

    //Define terminal node types
    private static final List<Class<? extends Node>> baseTerminalNodeTypes = Arrays.asList(
            ConstNode.class,
            PiNode.class,
            VarNode.class
    );

    private static final List<Class<? extends Node>> minimalTerminalNodeTypes = Arrays.asList(
            ConstNode.class,
            // PiNode.class,
            VarNode.class
    );

    public static void main(String[] args) {
        // Variables for synthetic problems
        List<GPProblemWrapper> problems;
        try {
            problems = Arrays.asList(
                    // Example problems
                    // new SyntheticProblem("Eq4", x -> Math.pow(x,3) + Math.pow(x,2) + x, -1, 1, 0.1, true),
                    // new SyntheticProblem("Eq10", Math::sqrt, 0, 4, 0.2, false),
                    // new SyntheticProblem("Eq29", x -> 0.3 * x * Math.sin(2 * Math.PI * x), -1, 1, 0.001, false)
                    new CSVProblem("Real Estate", "test_data/realEstate.csv", List.of("X1","X2","X3","X4","X5","X6"), 0.2)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Define different function sets to test
        List<List<Class<? extends Node>>> functionSets = Arrays.asList(
                baseFunctionNodeTypes,
                expandedFunctionNodeTypes
        );

        // Define different terminal sets to test
        List<List<Class<? extends Node>>> terminalSets = Arrays.asList(
                baseTerminalNodeTypes,
                minimalTerminalNodeTypes
        );

        // For each combination of function and terminal nodes
        HyperparamSearch.gridSearch(
                new ArrayList<>(problems),
                functionSets,
                terminalSets,
                ElitismGPAlgorithm::new,
                5
        );
    }
}
