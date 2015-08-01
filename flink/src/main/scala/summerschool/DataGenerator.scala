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