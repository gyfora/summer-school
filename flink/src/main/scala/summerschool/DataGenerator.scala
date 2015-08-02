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

import java.util.Properties
import kafka.producer.ProducerConfig
import kafka.javaapi.producer.Producer
import kafka.producer.KeyedMessage
import kafka.message.Message
import scala.util.Random

object DataGenerator {

  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put("metadata.broker.list", "127.0.0.1:9092")
    props.put("serializer.class", "kafka.serializer.StringEncoder")
    props.put("request.required.acks", "1")

    val config = new ProducerConfig(props)

    val producer = new Producer[String, String](config);

    var count = 0

    while (true) {
      Thread.sleep(10)
      producer.send(new KeyedMessage[String, String]("input", null, getRandomTemp));
      count += 1
      if (count % 100 == 0) {
        println("Generated " + count + " outputs...");
      }
    }
    producer.close
  }

  def getRandomTemp: String = {
    val city = cities(Random.nextInt(cities.size))
    val temp = Random.nextGaussian * 15 + 20
    city + "," + temp
  }

  val cities = List("Tirane", "Andorra-la-vella", "Jerevan", "Vienna", "Baku", "Minsk", "Brussels", "Sarajevo", "Sofia", "Zagreb", "Nicosia", "Prague", "Copenhagen", "Tallinn", "Helsinki", "Paris", "Cayenne", "Tbilisi", "Berlin", "Athens", "Budapest", "Reykjavik", "Rome", "Riga", "Vaduz", "Vilnius", "Luxemburg", "Skopje", "Valletta", "Fort-de-France", "Kishinev", "Monaco", "Amsterdam", "Oslo", "Belfast", "Warsaw", "Lisbon", "Bucharest", "Moscow", "San Marino", "Edinburgh", "Bratislava", "Ljubljana", "Madrid", "Stockholm", "Berne", "Dushanbe", "Kiev", "London", "Toshkent", "Vatican City", "Belgrade")

}