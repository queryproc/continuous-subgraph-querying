# Optimizing Subgraph Queries by Combining Binary and Worst-Case Optimal Joins

Table of Contents
-----------------
  * [Overview](#Overview)
  * [Build Steps](#Build-Steps)
  * [Executing Queries](#Executing-Queries)
  * [Contact](#contact)

Overview
-----------------
For an overview of our continuous subgraph matching optimizer, check our [paper](https://amine.io/assets/papers/wco-optimizers-tods21.pdf).    
We study the problem of multi-query optimization using the new worst-case optimal join plans. Worst-case optimal plans evaluate queries by matching one query vertex at a time using multi-way intersections. The core problem in optimizing worst-case optimal plans is to pick an ordering of the query vertices to match. We design a greedy cost-based optimizer that builds on the delta subgraph query framework. Given a set of continuous queries, our optimizer decomposes these queries into multiple delta subgraph queries, picks a plan for each delta query, and generates a single combined plan that evaluates all of the queries. Our combined plans share computations across operators of the plans for the delta queries if the operators perform the same intersections. To increase the amount of computation shared, we describe an additional optimization that shares partial intersections across operators.

Build Steps
-----------------
* To do a full clean build: `./gradlew clean build installDist`
* All subsequent builds: `./gradlew build installDist`

Executing Queries
-----------------
### Getting Started
After building, run the following command in the project root directory:
```
. ./env.sh
```
You can now move into the scripts folder to load a dataset and execute queries:
```
cd scripts
```  

### Dataset Preperation
A dataset may consist of two files: (i) a vertex file, where IDs are from 0 to N and each line is of the format (ID,LABEL); and (ii) an edge file where each line is of the format (FROM,TO,LABEL). If the vertex file is omitted, all vertices are assigned the same label. We mainly used datasets from [SNAP](https://snap.stanford.edu/). The `change_sldc_to_csv.py` and `serialize_dataset.py` scripts lets you load datasets from csv files and serialize them to the appropriate format for quick subsequent loading. `change_sldc_to_csv` can be used in adding labels to vertices and edges. 

To load and serialize a dataset from a single edges files, run the following commands in the `scripts` folder:
```
python3 serialize_dataset.py /absolute/path/snap_file.txt /absolute/path/edges.csv    
python3 serialize_dataset.py /absolute/path/edges.csv /absolute/path/data
```

The system will assume that all vertices have the same label in this case. The serialized graph will be stored in the `data` directory. If the dataset consists of an edges file and a vertices file, the following command can be used instead:
```
python3 serialize_dataset.py /absolute/path/edges.csv /absolute/path/data -v /absolute/path/vertices.csv
```
After running one of the commands above, a catalog can be generated for the optimizer using the `serialize_catalog.py` script based on the input queries since those sitting queries are the ones we care to build statistics for.
```
python3 serialize_catalog.py /absolute/path/data /absolute/path/queries
```

### Executing Queries

```
python3 gen_and_run_query_plans.py /absolute/path/queries /absolute/path/data /absolute/path/edges_to_insert /absolute/path/log_file -r 3
```

### Requiring More Memory
Note that the JVM heap by default is allocated a max of 2GB of memory. Changing the JVM heap maximum size can be done by prepending JAVA_OPTS='-Xmx500G' when calling any of the python scripts:
```
JAVA_OPTS='-Xmx500G' python3 serialize_catalog.py /absolute/path/data  
```

Contact
-----------------
[Amine Mhedhbi](http://amine.io/)

License
-----------------
This software is released under the Apache 2.0 license.
