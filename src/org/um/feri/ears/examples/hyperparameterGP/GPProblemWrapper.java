package org.um.feri.ears.examples.hyperparameterGP;

import org.um.feri.ears.algorithms.GPAlgorithm;

interface GPProblemWrapper {
    String getName();
    void setup();
    GPResult runGP(GPAlgorithm algorithm, Boolean visualize);
}
