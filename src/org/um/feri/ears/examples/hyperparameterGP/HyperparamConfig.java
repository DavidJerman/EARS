package org.um.feri.ears.examples.hyperparameterGP;

import org.um.feri.ears.individual.representations.gp.Node;

import java.util.List;

public record HyperparamConfig(
        List<Class<? extends Node>> funcSet,
        List<Class<? extends Node>> terminalSet
) {}
