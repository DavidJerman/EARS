package org.um.feri.ears.algorithms.so.lo;

import org.um.feri.ears.algorithms.AlgorithmInfo;
import org.um.feri.ears.algorithms.Author;
import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.problems.*;
import org.um.feri.ears.util.Util;
import org.um.feri.ears.util.annotation.AlgorithmParameter;
import org.um.feri.ears.util.comparator.ProblemComparator;
import org.um.feri.ears.util.random.RNG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * This class implements the Lemurs Optimizer (LO).
 * The implementation is based on the MATLAB file LO.m provided by the user.
 * <p>
 * Reference:
 * Ammar Kamal Abasi, Sharif Naser Makhadmeh, et al.
 * "Lemurs Optimizer: A New Metaheuristic Algorithm for Global Optimization (LO)"
 * Published in Journal of "Applied Sciences" (2022)
 *
 */
public class LO extends NumberAlgorithm {

    @AlgorithmParameter(name = "population size")
    private final int popSize;

    @AlgorithmParameter(name = "jumping rate min")
    private final double jumping_rate_min = 0.1;

    @AlgorithmParameter(name = "jumping rate max")
    private final double jumping_rate_max = 0.5;

    private ArrayList<NumberSolution<Double>> population; // Swarm
    private NumberSolution<Double> bestSolution; // Best solution found

    public LO() {
        this(30);
    }

    public LO(int popSize) {
        super();
        this.popSize = popSize;

        // Updated author and algorithm info based on LO.m
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
                        + "  ABSTRACT = {The Lemur Optimizer (LO) is a novel nature-inspired algorithm we propose in this paper. This algorithm’s primary inspirations are based on two pillars of lemur behavior: leap up and dance hub. These two principles are mathematically modeled in the optimization context to handle local search, exploitation, and exploration search concepts. The LO is first benchmarked on twenty-three standard optimization functions. Additionally, the LO is used to solve three real-world problems to evaluate its performance and effectiveness. In this direction, LO is compared to six well-known algorithms: Salp Swarm Algorithm (SSA), Artificial Bee Colony (ABC), Sine Cosine Algorithm (SCA), Bat Algorithm (BA), Flower Pollination Algorithm (FPA), and JAYA algorithm. The findings show that the proposed algorithm outperforms these algorithms in fourteen standard optimization functions and proves the LO’s robust performance in managing its exploration and exploitation capabilities, which significantly leads LO towards the global optimum. The real-world experimental findings demonstrate how LO may tackle such challenges competitively.},"
                        + "  DOI = {10.3390/app121910057}}"
        );
    }

    @Override
    public NumberSolution<Double> execute(Task<NumberSolution<Double>, DoubleProblem> task) throws StopCriterionException {
        this.task = task;
        initPopulation(); // Initialize swarm
        int maxIt = 10000;

        if (task.getStopCriterion() == StopCriterion.ITERATIONS) {
            maxIt = task.getMaxIterations();
        }
        if (task.getStopCriterion() == StopCriterion.EVALUATIONS) {
            maxIt = (task.getMaxEvaluations() - popSize) / popSize;
        }

        int dim = task.problem.getNumberOfDimensions();

        while (!task.isStopCriterion()) {

            // Main logic goes here

            task.incrementNumberOfIterations();
        }

        return bestSolution;
    }

    /**
     * Initializes the population (swarm) based on initialization.m.
     */
    private void initPopulation() throws StopCriterionException {
        population = new ArrayList<>();

        int dim = task.problem.getNumberOfDimensions();

        List<Double> lb = task.problem.getLowerLimit();
        List<Double> ub = task.problem.getUpperLimit();

        // TODO: Actually init the population
    }

    /**
     * Translates the logic from calculateFitness.m.
     * This converts a minimization objective value into a maximization fitness score.
     */
    private double calculateFitness(double objV) {
        if (objV >= 0) {
            return 1.0 / (objV + 1.0); //
        } else {
            return 1.0 + Math.abs(objV); //
        }
    }

    /**
     * Finds the best solution in the current population and updates
     * the global 'bestSolution' if a new best is found.
     * This replaces the MATLAB line `[cost, best_solution_Index] = min(ObjVal);`
     */
    private void updateBest() {
        // We can sort or iterate; iterating is slightly cheaper
        NumberSolution<Double> currentBest = population.get(0);

        // TODO: Update best
    }

    @Override
    public void resetToDefaultsBeforeNewRun() {
        population = null;
        bestSolution = null;
    }
}
