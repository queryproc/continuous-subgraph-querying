#!/usr/bin/env python3
import argparse
import random
import sys

def split_file(input_path, sample_path, rest_path, frac):
    """
    Stream through input file line by line, randomly sample each line with probability frac,
    writing sampled lines to sample_path and the rest to rest_path.
    """
    random.seed(0) # always get the same sequence of labels  
    with open(input_path, 'r') as infile, \
         open(sample_path, 'w') as sample_file, \
         open(rest_path, 'w') as rest_file:
        for line in infile:
            # Strip only the trailing newline; preserve any leading/trailing whitespace in fields
            stripped = line.rstrip('\n')
            if random.random() < frac:
                sample_file.write(stripped + '\n')
            else:
                rest_file.write(stripped + '\n')

def main():
    parser = argparse.ArgumentParser(
        description='Split a large edge-list file into two parts by random sampling.')
    parser.add_argument('input_dataset',
        help='Path to the input edge-list (src,dst,label) file')
    parser.add_argument('percent', type=float,
        help='Percentage of edges to insert (0 < percent < 100)')
    parser.add_argument('insertion_edges',
        help='Path to write the sampled X percent of edges')
    parser.add_argument('initial_edges',
        help='Path to write the remaining 100-X percent of edges')
    args = parser.parse_args()

    if not (0 < args.percent < 100):
        sys.exit('Error: percent must be between 0 and 100.')

    frac = args.percent / 100.0
    split_file(args.input_dataset, args.insertion_edges, args.initial_edges, frac)

if __name__ == '__main__':
    main()
