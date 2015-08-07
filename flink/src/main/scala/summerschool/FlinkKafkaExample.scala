/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package summerschool

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka._
import org.apache.flink.streaming.connectors.kafka.api._
import org.apache.flink.streaming.util.serialization.SerializationSchema
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import scala.util.Random
import org.apache.flink.streaming.api.scala.windowing.Time
import java.util.concurrent.TimeUnit.SECONDS

object FlinkKafkaExample {

  case class Temp(city: String, temp: Double)

  def main(args: Array[String]): Unit = {

    // We get the current environment. When executed from the IDE this will create a Flink mini-cluster,    
    // otherwise it accesses the current cluster environment. 
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    // Connect to Kafka and read the inputs (unparsed strings containing current temperature)
    val source = env.addSource(new KafkaSource[String]("localhost:2181", "input", new SimpleStringSchema))
    
    // Parse the text input minding the possible parsing errors
    val parsed: DataStream[Either[String, Temp]] = source.map(in =>
      {
        try {
          val split = in.split(",")
          Right(Temp(split(0), split(1).toDouble))
        } catch {
          case e: Exception => Left(in)
        }
      })

    // Print parsing errors to the console 
    parsed.filter(_.isLeft).map(_.left.get).map("Error while parsing: " + _).print

    val temps: DataStream[Temp] = parsed.filter(_.isRight).map(_.right.get)

    // Compute the current average of each city's temperature and output every 10th update/city
    val avgTemps: DataStream[Temp] = temps.keyBy("city").flatMapWithState((in, state: Option[(Double, Long)]) =>
      {
        val s = state.getOrElse((0.0, 0L))
        val u = (s._1 + in.temp, s._2 + 1)
        (if (s._2 % 10 == 0) List(Temp(in.city, u._1 / u._2)) else (List()), Some(u))
      })

    // Compute the current global maximum in every 5 second interval
    val globalMax = temps.window(Time.of(5, SECONDS)).maxBy("temp").flatten

    // Write the results to the respective Kafka topics
    avgTemps.addSink(new KafkaSink("localhost:9092", "output_avg", ss))
    globalMax.addSink(new KafkaSink("localhost:9092", "output_max", ss))

    env.execute
  }

  /**
   * Schema to write the Temperature data as text
   */
  val ss: SerializationSchema[Temp, Array[Byte]] = new SerializationSchema[Temp, Array[Byte]]() {
    override def serialize(temp: Temp): Array[Byte] = {
      val tempString = temp.city.toString + "," + temp.temp
      tempString.getBytes()
    }
  }
}