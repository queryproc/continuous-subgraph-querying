#!/usr/bin/env python3
import os
import glob
import subprocess
import argparse
import shutil

from types import *

csv_delimiter = ','
graphflow_output = os.environ['GRAPHFLOW_HOME'] + '/output/'
bin_home = os.environ['GRAPHFLOW_HOME'] + '/build/install/graphflow/bin/'

def main():
    args = parse_args()
    # if args.num_of_labels:
    #    add_labels_to_query(args)
    plan_ser_dir = get_plan_ser_dir(args)
    if os.path.isdir(plan_ser_dir):
        shutil.rmtree(plan_ser_dir)
    generate_and_serialize_query_plans(args, plan_ser_dir)
    output = args.log_file
    with open(output,'w') as f:
        f.write('#Runtime(ms),NumOutTuples,NumOperators,ICost,EstimatedICost,NumInterTuples\n')
    num_plans_possible = len(glob.glob(plan_ser_dir + '*'))
    print('num plans possible: ' + str(num_plans_possible))
    if num_plans_possible > args.top:
        num_plans_possible = args.top
    print('num plans to execute: ' + str(num_plans_possible))
    for i in range(0, num_plans_possible):
        for j in range(args.runs):
            execute_query_plans(args, plan_ser_dir + 'ser_plan_' + str(i), output)
        if args.runs > 1 and args.optimizer_type != 'ALL':
            write_output_avg(output, output + '_avg', args.runs)
    extract_run_times(output)

def generate_and_serialize_query_plans(args, plan_ser_dir):
    query_plan_gen_ser_args = [
        bin_home + 'query-plan-generator',
        '-i', args.input_graph, '-q', args.queries,
        '-o', plan_ser_dir, '-t', args.optimizer_type]
    if args.sharing_operators_enabled:
        query_plan_gen_ser_args.append('-s')
    if args.sharing_ALDs_enabled:
        query_plan_gen_ser_args.append('-a')
    execute_binary(query_plan_gen_ser_args)

def execute_query_plans(args, plan, output):
    query_plan_gen_ser_args = [
        bin_home + 'query-plan-executor',
        '-i', args.input_graph, '-p', plan,
        '-e', args.input_edges, '-o', output]
    execute_binary(query_plan_gen_ser_args)

def execute_binary(binary_and_args):
    popen = subprocess.Popen(tuple(binary_and_args), stdout=subprocess.PIPE)
    popen.wait()
    for line in iter(popen.stdout.readline, b''):
        print(line.decode("utf-8"), end='')

def get_plan_ser_dir(args):
    # reduce the len of queries' string rep, e.g. (a)->(b) becomes a->b.
    queries = args.queries.split('/')
    queries = queries[len(queries) - 1].split('.')[0]
    # get the directory where serialized query graphs are written.
    return graphflow_output + 'ser_plans/' + queries + '/'

def write_output_avg(output, output_avg, runs):
    with open(output_avg,"w+") as f_avg:
        run_time_avg = 0.0
        with open(output,"r") as f:
            for line in f:
                if line[0] == '#':
                    continue
                run_time_avg += float(line.split(',')[0])
                f_avg.write(line.split(',')[0] + '\n')
        run_time_avg /= runs
        f_avg.write(str(run_time_avg))

def extract_run_times(file_path):
    run_times = []
    run_times_and_line = []
    num_out_tuples = []
    with open(file_path, 'r') as f:
        for line in f:
            line = line.strip()
            # skip blank lines or comments
            if not line or line.startswith('#'):
                continue
            parts = line.split(',')
            if len(parts) < 2:
                continue  # malformed line
            rt = parts[0]
            nt = parts[1]
            run_times.append(rt)
            run_times_and_line.append((rt, line))
            num_out_tuples.append(nt)
    print("Run times (ms): ", sorted(run_times))

    # Validate NumOutTuples consistency
    unique_counts = set(num_out_tuples)
    if len(unique_counts) == 1:
        print(f"Num output tuples: {unique_counts.pop()}")
    else:
        print(f"Error: Inconsistent NumOutTuples values found: {unique_counts}")

def parse_args():
    parser = argparse.ArgumentParser(
        description='runs all unique query plans given query graphs.')
    parser.add_argument('queries',
        help='queries to evaluate.')
    parser.add_argument('input_graph',
        help='absolute path to the serialized input graph directory.')
    parser.add_argument('input_edges',
        help='absolute path to the batched input edges directory.')
    parser.add_argument('log_file',
        help='absolute path to the output file.')
    parser.add_argument('-o', '--optimizer_type', choices=['ALL', 'BS', 'BNS', 'GR'],
        help='type of optimizer to run.', default='ALL')
    parser.add_argument('-s', '--sharing_operators_enabled',
        help='option if the query plans should share operators.', action='store_false')
    parser.add_argument('-a', '--sharing_ALDs_enabled',
        help='option if the query plans should share ALDs.', action='store_false')
    parser.add_argument('-t', '--top',
        help='number of top plans to execute.', type=int, default=1)
    parser.add_argument('-r', '--runs',
        help='number of runs.', type=int, default=1)
    return parser.parse_args()

if __name__ == '__main__':
    main()
