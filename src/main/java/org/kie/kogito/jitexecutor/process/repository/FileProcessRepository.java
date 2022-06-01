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

package org.kie.kogito.jitexecutor.process.repository;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.drools.util.IoUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.kogito.jitexecutor.process.ProcessFile;
import org.kie.kogito.jitexecutor.process.ProcessRepository;

import io.quarkus.logging.Log;

@Singleton
public class FileProcessRepository implements ProcessRepository {

    @ConfigProperty(name = "org.kie.deployment.path", defaultValue = "src/test/resources")
    protected String path;

    @PostConstruct
    public void init() {
        Log.infov("path for deployment is {0}", path);
    }

    @Override
    public List<ProcessFile> processes() {
        List<ProcessFile> processes = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(Paths.get(path))) {
            processes.addAll(
                    walk.filter(Files::isRegularFile)
                            .map(this::toProcessFile)
                            .collect(Collectors.toList()));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return processes;
    }

    private ProcessFile toProcessFile(Path path) {
        try {
            String content = new String(IoUtils.readBytesFromInputStream(new FileInputStream(path.toFile())));
            return new ProcessFile(path.getFileName().toString(), content);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<ProcessFile> processesChangedSince(Date date) {
        List<ProcessFile> processes = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(Paths.get(path))) {
            processes.addAll(walk.filter(Files::isRegularFile)
                    .filter(e -> date == null || e.toFile().lastModified() > date.getTime())
                    .map(this::toProcessFile)
                    .collect(Collectors.toList()));

        } catch (IOException e) {
            Log.warn("No file in the input folder " + path);
        }
        return processes;
    }

}
