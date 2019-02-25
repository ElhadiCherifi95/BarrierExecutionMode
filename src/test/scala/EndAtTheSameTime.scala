import java.util.Date
import java.util.concurrent.ThreadLocalRandom

import org.apache.spark.{BarrierTaskContext, SparkConf, SparkContext}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

import scala.collection.mutable.ListBuffer


class EndAtTheSameTime extends FlatSpec {
  "RDDBarrier with BarrierTaskContext" should "synchronize tasks" in {
    val conf = new SparkConf().setAppName("RDDBarrier with BarrierTaskContext").setMaster("local[3]")
    val sparkContext: SparkContext = SparkContext.getOrCreate(conf)

    val numbersRdd = sparkContext.parallelize(1 to 10).repartition(3)

    val mappedNumbers = numbersRdd.map(number => {
      number
    }).barrier()

    mappedNumbers.mapPartitions(numbers => {
      val sleepingTime = ThreadLocalRandom.current().nextLong(1000L, 10000L)
      println(s"Sleeping ${sleepingTime} ms")
      Thread.sleep(sleepingTime)
      val context = BarrierTaskContext.get()
      context.barrier()
      Hitting.addTimeHit(new Date().toString)
      numbers
    }).collect()

    Hitting.TimeHitting.distinct.toList.foreach(println)
  }

}

object Hitting {

  var TimeHitting = new ListBuffer[String]()


  def addTimeHit(dateString: String): Unit = {
    TimeHitting += dateString
  }


}

