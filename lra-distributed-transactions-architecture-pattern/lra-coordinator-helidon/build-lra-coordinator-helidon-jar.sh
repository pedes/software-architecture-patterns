#!/bin/bash

echo "adding lra-coordinator-5.9.8.Final.war classes to lra-coordinator-helidon-0.0.1-SNAPSHOT.jar ... "
mkdir lra-coordinator-helidon-jarbuild
cp dependencies/lra-coordinator-5.9.8.Final-helidon.war lra-coordinator-helidon-jarbuild/
cd lra-coordinator-helidon-jarbuild/
jar xf lra-coordinator-5.9.8.Final-helidon.war
cd WEB-INF/classes/
jar uf ../../../target/lra-coordinator-helidon-0.0.1-SNAPSHOT.jar *
cd ../../../
rm -rf lra-coordinator-helidon-jarbuild
