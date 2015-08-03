# Stream Processing example with Flink, Kafka and Python

This repository contains the components for a simple streaming pipeline:
 * Generate data and write it to Apache Kafka
 * Process the generated data from Kafka using Apache Flink
 * Write the results back to Kafka for further processing
 * Analyze the results from Kafka using Ipython Notebook

### Description

In this very simple example we will analyse temperature data, generated for European cities in a streaming fashion.

Data is generated as simple Strings in the format of: `"City, Temperature"`
<br>`"Budapest, 30", "Stockholm, 20", "Budapest, 32" …` 

Our goal is to analyse the incoming data in a continuous fashion, updating our statistics as new data becomes available. 

We generate random temperatures using a [simple Scala program](https://github.com/gyfora/summer-school/blob/master/flink/src/main/scala/summerschool/DataGenerator.scala), which writes it directly to Kafka, making it available for processing.

We then use a [Flink program](https://github.com/gyfora/summer-school/blob/master/flink/src/main/scala/summerschool/FlinkKafkaExample.scala) to do the following processing steps:

1. Parse the incoming String into the Scala case class `Temp(city: String, temp: Double)`:

   ```
   "Budapest, 30" -> ("Budapest", 30)
   "Stockholm, 20" -> ("Stockholm", 20)
   ```
   
   *Note: An important consideration we need to make when implementing the parsing step is that it should be robust to errors coming from incorrect input formats.*
2. We compute a historical average of the temperatures for each city:

   ```
   ("Budapest", 30) -> Avg: (“Budapest", 30)
   ("Budapest", 40) -> Avg: (“Budapest", 35)
   (“Stockholm”, 20) -> Avg: (“Stockholm”, 20)
   ("Budapest", 37) -> Avg: (“Budapest", 35.67)
   (“Stockholm”, 22) -> Avg: (“Stockholm”, 21)
   ```
   *Note: There is distinctive property of computing a historical average compared to the parsing operator. Here we need to keep some computational state for each city (sum and count) so we are able to update the average when the next element arrives.*

    *You can read more about the interesting topic of operator states and stateful stream processing in [this wiki article](https://cwiki.apache.org/confluence/display/FLINK/Stateful+Stream+Processing).*
3. We compute the current global maximum temperature every 5 seconds:
   
    ```
   ("Budapest", 32) 
   ("Madrid", 34)     -> Max: (“Madrid", 34)
   ("Stockholm", 20)
   -----------------------------------------
   ("Budapest", 36) 
   ("Madrid", 33)     -> Max: (“Budapest", 36)
   ("Stockholm", 23)
   ```
   *Note: This is a typical example of window computations, when the data stream is discretised into small batches (windows) and some sort of aggregation or transformation is applied independently on each window.*
4. The computed statistics are written back to Kafka and can be further analysed. In this example we use [IPython notebook](https://github.com/gyfora/summer-school/blob/master/python/KafkaExample.ipynb) to provide basic visualisation.

### Running the example:

**Prerequisites**: *JDK 7+, Maven 3.x, Scala 2.10, Python 2.7, IPython notebook*

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
    bin/kafka-topics.sh --create --topic input --partitions 1 --replication-factor 1 --zookeeper localhost:2181
    bin/kafka-topics.sh --create --topic output_avg --partitions 1 --replication-factor 1 --zookeeper localhost:2181
    bin/kafka-topics.sh --create --topic output_max --partitions 1 --replication-factor 1 --zookeeper localhost:2181
    ```
 5. Start Flink streaming job

    ```bash
    # Running on a Flink mini cluster (equivalent to executing from the IDE) 
    cd <git>/summer-school/flink
    java -classpath target/FlinkExample.jar summerschool.FlinkKafkaExample
    ```

    To execute the job on an already running cluster ([cluster setup guide](https://ci.apache.org/projects/flink/flink-docs-master/setup/local_setup.html)) we can either use the Flink command-line or web-client:

    ```bash
    # Start a local Flink cluster
    cd <FLINK-DIR>
    bin/start-cluster-streaming.sh

    # Run the example job using the command-line client
    bin/flink run -c summerschool.FlinkKafkaExample <summer-school-dir>/flink/target/FlinkExample.jar
    ```

 6. Start Data generator

    ```bash
    cd <git>/summer-school/flink
    java -classpath target/FlinkExample.jar summerschool.DataGenerator
    ```
 7. Start IPython Notebook to further analyse the results

    ```bash
    cd <git>/summer-school/python
    ipython notebook
    ```