#!/bin/sh

./neo4j-community-3.5.13/bin/neo4j stop

rm -r neo4j-community-3.5.13/data/databases/graph.db

# neo4j-community-3.5.13/bin/neo4j-admin import --database=testdb --id-type=STRING --nodes:Node nodes.csv --relationships=rels.csv --relationships=cpg_edges.csv
# neo4j-community-3.5.13/bin/neo4j-admin import --id-type=STRING --nodes=nodes.csv --delimiter='\t' --relationships=rels.csv --relationships=cpg_edges.csv
neo4j-community-3.5.13/bin/neo4j-import --id-type=STRING --nodes=nodes.csv --delimiter='\t' --relationships=rels.csv --relationships=cpg_edges.csv --multiline-fields=true --into=neo4j-community-3.5.13/data/databases/graph.db

./neo4j-community-3.5.13/bin/neo4j start
