package ca.waterloo.dsg.graphflow.runner.plan;

import ca.waterloo.dsg.graphflow.plan.PlanCombined;
import ca.waterloo.dsg.graphflow.plan.PlansEnumeratorContinuous;
import ca.waterloo.dsg.graphflow.planner.Catalog.Catalog;
import ca.waterloo.dsg.graphflow.planner.Planner;
import ca.waterloo.dsg.graphflow.query.QueryGraph;
import ca.waterloo.dsg.graphflow.query.parser.QueryParser;
import ca.waterloo.dsg.graphflow.runner.AbstractRunner;
import ca.waterloo.dsg.graphflow.runner.ArgsFactory;
import ca.waterloo.dsg.graphflow.storage.KeyStore;
import ca.waterloo.dsg.graphflow.util.IOUtils;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Given a query graph, generates the setAdjListSortOrder of unique query plans and serializes
 * said queries for use by the {@link QueryPlanSerExecutor}.
 */
public class QueryPlansGenerator extends AbstractRunner {

    private static final Logger logger = LogManager.getLogger(QueryPlansGenerator.class);

    public static void main(String[] args) throws IOException {
        // If the user asks for help, enforce it over the required options.
        if (isAskingHelp(args, getCommandLineOptions())) {
            return;
        }

        var cmdLine = parseCmdLine(args, getCommandLineOptions());
        if (null == cmdLine) {
            logger.info("could not parse all the program arguments.");
            return;
        }

        var inputDirectory = sanitizeDirStr(cmdLine.getOptionValue(ArgsFactory.INPUT_GRAPH_DIR));
        KeyStore store;
        Catalog catalog;
        try {
            store = KeyStore.deserialize(inputDirectory);
            catalog = Catalog.deserialize(inputDirectory);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error in deserialization: " + e.getMessage());
            return;
        }

        if (cmdLine.hasOption(ArgsFactory.SHARING_ALDS_ENABLED)) {
            Planner.SHARING_ALDS_ENABLED = true;
        }
        if (cmdLine.hasOption(ArgsFactory.SHARING_OPERATORS_ENABLED)) {
            Planner.SHARING_OPERATORS_ENABLED = true;
        }
        var deltaQueries = getInputQueryGraphs(cmdLine.getOptionValue(ArgsFactory.QUERIES), store);
        var optimizer_type = cmdLine.getOptionValue(ArgsFactory.OPTIMIZER_TYPE);
        var outputDirectory = sanitizeDirStrAndMkdirIfNeeded(cmdLine.getOptionValue(
            ArgsFactory.SERIALIZE_OUTPUT));
        if (optimizer_type.equals("ALL")) {
            var enumerator = new PlansEnumeratorContinuous(deltaQueries);
            var plans = enumerator.generatePlans(catalog);
            sortPlans(plans);
            var it = plans.listIterator();
            while (it.hasNext()) {
                IOUtils.serializeObj(outputDirectory + "ser_plan_" + it.nextIndex(), it.next());
            }
        } else if (optimizer_type.equals("GR")) {
            var planner = new Planner(catalog);
            planner.registerQueries(deltaQueries);
            IOUtils.serializeObj(outputDirectory + "ser_plan_" + 0, planner.getCombinedPlan());
        } else if (optimizer_type.equals("BNS")) {
            var planner = new Planner(catalog);
            for (var deltaQuery : deltaQueries) {
                planner.register(deltaQuery);
            }
            IOUtils.serializeObj(outputDirectory + "ser_plan_" + 0, planner.getCombinedPlan());
        } else { // optimizer_type.equals("BS")
            var planner = new Planner(catalog);
            for (var deltaQuery : deltaQueries) {
                planner.register(deltaQuery);
            }
            var plans = planner.getPlans();
            planner = new Planner(catalog);
            planner.addAll(plans);
            IOUtils.serializeObj(outputDirectory + "ser_plan_" + 0, planner.getCombinedPlan());
        }
    }

    private static void sortPlans(List<PlanCombined> plans) {
        plans.sort((PlanCombined p1, PlanCombined p2) -> {
            if (p1.getExpectedICost() == p2.getExpectedICost()) {
                return 0;
            } else if (p1.getExpectedICost() < p2.getExpectedICost()) {
                return -1;
            }
            return 1;
        });
    }

    private static List<QueryGraph> getInputQueryGraphs(String inputFile, KeyStore store)
        throws IOException {
        var queryGraphs = new ArrayList<QueryGraph>();
        var reader = new BufferedReader(new FileReader(inputFile));
        var line = reader.readLine().split("\n")[0];
        while (line != null) {
            var deltaQueries = QueryParser.parseDeltaQueries(line, store);
            queryGraphs.addAll(deltaQueries);
            line = reader.readLine();
        }
        reader.close();
        return queryGraphs;
    }

    /**
     * @return The {@link Options} required by the {@link QueryPlansGenerator}.
     */
    private static Options getCommandLineOptions() {
        var options = new Options();                                        // ArgsFactory.
        options.addOption(ArgsFactory.getInputGraphDirOption());            // INPUT_GRAPH_DIR   -i
        options.addOption(ArgsFactory.getOutputDirOption());                // OUTPUT            -o
        options.addOption(ArgsFactory.getQueriesInputFileOption());         // QUERIES           -q
        options.addOption(ArgsFactory.getSharingOperatorsEnabledOption());  // SHARING_OPERATORS -s
        options.addOption(ArgsFactory.getSharingALDsEnabledOption());       // SHARING_ALDS      -a
        options.addOption(ArgsFactory.getOptimizerTypeOption());            // OPTIMIZER_TYPE    -t
        return options;
    }
}
