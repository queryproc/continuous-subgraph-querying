package ca.waterloo.dsg.graphflow.planner;

import ca.waterloo.dsg.graphflow.plan.Plan;
import ca.waterloo.dsg.graphflow.plan.PlanCombined;
import ca.waterloo.dsg.graphflow.plan.PlansEnumeratorSingle;
import ca.waterloo.dsg.graphflow.plan.operator.Operator;
import ca.waterloo.dsg.graphflow.planner.Catalog.Catalog;
import ca.waterloo.dsg.graphflow.query.QueryGraph;
import org.antlr.v4.runtime.misc.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Generates a {@link Plan}. The intersection cost (ICost) is used as a metric of the optimization.
 */
public class Planner {

    public static boolean SHARING_OPERATORS_ENABLED = false;
    public static boolean SHARING_ALDS_ENABLED = false;

    private Catalog catalog;
    private List<Plan> plans = new ArrayList<>();

    public List<Plan> getPlans() {
        return plans;
    }

    /**
     * Constructs a {@link Planner} object.
     *
     * @param catalog is the catalog containing cost and selectivity stats of the input graph.
     */
    public Planner(Catalog catalog) {
        this.catalog = catalog;
    }

    /**
     * returns the combined plan.
     *
     * @return The {@link PlanCombined}.
     */
    public PlanCombined getCombinedPlan() {
        var planCombined = new PlanCombined(plans);
        planCombined.setExpectedICost();
        return planCombined;
    }

    public void registerQueries(List<QueryGraph> deltaQueries) {
        for (var deltaQuery : deltaQueries) {
            var plans = new PlansEnumeratorSingle(deltaQuery).generatePlans();
            if (0 == plans.size()) {
                register(plans);
            } else {
                shareOperatorsToGetCostCombinedPlan(plans);
            }
        }
        plans.forEach(plan -> plan.setEstimatedICostAndSelectivity(catalog));
        addALDSharing();
    }

    public void addAll(List<Plan> plans) {
        plans.forEach(plan -> {
            var planAsList = new ArrayList<Plan>();
            planAsList.add(plan);
            shareOperatorsToGetCostCombinedPlan(planAsList);
        });
    }

    private void addALDSharing() {
        // Inside ALD Sharing, choose to share if it helps and take caching into account.
        if (Planner.SHARING_ALDS_ENABLED) {
            plans.forEach(plan -> plan.addALDSharing(catalog));
        }
    }

    public void register(QueryGraph deltaQuery) {
        register(new PlansEnumeratorSingle(deltaQuery).generatePlans());
    }

    private void shareOperatorsToGetCostCombinedPlan(List<Plan> generatedPlans) {
        var sharedOperators = new ArrayList<Triple<Plan, Operator, Operator>>();
        for (var generatedPlan : generatedPlans) {
            generatedPlan.setEstimatedICostAndSelectivity(catalog);
            generatedPlan.setExpectedICost();
            for (var plan : plans) {
                var operatorPair = plan.findOperatorToShare(generatedPlan, true /*startAtLastOp*/);
                sharedOperators.add(new Triple<>(plan, operatorPair.a, operatorPair.b));
            }
        }
        if (sharedOperators.size() == 0) {
            register(generatedPlans);
        } else { // sharedOperators.size() > 0
            Triple<Plan, Operator, Operator> bestOperatorPair = null;
            var planCost = Double.MAX_VALUE;
            for (var operatorPair : sharedOperators) {
                var cost = 0.0;
                if (operatorPair.b.getNextOperator() != null) {
                    var nextEI = operatorPair.b.getNextOperator();
                    var fromPosToNumOutTuples = new HashMap<Integer, Double>();
                    operatorPair.c.setIdxToNumOutTuples(fromPosToNumOutTuples, catalog);
                    cost = nextEI.getExpectedTotalICost(catalog, fromPosToNumOutTuples);
                }
                if (cost < planCost) {
                    planCost = cost;
                    bestOperatorPair = operatorPair;
                }
            }
            if (null != bestOperatorPair) {
                bestOperatorPair.a.setExpectedICost(planCost);
                bestOperatorPair.c.addNext(bestOperatorPair.b, bestOperatorPair.a);
            }
        }
    }

    private void register(List<Plan> generatedPlans) {
        Plan bestPlan = null;
        var minCost = Double.MAX_VALUE;
        for (var plan : generatedPlans) {
            plan.setEstimatedICostAndSelectivity(catalog);
            plan.setExpectedICost();
            if (plan.getExpectedICost() < minCost) {
                minCost = plan.getExpectedICost();
                bestPlan = plan;
            }
        }
        plans.add(bestPlan);
    }
}
