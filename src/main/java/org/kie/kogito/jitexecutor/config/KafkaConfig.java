package org.kie.kogito.jitexecutor.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "kafka")
public interface KafkaConfig {
    Bootstrap bootstrap();
    interface Bootstrap {
        @WithDefault("localhost:9092")
        String servers();
    }
}
