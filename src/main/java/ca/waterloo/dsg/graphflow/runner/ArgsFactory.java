package ca.waterloo.dsg.graphflow.runner;

import org.apache.commons.cli.Option;

/**
 * The class containing all Command Line Options options needed by the runners.
 */
public class ArgsFactory {

    public static String QUERIES = "q";
    public static String OUTPUT_FILE = "o";
    public static String INPUT_SER_PLAN = "p";
    public static String OPTIMIZER_TYPE = "t";
    public static String INPUT_GRAPH_DIR = "i";
    public static String INPUT_FILE_EDGES = "e";
    public static String SERIALIZE_OUTPUT = "o";
    public static String EDGES_FILE_SEPARATOR = "m";
    public static String SHARING_ALDS_ENABLED = "a";
    public static String SHARING_OPERATORS_ENABLED = "s";

    static Option getHelpOption() {
        return new Option("h" /* HELP */, "help", false, "Print this message.");
    }

    public static Option getInputFileEdges() {
        var option = new Option(INPUT_FILE_EDGES, "input_file_edges", true /* hasArg */,
            "The input file edges to load.");
        option.setRequired(true);
        return option;
    }

    public static Option getDeltaEdgesOption() {
        var option = new Option(INPUT_FILE_EDGES, "delta_edges", true /* hasArg */,
            "The delta file edges to load in batches.");
        option.setRequired(true);
        return option;
    }

    public static Option getOutputDirOption() {
        var option = new Option(SERIALIZE_OUTPUT, "output", true,
            "Absolute path to serialize the input graph.");
        option.setRequired(true);
        return option;
    }

    public static Option getOptimizerTypeOption() {
        var option = new Option(OPTIMIZER_TYPE, "optimizer_output", true, "The optimizer type.");
        option.setRequired(true);
        return option;
    }

    public static Option getEdgesFileSeparator() {
        return new Option(EDGES_FILE_SEPARATOR, "edge_separator", true /* hasArg */,
            "The separator between columns in the input CSV file. The default is set to ','.");
    }

    public static Option getInputGraphDirOption() {
        var option = new Option(INPUT_GRAPH_DIR, "input_graph_dir", true,
            "Absolute path to the directory of the serialized input graph.");
        option.setRequired(true);
        return option;
    }

    public static Option getInputSerializedPlanOption() {
        var option = new Option(INPUT_SER_PLAN, "input_ser_plan", true,
            "Query graph to evaluate e.g. '(a)->(b)' and '(a)->(b), (b)->(c)'");
        option.setRequired(true);
        return option;
    }

    public static Option getOutputFileOption() {
        var option = new Option(OUTPUT_FILE, "output_file", true,
            "Absolute path to the output log file.");
        option.setRequired(true);
        return option;
    }

    public static Option getSharingOperatorsEnabledOption() {
        return new Option(SHARING_OPERATORS_ENABLED, "sharing_operators_enabled", false,
            "Sharing operators enabled.");
    }

    public static Option getSharingALDsEnabledOption() {
        return new Option(SHARING_ALDS_ENABLED, "sharing_alds_enabled", false,
            "Sharing alds enabled.");
    }

    public static Option getQueriesInputFileOption() {
        var option = new Option(QUERIES, "query", true, "Queries to build a catalog.");
        option.setRequired(true);
        return option;
    }
}
