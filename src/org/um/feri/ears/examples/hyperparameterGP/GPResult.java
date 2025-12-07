package org.um.feri.ears.examples.hyperparameterGP;

// Not a java way of doing things...
public class GPResult {
    public double trainEval;
    public double testEval;

    GPResult(double trainEval, double testEval) {
        this.trainEval = trainEval;
        this.testEval = testEval;
    }
}
