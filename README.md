# BPMN Runner

Required a kafka server running in 9092 to execute StatelessProcessResourceTest

bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties

create topics

bin/kafka-topics.sh --create --topic applicants --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic decisions --bootstrap-server localhost:9092


# Usage

java -Dorg.kie.deployment.path=<your deployment path> -jar target/workflow-runner.jar

# Deployment

cp <sw.json> <your deployment path>

you will see:

10:54:38 INFO  [or.ki.ko.ji.pr.JITProcessServiceImpl] (pool-7-thread-1) org.kie.kogito.jitexecutor.process.JITProcessServiceImpl
10:54:38 INFO  [or.ki.ko.ji.pr.JITProcessServiceImpl] (pool-7-thread-1) building processes
10:54:38 INFO  [or.ki.ko.ji.pr.JITProcessServiceImpl] (pool-7-thread-1) creating process definition for File [applicantworkflow.sw.json]
10:54:38 INFO  [or.ki.ko.ji.pr.JITProcessServiceImpl] (pool-7-thread-1) deployed processes are: applicantworkflow
10:54:38 INFO  [or.ki.ko.ji.pr.KafkaManager] (pool-7-thread-1) subscribing to kafka topic applicants

you can add additional deployments anytime


