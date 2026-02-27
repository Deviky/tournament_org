package com.deviky.Tournament_Service.bracket.bracket_core.algorithm_base;
import java.util.Map;

public class BracketAlgorithmFactory {

    private final Map<String, BracketAlgorithm> algorithms;

    public BracketAlgorithmFactory(Map<String, BracketAlgorithm> algorithms) {
        this.algorithms = algorithms;
    }

    public BracketAlgorithm getAlgorithm(String type) {
        BracketAlgorithm algo = algorithms.get(type);
        if (algo == null) throw new IllegalArgumentException("Algorithm not found: " + type);
        return algo;
    }

    public Map<String, BracketAlgorithm> getAllAlgorithms() {
        return algorithms;
    }
}