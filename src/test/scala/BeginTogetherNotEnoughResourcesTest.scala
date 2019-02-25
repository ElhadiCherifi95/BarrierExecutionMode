import org.apache.spark.rdd.RDDBarrier
import org.apache.spark.{SparkConf, SparkContext, SparkException}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class BeginTogetherNotEnoughResourcesTest extends FlatSpec{
  it should "get stuck because of insufficient resources" in {
    val conf = new SparkConf().setAppName("Barrier Execution Mode with insufficient resources").setMaster("local[1]")
      .set("spark.scheduler.barrier.maxConcurrentTasksCheck.maxFailures", "1")
    val sparkContext:SparkContext = SparkContext.getOrCreate(conf)
    val numbersRdd = sparkContext.parallelize(1 to 100).repartition(3)

    val barrierException = intercept[SparkException] {
      val mappedNumbers: RDDBarrier[Int] = numbersRdd.map(number => {
        number
      }).barrier()

      mappedNumbers.mapPartitions(numbers => {
        println(s"All numbers are ${numbers.mkString(", ")}")
        Iterator("a", "b")
      }).collect()
    }

    barrierException.getMessage shouldEqual "[SPARK-24819]: Barrier execution mode does not allow run a barrier stage that requires more slots than the total number of slots in the cluster currently. Please init a new cluster with more CPU cores or repartition the input RDD(s) to reduce the number of slots required to run this barrier stage."
  }


}
