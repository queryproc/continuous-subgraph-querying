package ca.waterloo.dsg.graphflow.query;

import ca.waterloo.dsg.graphflow.query.QueryGraph.IsomorphismIterator;
import ca.waterloo.dsg.graphflow.query.parser.QueryParser;
import ca.waterloo.dsg.graphflow.storage.KeyStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests {@link QueryGraph}.
 */
public class SubgraphMappingIteratorTest {
/*
    private KeyStore storeMock = Mockito.mock(KeyStore.class);

    @Before
    public void mockKeyStore() {
        // type keys.
        Mockito.when(storeMock.getTypeKeyAsShort("0")).thenReturn((short) 0);
        Mockito.when(storeMock.getTypeKeyAsShort("1")).thenReturn((short) 1);
        // label keys.
        Mockito.when(storeMock.getLabelKeyAsShort("0")).thenReturn((short) 0);
        Mockito.when(storeMock.getLabelKeyAsShort("1")).thenReturn((short) 1);
    }

    @Test
    public void testIsomorphismWithTriangles() {
        var triangle = QueryParser.parse("(a)->(b),(b)->(c),(c)->(a)", storeMock);
        var triangle2 = QueryParser.parse("(x)->(y),(y)->(z),(z)->(x)", storeMock);
        assetNumMappings(triangle.getSubgraphMappingIterator(triangle2), 3 /* expected *);
    }

    @Test
    public void testIsomorphismWithSymTriangle() {
        var triangle = QueryParser.parse("(a)->(b),(b)->(c),(a)->(c)", storeMock);
        var triangle2 = QueryParser.parse("(x)->(y),(y)->(z),(x)->(z)", storeMock);
        assetNumMappings(triangle.getSubgraphMappingIterator(triangle2), 1 /* expected *);
    }

    @Test
    public void testIsomorphismWithTriangleWithTypes() {
        var triangle = QueryParser.parse("(a)->(b:1),(b)->(c),(c)->(a)", storeMock);
        var triangle2 = QueryParser.parse("(x)->(y),(y:1)->(z),(z)->(x)", storeMock);
        assetNumMappings(triangle.getSubgraphMappingIterator(triangle2), 1 /* expected *);

        triangle = QueryParser.parse("(a)->(b),(b)->(c),(c)->(a)", storeMock);
        triangle2 = QueryParser.parse("(x)->(y),(y:1)->(z),(z)->(x)", storeMock);
        assetNumMappings(triangle.getSubgraphMappingIterator(triangle2), 0 /* expected *);
    }

    @Test
    public void testIsomorphismWithTriangleWithLabels() {
        var triangle = QueryParser.parse("(a)-[1]->(b),(b)->(c),(c)->(a)", storeMock);
        var triangle2 = QueryParser.parse("(x)->(y),(y)-[1]->(z),(z)->(x)", storeMock);
        assetNumMappings(triangle.getSubgraphMappingIterator(triangle2), 1 /* expected *);

        triangle = QueryParser.parse("(a)-[0]->(b),(b)->(c),(c)->(a)", storeMock);
        triangle2 = QueryParser.parse("(x)->(y),(y)-[1]->(z),(z)->(x)", storeMock);
        assetNumMappings(triangle.getSubgraphMappingIterator(triangle2), 0 /* expected *);
    }


    @Test
    public void testIsomorphismWithSymTriangleWithTypes() {
        var triangle = QueryParser.parse("(a:1)->(b),(b)->(c),(a)->(c)", storeMock);
        var triangle2 = QueryParser.parse("(x)->(y),(y)->(z),(x:1)->(z)", storeMock);
        assetNumMappings(triangle.getSubgraphMappingIterator(triangle2), 1 /* expected *);

        triangle = QueryParser.parse("(a:1)->(b),(b)->(c),(a)->(c)", storeMock);
        triangle2 = QueryParser.parse("(x)->(y),(y)->(z),(x)->(z)", storeMock);
        assetNumMappings(triangle.getSubgraphMappingIterator(triangle2), 0 /* expected *);
    }

    @Test
    public void testSubgraphIsomorphism() {
        var diamond = QueryParser.parse("(a)->(b),(b)->(c),(c)->(a),(c)->(d)", storeMock);
        var twoEdgePath = QueryParser.parse("(a)->(b),(b)->(c)", storeMock);
        assetNumMappings(diamond.getSubgraphMappingIterator(twoEdgePath), 4 /* expected *);
    }

    private void assetNumMappings(IsomorphismIterator it, int expectedCount) {
        var count = 0;
        while (it.hasNext()) {
            count++;
            it.next();
        }
        Assert.assertEquals(expectedCount, count);
    }*/
}
