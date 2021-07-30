# FIX messaging over Openshift
## _This is a PoC for testing a FIX Messaging architecture over Openshift_

[![stability][0]][1]

This is a PoC intended to test an architecture that will run a FIX Messaging System with Iniciators and Acceptors over Openshift.

## Installation

Execute (manually) the following script or package it as a .sh file (not tested as shell scrip yet)

```sh
OCP_API_URL=https://api.cluster-5987.5987.sandbox1736.opentlc.com:6443

# Local clone the GIT repo
git clone https://github.com/tgubeli/poc-caja-de-valores.git
cd poc-caja-de-valores

# Maven builds for each project
cd fix-iniciator
mvn package -DskipTests
cd ../fix-acceptor
mvn package -DskipTests

# Login into OCP...
oc login $OCP_API_URL
oc new-project poc-caja-valores

# BuildConfig fix-acceptor...
oc new-build --name fix-acceptor --binary --strategy source --image-stream java:openjdk-11-el7
oc start-build fix-acceptor --from-file=./target/fix-acceptor-1.0.0-SNAPSHOT-runner.jar

# BuildConfig fix-iniciator
cd ../fix-iniciator
oc new-build --name fix-iniciator --binary --strategy source --image-stream java:openjdk-11-el7
oc start-build fix-iniciator --from-file=./target/fix-iniciator-1.0.0-SNAPSHOT-runner.jar

# Validating that the acceptor and iniciator imagestreams are builded...
#TODO for/while with sleep to validate the imagestreams

# Add image lookup to the recently generated images, in order to pull them using a simple nomenclature y the deployment.yaml
oc set image-lookup fix-acceptor
oc set image-lookup fix-iniciator

# Deployment fix-acceptor
cd ../ocp/fix-acceptor
oc apply -f deployment.yaml
oc apply -f service.yaml

# Edit the route host before executing
oc apply -f route.yaml

# Deployment fix-iniciator
cd ../fix-iniciator
oc apply -f configmap.yaml
oc apply -f pvc.yaml
oc apply -f deployment.yaml
oc apply -f service.yaml
```

If all the above ran well, let's test it

```sh
ROUTE_NAME=$(oc get route fix-acceptor -o jsonpath='{.spec.host}')

# Creating 5 dummy FIX messages
curl $ROUTE_NAME/generator?sizePerThread=5&threads=1

# Rest enpoint to see the total number of iniciators connected to the acceptor
curl $ROUTE_NAME/sessions
```
