package ca.waterloo.dsg.graphflow.query.parser;

import ca.waterloo.dsg.graphflow.storage.Graph.Version;
import ca.waterloo.dsg.graphflow.storage.KeyStore;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class QueryParserTest {
/*
    @Test
    public void parseEdgeScan() {
        var query = "(a)->(b)";
        var keyStore = Mockito.mock(KeyStore.class);
        var deltaQueries = QueryParser.parseDeltaQueries(query, keyStore);
        Assert.assertEquals(1 /* expected *, deltaQueries.size());
        Assert.assertEquals(1 /* expected *, deltaQueries.get(0).getEdges().size());
        Assert.assertEquals(Version.DELTA, deltaQueries.get(0).getEdges().get(0).getVersion());
    }

    @Test
    public void parseTriangle() {
        var query = "(a)->(b),(b)->(c),(c)->(a)";
        var keyStore = Mockito.mock(KeyStore.class);
        var deltaQueries = QueryParser.parseDeltaQueries(query, keyStore);
        Assert.assertEquals(3 /* expected *, deltaQueries.size());

        var deltaQuery1 = deltaQueries.get(0);
        Assert.assertEquals(3 /* expected *, deltaQuery1.getEdges().size());
        Assert.assertEquals(Version.DELTA, deltaQuery1.getEdges().get(0).getVersion());
        Assert.assertEquals(Version.OLD, deltaQuery1.getEdges().get(1).getVersion());
        Assert.assertEquals(Version.OLD, deltaQuery1.getEdges().get(2).getVersion());

        var deltaQuery2 = deltaQueries.get(1);
        Assert.assertEquals(3 /* expected *, deltaQuery2.getEdges().size());
        Assert.assertEquals(Version.NEW, deltaQuery2.getEdges().get(0).getVersion());
        Assert.assertEquals(Version.DELTA, deltaQuery2.getEdges().get(1).getVersion());
        Assert.assertEquals(Version.OLD, deltaQuery2.getEdges().get(2).getVersion());

        var deltaQuery3 = deltaQueries.get(2);
        Assert.assertEquals(3 /* expected *, deltaQuery3.getEdges().size());
        Assert.assertEquals(Version.NEW, deltaQuery3.getEdges().get(0).getVersion());
        Assert.assertEquals(Version.NEW, deltaQuery3.getEdges().get(1).getVersion());
        Assert.assertEquals(Version.DELTA, deltaQuery3.getEdges().get(2).getVersion());
    }

    @Test
    public void parseDiamond() {
        var query = "(a)->(b),(b)->(d),(a)->(c),(c)->(d)";
        var keyStore = Mockito.mock(KeyStore.class);
        var deltaQueries = QueryParser.parseDeltaQueries(query, keyStore);
        Assert.assertEquals(4 /* expected *, deltaQueries.size());

        var deltaQuery1 = deltaQueries.get(0);
        Assert.assertEquals(4 /* expected *, deltaQuery1.getEdges().size());
        Assert.assertEquals(Version.DELTA, deltaQuery1.getEdges().get(0).getVersion());
        Assert.assertEquals(Version.OLD, deltaQuery1.getEdges().get(1).getVersion());
        Assert.assertEquals(Version.OLD, deltaQuery1.getEdges().get(2).getVersion());
        Assert.assertEquals(Version.OLD, deltaQuery1.getEdges().get(3).getVersion());

        var deltaQuery2 = deltaQueries.get(1);
        Assert.assertEquals(4 /* expected *, deltaQuery2.getEdges().size());

        Assert.assertEquals(Version.NEW, deltaQuery2.getEdges().get(0).getVersion());
        Assert.assertEquals(Version.DELTA, deltaQuery2.getEdges().get(1).getVersion());
        Assert.assertEquals(Version.OLD, deltaQuery2.getEdges().get(2).getVersion());
        Assert.assertEquals(Version.OLD, deltaQuery2.getEdges().get(3).getVersion());

        var deltaQuery3 = deltaQueries.get(2);
        Assert.assertEquals(4 /* expected *, deltaQuery3.getEdges().size());
        Assert.assertEquals(Version.NEW, deltaQuery3.getEdges().get(0).getVersion());
        Assert.assertEquals(Version.NEW, deltaQuery3.getEdges().get(1).getVersion());
        Assert.assertEquals(Version.DELTA, deltaQuery3.getEdges().get(2).getVersion());
        Assert.assertEquals(Version.OLD, deltaQuery3.getEdges().get(3).getVersion());

        var deltaQuery4 = deltaQueries.get(3);
        Assert.assertEquals(4 /* expected *, deltaQuery4.getEdges().size());
        Assert.assertEquals(Version.NEW, deltaQuery4.getEdges().get(0).getVersion());
        Assert.assertEquals(Version.NEW, deltaQuery4.getEdges().get(1).getVersion());
        Assert.assertEquals(Version.NEW, deltaQuery4.getEdges().get(2).getVersion());
        Assert.assertEquals(Version.DELTA, deltaQuery4.getEdges().get(3).getVersion());
    }*/
}
