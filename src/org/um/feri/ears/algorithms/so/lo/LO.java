package org.um.feri.ears.algorithms.so.lo;

import org.um.feri.ears.algorithms.AlgorithmInfo;
import org.um.feri.ears.algorithms.Author;
import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.problems.*;
import org.um.feri.ears.util.Util;
import org.um.feri.ears.util.annotation.AlgorithmParameter;
import org.um.feri.ears.util.random.RNG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class LO extends NumberAlgorithm {

    @AlgorithmParameter(name = "population size")
    private final int popSize;

    @AlgorithmParameter(name = "jumping rate min")
    private final double jumping_rate_min;

    @AlgorithmParameter(name = "jumping rate max")
    private final double jumping_rate_max;

    @AlgorithmParameter(name = "debug mode")
    private boolean isDebug;

    @AlgorithmParameter(name = "max iterations")
    private int maxIt;

    private ArrayList<NumberSolution<Double>> population;
    public NumberSolution<Double> bestSolution; // Global best

    public LO() {
        this(30);
    }

    public LO(int popSize) {
        super();
        this.popSize = popSize;
        this.maxIt = 10000;
        this.jumping_rate_min = 0.5; // Parameters used in the paper
        this.jumping_rate_max = 0.7; // Parameters used in the paper

        au = new Author("Ammar Kamal Abasi et al.", "");
        ai = new AlgorithmInfo("LO", "Lemurs Optimizer",
                "@Article{app121910057,"
                        + "  AUTHOR = {Abasi, Ammar Kamal and Makhadmeh, Sharif Naser and Al-Betar, Mohammed Azmi and Alomari, Osama Ahmad and Awadallah, Mohammed A. and Alyasseri, Zaid Abdi Alkareem and Doush, Iyad Abu and Elnagar, Ashraf and Alkhammash, Eman H. and Hadjouni, Myriam},"
                        + "  TITLE = {Lemurs Optimizer: A New Metaheuristic Algorithm for Global Optimization},"
                        + "  JOURNAL = {Applied Sciences},"
                        + "  VOLUME = {12},"
                        + "  YEAR = {2022},"
                        + "  NUMBER = {19},"
                        + "  ARTICLE-NUMBER = {10057},"
                        + "  URL = {https://www.mdpi.com/2076-3417/12/19/10057},"
                        + "  ISSN = {2076-3417},"
                        + "  DOI = {10.3390/app121910057}}"
        );
    }

    public LO(int popSize, boolean debug, int maxIter) {
        this.isDebug = debug;
        this.popSize = popSize;
        this.maxIt = maxIter;
        this.jumping_rate_min = 0.1;
        this.jumping_rate_max = 0.5;  // Debug version (used for comparison) uses default matlab parameters

        au = new Author("Ammar Kamal Abasi et al.", "");
        ai = new AlgorithmInfo("LO", "Lemurs Optimizer",
                "@Article{app121910057,"
                        + "  AUTHOR = {Abasi, Ammar Kamal and Makhadmeh, Sharif Naser and Al-Betar, Mohammed Azmi and Alomari, Osama Ahmad and Awadallah, Mohammed A. and Alyasseri, Zaid Abdi Alkareem and Doush, Iyad Abu and Elnagar, Ashraf and Alkhammash, Eman H. and Hadjouni, Myriam},"
                        + "  TITLE = {Lemurs Optimizer: A New Metaheuristic Algorithm for Global Optimization},"
                        + "  JOURNAL = {Applied Sciences},"
                        + "  VOLUME = {12},"
                        + "  YEAR = {2022},"
                        + "  NUMBER = {19},"
                        + "  ARTICLE-NUMBER = {10057},"
                        + "  URL = {https://www.mdpi.com/2076-3417/12/19/10057},"
                        + "  ISSN = {2076-3417},"
                        + "  DOI = {10.3390/app121910057}}"
        );
    }

    @Override
    public NumberSolution<Double> execute(Task<NumberSolution<Double>, DoubleProblem> task) throws StopCriterionException {
        this.task = task;
        initPopulation();

        // =======================================================
        // DEBUG: Print Initial Swarm for MATLAB Comparison
        // =======================================================
        if (isDebug) {
            System.out.println("--------------------------------------------------");
            System.out.println("INITIAL SWARM:");
            for (int i = 0; i < popSize; i++) {
                System.out.print("Agent " + i + ": [");
                for (int j = 0; j < task.problem.getNumberOfDimensions(); j++) {
                    if (j > 0) {
                        System.out.print(", ");
                    }
                    System.out.printf("%.16f", population.get(i).getValue(j));
                }
                System.out.println("]");
            }
            System.out.println("--------------------------------------------------");
        }

        // Max interations
        if (task.getStopCriterion() == StopCriterion.ITERATIONS) {
            maxIt = task.getMaxIterations();
        } else if (task.getStopCriterion() == StopCriterion.EVALUATIONS) {
            maxIt = (task.getMaxEvaluations() - popSize) / popSize;
        }

        while (!task.isStopCriterion()) {

            // Compute the jumping rate
            double currentIter = task.getNumberOfIterations();
            /// jumping_rate = jumping_rate_max - itr * ((jumping_rate_max - jumping_rate_min) / Max_iter);
            double jumping_rate = jumping_rate_max - currentIter * ((jumping_rate_max - jumping_rate_min) / maxIt);

            // Sort the population based on fitness
            /// [sorted_objctive, sorted_indexes] = sort(Fitness);
            double[] fitness = new double[popSize];
            Integer[] sortedIndices = new Integer[popSize];

            for (int i = 0; i < popSize; i++) {
                fitness[i] = calculateFitness(population.get(i).getObjective(0));
                sortedIndices[i] = i;
            }

            Arrays.sort(sortedIndices, Comparator.comparingDouble(i -> fitness[i]));

            // Best solution is already picked in initialize, so it is not done here

            // Main loop
            for (int i = 0; i < popSize; i++) {
                // Find rank of current solution
                /// current_solution = find(sorted_indexes == i);
                int currentRank = -1;
                for (int k = 0; k < popSize; k++) {
                    if (sortedIndices[k] == i) {
                        currentRank = k;
                        break;
                    }
                }

                // Determine "Near Solution" (neighbor in rank)
                /// near_solution_postion = current_solution - 1
                int nearSolutionRank = currentRank - 1;
                if (nearSolutionRank < 0) nearSolutionRank = 0; // Boundary check: if 0, stay 0

                NumberSolution<Double> currentSol = population.get(i);
                NumberSolution<Double> nearSol = population.get(sortedIndices[nearSolutionRank]);

                // Prepare new position array
                /// NewSol = swarm(i, :);
                double[] newPosition = new double[task.problem.getNumberOfDimensions()];

                // Dimension Loop (jumping logic)
                for (int j = 0; j < task.problem.getNumberOfDimensions(); j++) {
                    double r = RNG.nextDouble(); // rand()

                    double currentVal = currentSol.getValue(j);

                    if (r < jumping_rate) {
                        // Dance Hub (Local Search)
                        // NewSol(j) = swarm(i, j) + abs(swarm(i, j) - swarm(near_solution, j)) * (rand - 0.5) * 2;
                        double nearVal = nearSol.getValue(j);
                        newPosition[j] = currentVal + Math.abs(currentVal - nearVal) * (RNG.nextDouble() - 0.5) * 2;
                    } else {
                        // Leap Up (Global Search)
                        // NewSol(j) = swarm(i, j) + abs(swarm(i, j) - swarm(best_solution_Index, j)) * (rand - 0.5) * 2;
                        double bestVal = bestSolution.getValue(j);
                        newPosition[j] = currentVal + Math.abs(currentVal - bestVal) * (RNG.nextDouble() - 0.5) * 2;
                    }
                }

                // Boundary Handling
                // "manipulate range between lb and ub"
                task.problem.setFeasible(newPosition);

                if (task.isStopCriterion()) break;

                // Evaluation and Update
                NumberSolution<Double> newSol = new NumberSolution<>(Util.toDoubleArrayList(newPosition));
                task.eval(newSol);

                // Greedy Update: "if (ObjVal(i) > ObjValSol)"
                if (newSol.getObjective(0) < currentSol.getObjective(0)) {
                    population.set(i, newSol);

                    if (isDebug) {
                        System.out.println("Improvement found for particle " + i +
                                " at iter " + task.getNumberOfIterations() +
                                ". New Obj: " + newSol.getObjective(0));
                    }

                    // Update global best if the new individual is the best overall
                    if (newSol.getObjective(0) < bestSolution.getObjective(0)) {
                        bestSolution = newSol;
                        if (isDebug) {
                            System.out.println(">>> New Global Best: " + bestSolution.getObjective(0));
                        }
                    }
                }
            }

            task.incrementNumberOfIterations();
        }

        return bestSolution;
    }

    /**
     * Logic from intialization.m
     * Initializes the swarm
     */
    private void initPopulation() throws StopCriterionException {
        population = new ArrayList<>();
        for (int i = 0; i < popSize; i++) {
            if (task.isStopCriterion()) break;
            NumberSolution<Double> sol = task.getRandomEvaluatedSolution();
            population.add(sol);

            // Initialize best solution
            if (bestSolution == null || sol.getObjective(0) < bestSolution.getObjective(0)) {
                bestSolution = sol;
            }
        }
    }

    /**
     * Logic from calculateFitness.m
     * This transforms the objective value into a positive fitness score for ranking.
     */
    private double calculateFitness(double objVal) {
        if (objVal >= 0) {
            return 1.0 / (objVal + 1.0);
        } else {
            return 1.0 + Math.abs(objVal);
        }
    }

    @Override
    public void resetToDefaultsBeforeNewRun() {
        population = null;
        bestSolution = null;
    }
}