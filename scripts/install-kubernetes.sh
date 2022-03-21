#!/bin/bash
function install() {
   echo "Deploying jitexecutor"
   kubectl apply -f "${TARGET_DIR}/jitexecutor.yaml" -n ${PROJECT_NAME}

}
