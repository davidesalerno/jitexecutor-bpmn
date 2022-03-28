/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.jitexecutor.process.factory;

import javax.enterprise.context.ApplicationScoped;

import io.serverlessworkflow.api.events.EventDefinition;
import org.jbpm.ruleflow.core.Metadata;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.kie.kogito.jitexecutor.process.ProcessBuild;
import org.kie.kogito.jitexecutor.process.ProcessFactory;
import org.kie.kogito.jitexecutor.process.ProcessFile;
import org.kie.kogito.process.bpmn2.BpmnProcess;
import org.kie.kogito.serverless.workflow.SWFConstants;
import org.kie.kogito.serverless.workflow.parser.ServerlessWorkflowParser;
import org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.serverlessworkflow.api.Workflow;

@ApplicationScoped
public class SWFProcessFactory extends AbstractProcessFactory implements ProcessFactory {

    @Override
    public boolean accept(ProcessFile processFile) {
        return processFile.name().endsWith(".sw.json");
    }

    @Override
    public ProcessBuild createProcessDefinition(ProcessFile processFile) {
        try {
            Process processDefinition = getWorkflowParser(processFile);
            ProcessBuild processBuild = new ProcessBuild(processDefinition.getId());
            compileProcess(processBuild, processDefinition);
            BpmnProcess process = new BpmnProcess(processDefinition);
            processBuild.setProcess(process);
            return processBuild;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Process getWorkflowParser(ProcessFile processFile) throws JsonProcessingException {
        Workflow workflow = ServerlessWorkflowUtils.getObjectMapper("json").readValue(processFile.content(), Workflow.class);
        ServerlessWorkflowParser parser = ServerlessWorkflowParser.of(workflow, JavaKogitoBuildContext.builder().build());
        RuleFlowProcess process = (RuleFlowProcess) parser.getProcessInfo().info();
        for (Node node : process.getStartNodes()) {
            if (node.getMetaData().get(Metadata.TRIGGER_REF) != null) {
                node.getMetaData().put(Metadata.TRIGGER_MAPPING_INPUT, SWFConstants.DEFAULT_WORKFLOW_VAR);
            }
        }
        process.setMetaData("ServerlessWorkflow", Boolean.TRUE);
        return process;
    }
}
