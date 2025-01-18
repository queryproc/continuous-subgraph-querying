package ca.waterloo.dsg.graphflow.runner.plan;

import ca.waterloo.dsg.graphflow.plan.PlanCombined;
import ca.waterloo.dsg.graphflow.query.QueryGraph;
import ca.waterloo.dsg.graphflow.runner.AbstractRunner;
import ca.waterloo.dsg.graphflow.runner.ArgsFactory;
import ca.waterloo.dsg.graphflow.storage.Graph;
import ca.waterloo.dsg.graphflow.storage.GraphLoader;
import ca.waterloo.dsg.graphflow.storage.KeyStore;
import ca.waterloo.dsg.graphflow.util.IOUtils;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Runs a specific transform for a {@link QueryGraph} and logs the output.
 */
public class QueryPlanSerExecutor extends AbstractRunner {

    protected static final Logger logger = LogManager.getLogger(QueryPlanSerExecutor.class);

    public static void main(String[] args) throws IOException {
        // If the user asks for help, enforce it over the required options.
        if (isAskingHelp(args, getCommandLineOptions())) {
            return;
        }

        var cmdLine = parseCmdLine(args, getCommandLineOptions());
        if (null == cmdLine) {
            logger.info("could not parse all the program arguments");
            return;
        }

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

        PlanCombined plan;
        try {
            var queryPlanFile = cmdLine.getOptionValue(ArgsFactory.INPUT_SER_PLAN);
            plan = (PlanCombined) IOUtils.deserializeObj(queryPlanFile);
        } catch (IOException | ClassNotFoundException e) {
            logger.info(e.getMessage() + "Error deserializing the Plan object");
            return;
        }
        plan.init(graph, store);
        var filename = cmdLine.getOptionValue(ArgsFactory.INPUT_FILE_EDGES);
        var reader = new BufferedReader(new FileReader(filename));
        var notifyDone = false;
        while (true) {
            for (var i = 0; i < Graph.EDGE_BATCH_SIZE; i ++) {
                var line = reader.readLine();
                if (null == line) {
                    notifyDone = true;
                    break;
                }
                var row = line.split(",");
                var fromVertex = Integer.parseInt(row[0]);
                var toVertex = Integer.parseInt(row[1]);
                var label = store.getLabelKeyAsShort(row[2]);
                graph.insertEdgeTemporarily(fromVertex, toVertex, label);
            }
            plan.execute();
            graph.finalizeChanges();
            // System.gc();
            if (notifyDone) {
                break;
            }
        }
        // initialize and execute the query and log the output.
        IOUtils.log(cmdLine.getOptionValue(ArgsFactory.OUTPUT_FILE), plan.getOutputLog());
    }

    /**
     * @return The {@link Options} required by the {@link QueryPlanSerExecutor}.
     */
    private static Options getCommandLineOptions() {
        var options = new Options();                                       // ArgsFactory.
        options.addOption(ArgsFactory.getInputGraphDirOption());           // INPUT_GRAPH_DIR    -i
        options.addOption(ArgsFactory.getInputSerializedPlanOption());     // INPUT_SER_PLAN     -p
        options.addOption(ArgsFactory.getDeltaEdgesOption());              // INPUT_FILE_EDGES   -e
        options.addOption(ArgsFactory.getOutputFileOption());              // OUTPUT_FILE        -o
        return options;
    }
}
