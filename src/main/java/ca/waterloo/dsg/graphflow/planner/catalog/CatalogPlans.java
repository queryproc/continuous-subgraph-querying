package ca.waterloo.dsg.graphflow.planner.Catalog;

import ca.waterloo.dsg.graphflow.plan.Plan;
import ca.waterloo.dsg.graphflow.plan.PlanCombined;
import ca.waterloo.dsg.graphflow.plan.PlansEnumeratorSingle;
import ca.waterloo.dsg.graphflow.query.QueryGraph;
import ca.waterloo.dsg.graphflow.storage.Graph;
import ca.waterloo.dsg.graphflow.storage.KeyStore;

import java.util.ArrayList;
import java.util.List;

public class CatalogPlans {

    private PlanCombined planCombined;

    public PlanCombined getPlanCombined() {
        return planCombined;
    }

    public CatalogPlans(List<QueryGraph> queryGraphs) {
        var plans = new ArrayList<Plan>();
        for (var query : queryGraphs) {
            plans.addAll(new PlansEnumeratorSingle(query, true /* isForCatalog*/).generatePlans());
        }
        planCombined = new PlanCombined(plans);
    }

    void execute(Graph graph, KeyStore store) {
        planCombined.setScansToSample(graph);
        planCombined.init(graph, store);
        planCombined.execute();
    }

    void addStatsToCatalog(Catalog catalog) {
        for (var plan : planCombined.getPlans()) {
            plan.getScan().addStatsToCatalog(catalog);
        }
    }
}
