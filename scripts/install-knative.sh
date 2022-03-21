#!/bin/bash
function install() {
   echo "Creating KafkaBinding"
   kubectl apply -f "${TARGET_DIR}/kafka-binding.yaml" -n ${PROJECT_NAME}

   echo "Creating Knative serving resources"
   kubectl apply -f "${TARGET_DIR}/knative.yaml" -n ${PROJECT_NAME}

   echo "Creating KafkaSource with sink binding"
   kubectl apply -f "${TARGET_DIR}/kafka-source.yaml" -n ${PROJECT_NAME}

}
