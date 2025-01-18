package ca.waterloo.dsg.graphflow.plan;

import ca.waterloo.dsg.graphflow.query.parser.QueryParser;
import ca.waterloo.dsg.graphflow.storage.KeyStore;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class PlanEnumeratorTest {
/*
    @Test
    public void generateScannedEdges() {
        var query = "(a)->(b)";
        var keyStore = Mockito.mock(KeyStore.class);
        var deltaQueries = QueryParser.parseDeltaQueries(query, keyStore);
        Assert.assertEquals(1 /* expected *, deltaQueries.size());
        for (var deltaQ : deltaQueries) {
            Assert.assertEquals(1 /*expected*, new PlansEnumeratorSingle(deltaQ).generatePlans().size());
        }
    }

    @Test
    public void generateTrianglePlans() {
        var query = "(a)->(b),(b)->(c),(c)->(a)";
        var keyStore = Mockito.mock(KeyStore.class);
        var deltaQueries = QueryParser.parseDeltaQueries(query, keyStore);
        for (var deltaQ : deltaQueries) {
            Assert.assertEquals(1 /*expected*, new PlansEnumeratorSingle(deltaQ).generatePlans().size());
        }
    }

    @Test
    public void generateDiamondPlans() {
        var query = "(a)->(b),(b)->(d),(a)->(c),(c)->(d)";
        var keyStore = Mockito.mock(KeyStore.class);
        var deltaQueries = QueryParser.parseDeltaQueries(query, keyStore);
        for (var deltaQ : deltaQueries) {
            Assert.assertEquals(2 /*expected*, new PlansEnumeratorSingle(deltaQ).generatePlans().size());
        }
    }*/
}
