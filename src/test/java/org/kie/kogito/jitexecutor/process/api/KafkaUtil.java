package org.kie.kogito.jitexecutor.process.api;

import org.kie.kogito.jitexecutor.config.KafkaConfig;

import javax.inject.Inject;
import java.util.Properties;

public class KafkaUtil {

    String servers;

    public KafkaUtil(String servers) {
        this.servers=servers;
    }

    public Properties createConsumerProperties() {
        Properties props = createCommonProperties();
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return props;
    }

    public Properties createProducerProperties() {
        Properties props = createCommonProperties();
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return props;
    }

    public Properties createCommonProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", servers);
        props.put("group.id", "group-id");
        return props;
    }
}
