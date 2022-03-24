#!/bin/bash
# Setting working dir to parent project directory
cd "$(dirname ../../)"

echo "########################################################################"
echo "#                                                                      #"
echo "#  Please be sure that you have got a Kubernetes cluster with the OLM  #"
echo "#  If you are using minikube:                                          #"
echo "#                                                                      #"
echo "#  minikube start                                                      #"
echo "#  minikube addons enable olm                                          #"
echo '#  eval $(minikube docker-env)                                         #'
echo "#                                                                      #"
echo "########################################################################"

./mvnw clean package -DskipTests -Dquarkus.container-image.build=true

if "$DRY_RUN"; then
    cmd=echo
else
    cmd=kubectl
fi

PROJECT_NAME=$2
if [ "x$2" == "x"  ]; then
     echo "Setting project/namespace name to sw-runner-poc"
     PROJECT_NAME="sw-runner-poc"
fi
$cmd create namespace "$PROJECT_NAME"

TARGET_DIR="./kubernetes/resources"
SCRIPTS_DIR="./scripts"
STRIMZI_VERSION=0.28.0

echo "Installing Strimzi Operator"
wget "https://github.com/strimzi/strimzi-kafka-operator/releases/download/${STRIMZI_VERSION}/strimzi-${STRIMZI_VERSION}.tar.gz" -P "$TARGET_DIR/strimzi"
tar zxf "${TARGET_DIR}/strimzi/strimzi-${STRIMZI_VERSION}.tar.gz" -C "$TARGET_DIR"
sed -i 's/namespace: .*/namespace: '"${PROJECT_NAME}"'/' "${TARGET_DIR}"/strimzi-"${STRIMZI_VERSION}"/install/cluster-operator/*RoleBinding*.yaml
$cmd apply -f "${TARGET_DIR}/strimzi-${STRIMZI_VERSION}/install/cluster-operator/" -n ${PROJECT_NAME}

echo "Sleeping in order to have the strimzi operator up and running"
if [ "$dry_run" = true ]; then
    sleep 5m
fi

echo "Creating Kafka cluster"
$cmd apply -f "${TARGET_DIR}/kafka.yaml" -n ${PROJECT_NAME}

if [ "$dry_run" = true ]; then
    sleep 3m
fi

echo "Creating Kafka topics"
$cmd apply -f "${TARGET_DIR}/kafka-topics.yaml" -n ${PROJECT_NAME}

echo "Sleeping in order to have the kafka cluster with the topics created"
if [ "$dry_run" = true ]; then
    sleep 1m
fi

echo "Creating configmap"
$cmd create configmap input-sw --from-file="${TARGET_DIR}/configmap/applicantworkflow.sw.json" -n ${PROJECT_NAME}

if [[ "x$1" == "x" ]] || [[ "$1" == "kubernetes" ]] ; then
     echo "Continuing setup for kubernetes"
     source "${SCRIPTS_DIR}/install-kubernetes.sh"
elif [ "$1" == "knative"  ]; then
     echo "Continuing setup for knative"
     TARGET_DIR="./knative/resources"
     source "${SCRIPTS_DIR}/install-knative.sh"
fi

install