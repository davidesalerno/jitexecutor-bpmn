#!/bin/bash
function install() {
   echo "Deploying jitexecutor"
   $cmd apply -f "${TARGET_DIR}/jitexecutor.yaml" -n ${PROJECT_NAME}

}
