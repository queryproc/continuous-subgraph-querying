#!/bin/bash

dataset="amazon"
queryset="SEED"
JAVA_OPTS="-Xmx200G" python3 gen_and_run_query_plans.py "/home/mamhedhb/Documents/datasets/queries/${queryset}" "/home/mamhedhb/Documents/datasets/${dataset}/label_1/${dataset}-90-bin" "/home/mamhedhb/Documents/datasets/${dataset}/label_1/${dataset}-10.txt" -o GR -a
