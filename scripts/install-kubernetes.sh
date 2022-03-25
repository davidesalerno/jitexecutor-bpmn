#!/bin/bash
function install() {
   echo "Deploying workflow-runner"
   $cmd apply -f "${TARGET_DIR}/runner.yaml" -n ${PROJECT_NAME}

}
