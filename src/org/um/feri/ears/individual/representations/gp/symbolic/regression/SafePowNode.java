package org.um.feri.ears.individual.representations.gp.symbolic.regression;

import org.um.feri.ears.individual.representations.gp.Node;

import java.util.List;
import java.util.Map;

public class SafePowNode extends OperatorNode {

    public SafePowNode() {
        super("^");
    }
    public SafePowNode(List<Node> children) {
        super("^", 2, children);
    }

    @Override
    public double evaluate(Map<String, Double> variables) {
        double base = children.get(0).evaluate(variables);
        double exponent = children.get(1).evaluate(variables);

        // Handles problematic values
        if (Double.isNaN(base) || Double.isNaN(exponent)) return 0;
        if (Double.isInfinite(base) || Double.isInfinite(exponent)) return 0;

        // Handle zero base with negative exponent
        if (base == 0 && exponent < 0) return 1;

        // Handle negative base with fractional exponent
        if (base < 0 && exponent != Math.floor(exponent)) return 1;

        // Makes sure there are no NaN values
        double result = Math.pow(base, exponent);
        if (Double.isNaN(result) || Double.isInfinite(result)) return 0;

        return result;
    }
}