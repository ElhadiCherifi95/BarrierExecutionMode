import java.util.Date
import java.util.concurrent.ThreadLocalRandom

import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._


class NotEndAtTheSameTime extends FlatSpec {
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
      Hitting.addTimeHit(new Date().toString)
      numbers
    }).collect()

    Hitting.TimeHitting.distinct.toList.foreach(println)
  }


}

