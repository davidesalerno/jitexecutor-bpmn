# BPMN Runner

## Requirements

Required a kafka server running in 9092 to execute StatelessProcessResourceTest

```bin/zookeeper-server-start.sh config/zookeeper.properties```
```bin/kafka-server-start.sh config/server.properties```

create topics

```bin/kafka-topics.sh --create --topic applicants --bootstrap-server localhost:9092```
```bin/kafka-topics.sh --create --topic decisions --bootstrap-server localhost:9092```


## Usage

```java -Dorg.kie.deployment.path=<your deployment path> -jar target/workflow-runner.jar```

## Deployment

```cp <sw.json> <your deployment path>```

you will see:

```
10:54:38 INFO  [or.ki.ko.ji.pr.JITProcessServiceImpl] (pool-7-thread-1) org.kie.kogito.jitexecutor.process.JITProcessServiceImpl
10:54:38 INFO  [or.ki.ko.ji.pr.JITProcessServiceImpl] (pool-7-thread-1) building processes
10:54:38 INFO  [or.ki.ko.ji.pr.JITProcessServiceImpl] (pool-7-thread-1) creating process definition for File [applicantworkflow.sw.json]
10:54:38 INFO  [or.ki.ko.ji.pr.JITProcessServiceImpl] (pool-7-thread-1) deployed processes are: applicantworkflow
10:54:38 INFO  [or.ki.ko.ji.pr.KafkaManager] (pool-7-thread-1) subscribing to kafka topic applicants
```
you can add additional deployments anytime

## Cloud enablement
### Image
An image of this service is available on Quay.io
- https://quay.io/repository/dsalerno/workflow-runner

### General considerations
The workflow in the deployment can be pushed using a config map. 

You can quickly create a ConfigMap using the command:

``` kubectl create configmap input-sw --from-file="<path you your sw.json>" -n <namespace> ```

An example to mount the ConfiMap into the required path could be found in the Deployment:
- kubernetes/resources/runner.yaml

If you want the service up and running with the test applicant workflow (applicantworkflow.sw.json) and Kafka with the needed topics you 
jump to the next section or look at the ```scripts/install.sh``` file. 

By default Kubernetes it will be the target env and sw-runner-poc the working namespace.

### Kubernetes
#### Prerequisites
You will need:
- Java 11+ installed
- Environment variable JAVA_HOME set accordingly
- Maven 3.8.1+ installed
- a Kubernetes cluster up ad running ([minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/) and/or [crc](https://developers.redhat.com/products/codeready-containers/overview) are also fine)
- the OLM component active (Operator Lifecycle Manager)
#### Install
```
cd scritps
./install.sh
```
### Knative
#### Prerequisites
You will need:
- Java 11+ installed
- Environment variable JAVA_HOME set accordingly
- Maven 3.8.1+ installed
- a Kubernetes cluster up ad running ([minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/) and/or [crc](https://developers.redhat.com/products/codeready-containers/overview) are also fine)
- the OLM component active (Operator Lifecycle Manager)
- [Knative serving](https://knative.dev/docs/install/)
- [Knative eventing]((https://knative.dev/docs/install/))
- 
#### Install
```
cd scritps
./install.sh knative
```

#### Test
To test the **applicant** workflow you can push some events to Kafka **applicants** topic and see that Knative will deploy 
a pod of the workflow-runner that will run the workflow with the provided data and push the result on the Kafka 
**decisions** topic.

Supposing that you PoC is running on namespace **sw-runner-poc**, you can start a Kafka producer with the command:

```kubectl -n sw-runner-poc run kafka-producer -ti --image=strimzi/kafka:0.14.0-kafka-2.3.0 --rm=true --restart=Never -- bin/kafka-console-producer.sh --broker-list my-cluster-kafka-bootstrap:9092 --topic applicants ```

In a similar way you can start a Kafka consumer with the command:

```kubectl -n sw-runner-poc -ti --image=quay.io/strimzi/kafka:0.28.0-kafka-3.1.0 --rm=true --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic decisions --from-beginning```