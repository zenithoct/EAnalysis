/**
  * Created by siva on 23/10/16.
  */

import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.{Encoder, Encoders}

/**
  * custom weightage
  * @param f
  * @tparam I
  */
  class CountWeightage[I](f: I => Double)  extends Aggregator[I,  Double, Double]
    with Serializable {
    val zero = 0.0
    def reduce(acc: Double, x: I) = acc + f(x)
    def merge(acc1: Double, acc2: Double) = acc1 + acc2
    def finish(acc: Double) = acc

    def bufferEncoder: Encoder[Double] = Encoders.scalaDouble
    def outputEncoder: Encoder[Double] = Encoders.scalaDouble
  }

