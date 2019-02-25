import org.apache.spark.rdd.RDDBarrier
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class BeginTogetherEnoughResourcesTest extends FlatSpec{
  it should "run all tasks in parallel when there are enough resources" in {
    val conf = new SparkConf().setAppName("Barrier Execution Mode with sufficent resources").setMaster("local[3]")
    val sparkContext:SparkContext = SparkContext.getOrCreate(conf)
    val numbersRdd = sparkContext.parallelize(1 to 100).repartition(3)

    val mappedNumbers: RDDBarrier[Int] = numbersRdd.map(number => {
      number
    }).barrier()

    val collectedNumbers = mappedNumbers.mapPartitions(numbers => {
      numbers
    }).collect()

    collectedNumbers should have size 100
    collectedNumbers should contain allElementsOf (1 to 100)
  }
}
