import org.apache.spark.{BarrierTaskContext, SparkConf, SparkContext, SparkException}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._


class ExceedTimeOut extends FlatSpec {
  "Exceed TimeOut Error" should "TEst exceeding timeout Failure" in {
    val conf = new SparkConf()
      .set("spark.barrier.sync.timeout", "1")
      .setAppName("Barrier Execution Mode with insufficient resources").setMaster("local[4]")
    val sc = new SparkContext(conf)
    val rdd = sc.makeRDD(1 to 10, 4)
    val rdd2 = rdd.barrier().mapPartitions { it =>
      val context = BarrierTaskContext.get()
      // Task 3 shall sleep 2000ms to ensure barrier() call timeout
      if (context.taskAttemptId == 3) {
        Thread.sleep(2000)
      }
      context.barrier()
      it
    }

    val error = intercept[SparkException] {
      rdd2.collect()
    }.getMessage
    assert(error.contains("The coordinator didn't get all barrier sync requests"))
    assert(error.contains("within 1 second(s)"))
  }


}

