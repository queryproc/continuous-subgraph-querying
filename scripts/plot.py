#!/usr/bin/env python3
import argparse

import sys
import collections
import matplotlib
import matplotlib.pyplot as plt
import numpy as np

font = {#'weight' : 'bold',
        'size'   : 14}
matplotlib.rc('font', **font)

def parse_args():
    parser = argparse.ArgumentParser(
        description='given multiple output file abostule paths, '\
                    'plots the plan spectrum chart.')
    parser.add_argument('files', nargs='+', help='output.data files to plot')
    parser.add_argument('-o', '--output', help='output directory to save to')
    parser.add_argument('-l', '--labels', nargs='+', help='labels to plot')
    parser.add_argument('-w', '--width',
        help="figure's width", type=float, default=2)
    parser.add_argument('-e', '--height',
        help="figure's height", type=float, default=3)
    parser.add_argument('-j', '--jitter',
        help="jitter to add", type=float, default=0.08)
    parser.add_argument('-m', '--marker',
        help='the marker used in the figure', default='.')
    parser.add_argument('-f', '--fontsize', help='the font size')
    parser.add_argument('-p', '--plot',
        choices=['spectrum', 'icost_correlation'], help='the type of plot',
        default='spectrum')
    parser.add_argument('-x', '--optimizer_category',
        help='optimizer category choice', type=int)
    parser.add_argument('-z', '--optimizer_run_time',
        help='optimizer run-time', type=float)
    parser.add_argument('-y', '--adaptive_optimizer_category',
        help='adaptive optimizer category choice', type=int)
    parser.add_argument('-v', '--adaptive_optimizer_run_time',
        help='adaptive optimizer run-time', type=float)
    return parser.parse_args()

def get_data(data_file):
    y_data = []
    with open(data_file) as f:
        for line in f:
            if line[0] == '#':
                continue
            try:
                line = line.split("\n")[0]   # remove '\n'
                line = line.split(",")
                y_data.append(float(line[0])/1000)
            except:
                pass # first line
    print(y_data)
    # y_data.sort()
    return y_data

def plot_spectrum(jitter, width, height, output_file, files, labels, marker, args):
    plan_labels = []
    data_to_plot = []
    x = []
    number_plans = 0
    for idx, data_file in enumerate(files):
        data = get_data(data_file)
        data_to_plot.append(np.array(data))
        number_plans = len(data)
        x.append(idx)
        plan_labels.append(labels[idx] + '(' + str(number_plans) + ')')
    plt.figure(figsize=(width, height))
    plt.rcParams['axes.xmargin'] = 0
    ax = plt.gca()
    ax.grid
    ax.tick_params(axis='x', which='major', pad=3)
    #ax.set_xlabel('Plans', fontsize=15)
    ax.set_ylabel('Run time (secs)', fontsize=15)
    for i in x:
        x_axis = np.random.normal(1+i, jitter, size=len(data_to_plot[i]))
        plt.plot(x_axis, data_to_plot[i], 'r.', alpha=0.8,marker=marker)
    for idx, i in enumerate(x):
        x[idx] = i + 1
    if args.optimizer_category:
        run_time = args.optimizer_run_time/1000
        plt.plot(args.optimizer_category, run_time, run_time, 'r.',\
                 alpha=0.8,color='black', markersize=6,marker='x')
    if args.adaptive_optimizer_category:
        plt.plot(args.adaptive_optimizer_category, args.adaptive_optimizer_run_time,
                 args.adaptive_optimizer_run_time, 'r.',\
                 alpha=0.8,color='black', markersize=6,marker='x')
    plt.yticks(rotation=45)
    plt.xticks(x, plan_labels)
    plt.xlim(x[0] - 0.5, x[len(x) - 1] + 0.5)
    plt.savefig(fname=output_file, format='pdf', bbox_inches='tight')

def main():
    args = parse_args()
    if args.plot == 'spectrum' and len(args.files) != len(args.labels):
        print("number of labels does not match number of files.")
        return
    if args.plot == 'spectrum':
        plot_spectrum(args.jitter, args.width, args.height, args.output,
            args.files, args.labels, args.marker, args)
    else:
        plot_icost_correlation(args.jitter, args.width, args.height,
            args.output, args.files[0], args.marker)

if __name__ == '__main__':
    main()

