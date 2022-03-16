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

package org.kie.kogito.jitexecutor.process.api;

import java.time.Duration;
import java.util.Collections;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;


import static io.restassured.RestAssured.given;

@QuarkusTest
public class StatelessProcessResourceTest {

    @BeforeEach
    public void bootstrap() {
        Response response = given()
                .contentType(ContentType.JSON)
                .when().post("/runtime/compile");

        response.then()
                .statusCode(200);
    }

    @Test
    public void testApplicantWorkflow() throws Exception {

        try (KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(KafkaUtil.createProducerProperties())) {
            kafkaProducer.send(new ProducerRecord<String, String>("applicants", "applicants", "{ \"salary\" : 3500 } ")).get();
        }

        String value = null;
        try (KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(KafkaUtil.createConsumerProperties())) {
            kafkaConsumer.subscribe(Collections.singleton("decisions"));
            for (ConsumerRecord<String, String> record : kafkaConsumer.poll(Duration.ofMillis(5000))) {
                value = record.value();
            }
        }
        // straight through process
        Assertions.assertNotNull(value);
        Assertions.assertEquals("{\"salary\":3500,\"decision\":\"Approved\"}", value);
    }



}
