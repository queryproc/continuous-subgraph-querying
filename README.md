Graphflow Optimizers
--------------------

<img src="docs/img/graphflow.png" height="181px" weight="377">

Table of Contents
-----------------
  * [Overview](#overview)
  * [Dependencies](#dependencies)
  * [Contact](#contact)
  * [License](#license)

Overview
-----------------
This repo is a based on [Graphflow](http://graphflow.io/) and is used to study and experiment with subgraph matching query optimization in the absence of predicate filters. The query optimization studied is for both one-time queries (queries running on the whole graph) and continuous time queries (similar to [triggers](https://www.essentialsql.com/what-is-a-database-trigger/) in the SQL world).    

A full description is in our [paper](#). A brief overview is as follows:
blabla...

p.s. For an overview on Graphflow, check our [demo paper](http://dl.acm.org/authorize?N37400) at [SIGMOD 2017](http://sigmod2017.org/).

Dependencies
-----------------
TODO.

Build steps
-----------------
* To do a full clean build: `./gradlew clean build installDist`
* All subsequent builds: `./gradlew build installDist`

Running experiments
-----------------
TODO.

Contact
-----------------
[Amine Mhedhbi](http://amine.io/)

License
-----------------
Graphflow-Optimizers is an open source software under the Apache 2.0 license.
