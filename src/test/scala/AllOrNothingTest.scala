import org.apache.spark.{BarrierTaskContext, SparkConf, SparkContext, SparkException}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class AllOrNothingTest extends FlatSpec{
  it should "restart all stages together for ShuffleMapStage" in {

    val conf = new SparkConf().setAppName("Barrier Execution Mode All Or Nothing").setMaster("local[3]")
    val sparkContext:SparkContext = SparkContext.getOrCreate(conf)
    val numbersRdd = sparkContext.parallelize(1 to 10, 3)
    val mappedNumbers = numbersRdd.filter(number => {
      number % 2 == 0
    }).groupBy(number => {
      // Please notice: the retry applies only for ShuffleMapStage
      // For ResultStage it fails because:
      // Abort the failed result stage since we may have committed output for some partitions.
      if (number == 5) {
        //FailureFlags.wasFailed = true
        throw new SparkException("Expected failure")
      }
      number
    }).barrier()

    val collectedNumbers = mappedNumbers.mapPartitions(numbers => {
      val context = BarrierTaskContext.get()
      println(s"context=${context.taskAttemptId()} / ${context.stageAttemptNumber()}")
      context.barrier()
      numbers
    }).collect()

    collectedNumbers.foreach(println)

    collectedNumbers should have size 5
  }
}
