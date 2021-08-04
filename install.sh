ROUTE=$(oc get route console -n openshift-console | awk -F ' ' '{print $2}' | awk '{if(NR>1)print}')
ROUTE=$(echo ${ROUTE#*.})

# Maven builds for each project
mvn package -DskipTests -f fix-iniciator/pom.xml
mvn package -DskipTests -f fix-acceptor/pom.xml

# Login into OCP...
oc new-project poc-caja-valores

# BuildConfig fix-acceptor...
oc new-build --name fix-acceptor --binary --strategy source --image-stream java:openjdk-11-el7
oc start-build fix-acceptor --from-file=fix-acceptor/target/fix-acceptor-1.0.0-SNAPSHOT-runner.jar

# BuildConfig fix-iniciator
oc new-build --name fix-iniciator --binary --strategy source --image-stream java:openjdk-11-el7
oc start-build fix-iniciator --from-file=fix-iniciator/target/fix-iniciator-1.0.0-SNAPSHOT-runner.jar

# Validating that the acceptor and iniciator imagestreams are builded...
#TODO for/while with sleep to validate the imagestreams

sleep 30

# Add image lookup to the recently generated images, in order to pull them using a simple nomenclature y the deployment.yaml
oc set image-lookup fix-acceptor
oc set image-lookup fix-iniciator

# Deployment fix-acceptor
oc apply -f ocp/fix-acceptor/deployment.yaml
oc apply -f ocp/fix-acceptor/service.yaml

# Edit the route host before executing
sed  "/^\([[:space:]]*host: \).*/s//\1$ROUTE/" ocp/fix-acceptor/route.yaml | oc apply -f -
# oc apply -f ocp/fix-acceptor/route.yaml

# Deployment fix-iniciator
oc apply -f ocp/fix-iniciator/configmap.yaml
oc apply -f ocp/fix-iniciator/pvc.yaml
oc apply -f ocp/fix-iniciator/deployment.yaml
oc apply -f ocp/fix-iniciator/service.yaml

sleep 15

ROUTE_NAME=$(oc get route fix-acceptor -o jsonpath='{.spec.host}')

# Creating 5 dummy FIX messages
curl $ROUTE_NAME/generator?sizePerThread=5&threads=1

# Rest enpoint to see the total number of iniciators connected to the acceptor
curl $ROUTE_NAME/sessions
