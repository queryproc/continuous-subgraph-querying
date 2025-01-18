package ca.waterloo.dsg.graphflow.planner;

import ca.waterloo.dsg.graphflow.planner.Catalog.Catalog;
import ca.waterloo.dsg.graphflow.query.parser.QueryParser;
import ca.waterloo.dsg.graphflow.storage.KeyStore;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class GreedyPlannerTest {
/*
    @Test
    public void registerNoSharing() {
        var query = "(a)->(b),(b)->(d),(a)->(c),(c)->(d)";
        var catalog = Mockito.mock(Catalog.class);
        Mockito.when(catalog.getICost(Mockito.any(), Mockito.any())).thenReturn(0.0);
        Mockito.when(catalog.getSelectivity(Mockito.any())).thenReturn(1.0);
        var keyStore = Mockito.mock(KeyStore.class);
        var deltaQueries = QueryParser.parseDeltaQueries(query, keyStore);
        var planner = new Planner(catalog);
        for (var deltaQuery : deltaQueries) {
            planner.register(deltaQuery);
        }
        Assert.assertEquals(4 /* single plan *, planner.getPlans().size());
    }

    @Test
    public void registerWithSharing() {
        var query = "(a)->(b),(b)->(d),(a)->(c),(c)->(d)";
        var catalog = Mockito.mock(Catalog.class);
        Mockito.when(catalog.getICost(Mockito.any(), Mockito.any())).thenReturn(0.0);
        Mockito.when(catalog.getSelectivity(Mockito.any())).thenReturn(1.0);
        var keyStore = Mockito.mock(KeyStore.class);
        var deltaQueries = QueryParser.parseDeltaQueries(query, keyStore);
        var planner = new Planner(catalog);
        planner.registerQueries(deltaQueries);
        Assert.assertEquals(1 /* single plan *, planner.getPlans().size());
    }*/
}
