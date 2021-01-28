#!/bin/bash

# Get properties from config file
source './pipeline.config.properties'

echo 'Starting LinkedPipes with following properties:'
echo '----------------------------------------------'
echo "Host machine IP: $HOST_IP"
echo "GraphDB Repository URL: $GRAPHDB_REPO_URI"
echo "GraphDB User: $GRAPHDB_USER"
hidden_pass=$(printf "%-${#GRAPHDB_PWD}s" "*")
echo "GraphDB Password: ${hidden_pass// /*}"

# Update ./configuration.properties
sed -i "s|{HOST_IP}:|${HOST_IP}:|" ./configuration.properties

# Update ./options/*
cd ./data/options/
find . -name '*.jsonld' | xargs sed -i "s|{HOST_IP}:|${HOST_IP}:|"
cd ../../

# Update ./pipelines/*
cd ./data/pipelines/
find . -name '*.trig' | xargs sed -i "s|{HOST_IP}:|${HOST_IP}:|"
find . -name '*.trig' | xargs sed -i "s|{GRAPHDB_REPO_URI}|${GRAPHDB_REPO_URI}|"
find . -name '*.trig' | xargs sed -i "s|{GRAPHDB_USER}|${GRAPHDB_USER}|"
find . -name '*.trig' | xargs sed -i "s|{GRAPHDB_PWD}|${GRAPHDB_PWD}|"
cd ../../

# Update ./templates/*
cd ./data/templates/
find . -name '*.trig' | xargs sed -i "s|{HOST_IP}:|${HOST_IP}:|"
find . -name '*.trig' | xargs sed -i "s|{GRAPHDB_REPO_URI}|${GRAPHDB_REPO_URI}|"
find . -name '*.trig' | xargs sed -i "s|{GRAPHDB_USER}|${GRAPHDB_USER}|"
find . -name '*.trig' | xargs sed -i "s|{GRAPHDB_PWD}|${GRAPHDB_PWD}|"
cd ../../
