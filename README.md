# Stream Processing example with Flink, Kafka and Python

This repository contains the components for a simple streaming pipeline:
 * Generate data and write it to Apache Kafka
 * Process the generated data from Kafka using Apache Flink
 * Write the results back to Kafka for further processing
 * Analyze the results from Kafka using Ipython Notebook

**Prerequisites**
 * JDK 7+
 * Maven 3.x
 * Scala 2.10
 * Python 2.7
 * IPython notebook

### Running the example:
 1. Download & install [Apache Kafka](https://kafka.apache.org/08/quickstart.html)
 2. Install the Python kafka client

    ```bash
    pip install kafka-python
    ```
 3. Clone the repository and build a jar

     ```bash
    git clone https://github.com/gyfora/summer-school.git
    cd summer-school/flink
    mvn assembly:assembly
    ```
 4. Start the Kafka server and create the topics

     ```bash
    cd <KAFKA-DIR>
    # Start Zookeper server
    bin/zookeeper-server-start.sh config/zookeeper.properties
    # Start Kafka server
    bin/kafka-server-start.sh config/server.properties
    # Create input and output topics
    bin/kafka-topics.sh --create --topic input --replication-factor 1 --partitions 1 --zookeeper localhost:2181
    bin/kafka-topics.sh --create --topic output_avg --replication-factor 1 --partitions 1 --zookeeper localhost:2181
    bin/kafka-topics.sh --create --topic output_max --replication-factor 1 --partitions 1 --zookeeper localhost:2181
    ```
 5. Start Flink streaming job

    ```bash
    cd <git>/summer-school/flink
    # We use a Flink minicluster now, but this jar could be submitted to a proper Flink cluster as well
    java -classpath target/flink-kafka-example-1.0.0-jar-with-dependencies.jar summerschool.FlinkKafkaExample
    ```
 6. Start Data generator

    ```bash
    cd <git>/summer-school/flink
    java -classpath target/flink-kafka-example-1.0.0-jar-with-dependencies.jar summerschool.DataGenerator
    ```
 7. Start IPython Notebook to further analyse the results

    ```bash
    cd <git>/summer-school/python
    ipython notebook
    ```