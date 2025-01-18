package ca.waterloo.dsg.graphflow.plan;

import ca.waterloo.dsg.graphflow.storage.Graph;
import ca.waterloo.dsg.graphflow.storage.KeyStore;
import ca.waterloo.dsg.graphflow.util.IOUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanCombined implements Serializable {

    private List<Plan> plans;
    private double expectedICost;
    private double elapsedTime;

    public List<Plan> getPlans() {
        return plans;
    }

    public double getExpectedICost() {
        return expectedICost;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    /**
     * Constructs a combined plan
     *
     * @param plans The plans combined.
     */
    public PlanCombined(List<Plan> plans) {
        this.plans = plans;
    }

    /**
     * Initialize the plan by initializing all of its plans.
     *
     * @param graph is the input data graph.
     * @param store is the labels and types key store.
     */
    public void init(Graph graph, KeyStore store) {
        plans.forEach(plan -> plan.init(graph, store));
    }

    /**
     * Executes the {@link PlanCombined}.
     */
    public void execute() {
        var beginTime = System.nanoTime();
        plans.forEach(Plan::execute);
        elapsedTime += IOUtils.getElapsedTimeInMillis(beginTime);
        plans.forEach(Plan::resetCache);
    }

    public void setScansToSample(Graph graph) {
        plans.forEach(plan -> plan.setScansToSample(graph));
    }

    public void setExpectedICost() {
        plans.forEach(plan -> expectedICost += plan.getExpectedICost());
    }

    public String getOutputLog() {
        var icost = plans.stream().mapToLong(Plan::getICost).sum();
        var numOutTuples = plans.stream().mapToLong(Plan::getNumOutTuples).sum();
        var numOperators = plans.stream().mapToInt(Plan::getNumOperators).sum();
        var numInterTuples = plans.stream().mapToLong(Plan::getNumIntermediateTuples).sum();
        var outputLog = String.format("%.2f", elapsedTime) + "," + numOutTuples + "," +
            numOperators + "," + icost + "," + String.format("%.2f", expectedICost) + "," +
                numInterTuples + "\n";
        outputLog += "# <level, ICost> pairs\n";
        var levelToICostMap = getLevelToICostMap();
        for (var level : levelToICostMap.keySet()) {
            outputLog += "# " + level + ": " + levelToICostMap.get(level) + "\n";
        }
        return outputLog;
    }

    private Map<Integer, Long> getLevelToICostMap() {
        var levelToICostMap = new HashMap<Integer, Long>();
        plans.forEach(plan -> plan.getLevelToICostMap(levelToICostMap));
        return levelToICostMap;
    }
}
