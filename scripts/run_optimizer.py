#!/usr/bin/env python3
import os
import glob
import subprocess
import argparse
import shutil
import random

bin_home = os.environ['GRAPHFLOW_HOME'] + '/build/install/graphflow/bin/'

def main():
    args = parse_args()
    if args.num_of_labels:
        add_labels_to_query(args)
    print(args.query)
    # set OptimizerRunner.java arguments and exectue the binary.
    binary_and_args = [
        bin_home + 'optimizer-executor',
        '-q', args.query, '-i', args.input_graph]
    if args.num_edges:
        binary_and_args.append('-n')
        binary_and_args.append(str(args.num_edges))
    if args.disable_flattening:
        binary_and_args.append('-f')
    if args.execute:
        binary_and_args.append('-e')
    if args.enable_adaptivity:
        binary_and_args.append('-a')
    if args.threads:
        binary_and_args.append('-t')
        binary_and_args.append(str(args.threads))
    # OptimizerExecutor from
    # Graphflow-Optimizers/src/ca.waterloo.dsg.graphflow.runner.plan:
    #     1) gets a query plan using QueryPlanner.
    #     2) Output is logged to STDOUT.
    popen = subprocess.Popen(tuple(binary_and_args), stdout=subprocess.PIPE)
    popen.wait()
    for line in iter(popen.stdout.readline, b''):
        print(line.decode("utf-8"), end='')

def add_labels_to_query(args):
    random.seed(0) # use '0' to always get the same sequence of labels  
    query = args.query
    query_edges = query.split(',')
    for idx, query_edge in enumerate(query_edges):
        label = random.randint(0, int(args.num_of_labels) - 1)
        query_edge = query_edge.split('>')
        query_edges[idx] = query_edge[0] + \
                          '[' + str(label) + ']->' + \
                           query_edge[1]
    args.query = ','.join(query_edges)

def parse_args():
    parser = argparse.ArgumentParser(
        description='runs the optimizer to evaluate a query.')
    return parser.parse_args()

if __name__ == '__main__':
    main()

