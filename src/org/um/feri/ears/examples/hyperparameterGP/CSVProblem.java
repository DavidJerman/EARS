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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CSVProblem implements GPProblemWrapper {
    String name;

    List<String> variables;

    List<Target> trainData = new ArrayList<>();
    List<Target> testData = new ArrayList<>();
    SymbolicRegressionProblem trainProblem;
    SymbolicRegressionProblem testProblem;

    // Dynamic function nodes
    List<Class<? extends Node>> functionNodes;
    List<Class<? extends Node>> terminalNodes;

    public CSVProblem(String name, String csvPath, List<String> variables, double testFraction) throws IOException {
        this.name = name;
        this.variables = variables;

        generateData(csvPath, testFraction);
    }

    private void generateData(String csvPath, double testFraction) throws IOException {
        trainData = new ArrayList<>();
        testData = new ArrayList<>();

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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setup() {
        VarNode.variables = variables;
        // Data should already be generated in the constructor
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

        // Try running up to 3 times
        int c = 0;
        while (true)
        {
            if (c == 3)
                return new GPResult(Double.NaN, Double.NaN);

            try {
                ProgramSolution solution = algorithm.execute(task);

                double trainEval = solution.getEval();  // fitness on training data
                testProblem.evaluate(solution);          // evaluate on test data
                double testEval = solution.getEval();   // fitness on test data

                if (visualize) solution.getTree().displayTree(this.name, true);

                return new GPResult(trainEval, testEval);
            } catch (RuntimeException | StopCriterionException e) {
                System.out.println("Encountered an error while running GP, retrying");
            } finally {
                c++;
            }
        }
    }
}