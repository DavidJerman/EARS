package org.um.feri.ears.examples.hyperparameterGP;

import org.apache.commons.math3.util.Combinations;
import org.um.feri.ears.individual.representations.gp.Node;

import java.util.ArrayList;
import java.util.List;

public class NodeListGenerator {
    public List<List<Class<? extends Node>>> combinations;
    int count;

    // Generates all possible combinations of nodes
    public NodeListGenerator(List<Class<? extends Node>> baseNodes, List<Class<? extends Node>> extraNodes) {
        combinations = new ArrayList<>();
        count = 0;

        for (int k = 0; k <= extraNodes.size(); k++) {
            Combinations combs = new Combinations(extraNodes.size(), k);

            for (int[] c : combs) {
                List<Class<? extends Node>> set = new ArrayList<>(baseNodes);
                for (int idx : c) set.add(extraNodes.get(idx));
                combinations.add(set);
                count++;
            }
        }

        System.out.println("Generated combinations: " + count);
    }
}
