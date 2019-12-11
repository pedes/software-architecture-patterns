#!/bin/bash

echo "build order service..."
cd order
mvn install
cp dependencies/lra-client-5.9.8.Final-helidon.jar target/libs/lra-client-5.9.8.Final.jar
cp dependencies/narayana-lra-5.9.8.Final-helidon.jar target/libs/narayana-lra-5.9.8.Final.jar
cd ../

echo "build inventory service..."
cd inventory
mvn install
cp dependencies/lra-client-5.9.8.Final-helidon.jar target/libs/lra-client-5.9.8.Final.jar
cp dependencies/narayana-lra-5.9.8.Final-helidon.jar target/libs/narayana-lra-5.9.8.Final.jar
cd ../

echo "build LRA coordinator service..."
cd lra-coordinator-helidon
mvn install
./build-lra-coordinator-helidon-jar.sh
cd ../
