package ca.waterloo.dsg.graphflow.runner.dataset;

import ca.waterloo.dsg.graphflow.plan.operator.Operator;
import ca.waterloo.dsg.graphflow.planner.Catalog.Catalog;
import ca.waterloo.dsg.graphflow.query.QueryGraph;
import ca.waterloo.dsg.graphflow.query.parser.QueryParser;
import ca.waterloo.dsg.graphflow.runner.AbstractRunner;
import ca.waterloo.dsg.graphflow.runner.ArgsFactory;
import ca.waterloo.dsg.graphflow.storage.Graph;
import ca.waterloo.dsg.graphflow.storage.Graph.Version;
import ca.waterloo.dsg.graphflow.storage.GraphLoader;
import ca.waterloo.dsg.graphflow.storage.KeyStore;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the given dataset, resets the catalog and saves the catalog in a serialized format in the
 * given output directory.
 */
public class CatalogSerializer extends AbstractRunner {

    protected static final Logger logger = LogManager.getLogger(CatalogSerializer.class);

    public static void main(String[] args) {
        // If the user asks for help, enforce it over the required options.
        if (isAskingHelp(args, getCommandLineOptions())) {
            return;
        }

        var cmdLine = parseCmdLine(args, getCommandLineOptions());
        if (null == cmdLine) {
            logger.info("could not parse all the program arguments");
            return;
        }

        // Load the data from the given binary directory.
        var inputDirectory = sanitizeDirStr(cmdLine.getOptionValue(ArgsFactory.INPUT_GRAPH_DIR));
        Graph graph;
        KeyStore store;
        try {
            store = KeyStore.deserialize(inputDirectory);
            graph = new GraphLoader().make(inputDirectory, store.getNextLabelKey());
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error in deserialization: " + e.getMessage());
            return;
        }

        Catalog catalog;
        try {
            catalog = Catalog.deserialize(inputDirectory);
        } catch (IOException | ClassNotFoundException e) {
            catalog = new Catalog();
        }

        try {
            Operator.CACHING_ENABLED = false;
            var queries = getInputQueryGraphs(cmdLine.getOptionValue(ArgsFactory.QUERIES), store);
            catalog.populate(graph, store, queries, inputDirectory);
        } catch (IOException e) {
            logger.error("Error logging catalog in human readable format: " + e.getMessage());
        }

        try {
            catalog.serialize(inputDirectory);
        } catch (IOException e) {
            logger.error("Error in serializing the catalog: " + e.getMessage());
        }
    }

    private static List<QueryGraph> getInputQueryGraphs(String inputFile, KeyStore store)
        throws IOException {
        var queryGraphs = new ArrayList<QueryGraph>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        var line = reader.readLine().split("\n")[0];
        while (line != null) {
            var queryGraph = QueryParser.parse(line, store);
            for (var edge : queryGraph.getEdges()) {
                edge.setVersion(Version.OLD);
            }
            queryGraphs.add(queryGraph);
            line = reader.readLine();
        }
        reader.close();
        return queryGraphs;
    }

    /**
     * @return The {@link Options} required by the {@link CatalogSerializer}.
     */
    private static Options getCommandLineOptions() {
        var options = new Options();                                   // ArgsFactory.
        options.addOption(ArgsFactory.getInputGraphDirOption());       // INPUT_GRAPH_DIR        -i
        options.addOption(ArgsFactory.getQueriesInputFileOption());    // QUERIES                -q
        return options;
    }
}
