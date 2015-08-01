package summerschool

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka._
import org.apache.flink.streaming.connectors.kafka.api.KafkaSink
import org.apache.flink.streaming.util.serialization.SerializationSchema
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.streaming.connectors.kafka.api.KafkaSource
import scala.util.Random
import org.apache.flink.streaming.api.scala.windowing.Time
import java.util.concurrent.TimeUnit.SECONDS
import org.apache.flink.streaming.connectors.kafka.api.persistent.PersistentKafkaSource

object FlinkKafkaExample {

  case class Temp(city: String, temp: Double)

  def main(args: Array[String]): Unit = {
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

    // We separate correctly parsed and errored inputs  
    val errors: DataStream[String] = parsed.filter(_.isLeft).map(_.left.get)
    val temps: DataStream[Temp] = parsed.filter(_.isRight).map(_.right.get)

    // We compute the current average of each city's temperature and output every 10th update/city
    val avgTemps: DataStream[Temp] = temps.keyBy("city").flatMapWithState((in, state: Option[(Double, Long)]) =>
      {
        val s = state.getOrElse((0.0, 0L))
        val u = (s._1 + in.temp, s._2 + 1)
        (if (s._2 % 10 == 0) List(Temp(in.city, u._1 / u._2)) else (List()), Some(u))
      })

    // For also compute the current global in every 5 second interval
    val globalMax = temps.window(Time.of(5, SECONDS)).max("temp").flatten

    // We write the results to the respective Kafka topics
    avgTemps.addSink(new KafkaSink("localhost:9092", "output_avg", ss))
    globalMax.addSink(new KafkaSink("localhost:9092", "output_max", ss))

    // Print the errors to the console
    errors.map("Errored: " + _).print

    env.execute
  }

  val ss: SerializationSchema[Temp, Array[Byte]] = new SerializationSchema[Temp, Array[Byte]]() {
    override def serialize(temp: Temp): Array[Byte] = {
      val tempString = temp.city.toString + "," + temp.temp
      tempString.getBytes()
    }
  }
}