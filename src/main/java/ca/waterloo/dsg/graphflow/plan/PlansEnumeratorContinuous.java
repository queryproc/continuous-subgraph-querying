package ca.waterloo.dsg.graphflow.plan;

import ca.waterloo.dsg.graphflow.planner.Catalog.Catalog;
import ca.waterloo.dsg.graphflow.planner.Planner;
import ca.waterloo.dsg.graphflow.query.QueryGraph;
import ca.waterloo.dsg.graphflow.util.collection.ListUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Given a delta query, enumerates a plan per connected QVO.
 */
public class PlansEnumeratorContinuous {

    private List<QueryGraph> deltaQueries;

    /**
     * Constructs a {@link PlansEnumeratorContinuous} object.
     */
    public PlansEnumeratorContinuous(List<QueryGraph> deltaQueries) {
        this.deltaQueries = deltaQueries;
    }

    /**
     * @return The list of generated plans.
     */
    public List<PlanCombined> generatePlans(Catalog catalog) {
        var plansPerDeltaQuery = deltaQueries
            .stream()
            .map(deltaQuery -> new PlansEnumeratorSingle(deltaQuery).generatePlans())
            .collect(Collectors.toList());

        var planCartesianProduct = ListUtils.cartesianProduct(plansPerDeltaQuery);
        return planCartesianProduct
            .stream()
            .map(plans -> getCombinedPlan(plans, catalog))
            .collect(Collectors.toList());
    }

    private PlanCombined getCombinedPlan(List<Plan> plans, Catalog catalog) {
        var planner = new Planner(catalog);
        planner.addAll(plans
            .stream()
            .map(Plan::copy)
            .collect(Collectors.toList()));
        return planner.getCombinedPlan();
    }
}
