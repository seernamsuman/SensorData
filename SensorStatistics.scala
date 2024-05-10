import scala.io.Source
import scala.util.Try

object SensorStatistics {

  case class Measurement(sensorId: String, humidity: Option[Int])

  case class SensorStats(
      sensorId: String,
      min: Option[Int] = None,
      max: Option[Int] = None,
      sum: Int = 0,
      count: Int = 0,
      failedCount: Int = 0
  ) {
    def avg: Option[Double] = if (count > 0) Some(sum.toDouble / count) else None
    def merge(measurement: Measurement): SensorStats = measurement.humidity match {
      case Some(value) =>
        copy(
          min = Some(min.map(math.min(_, value)).getOrElse(value)),
          max = Some(max.map(math.max(_, value)).getOrElse(value)),
          sum = sum + value,
          count = count + 1
        )
      case None =>
        copy(failedCount = failedCount + 1)
    }
  }

  def parseLine(line: String): Option[Measurement] = {
    val parts = line.split(",").map(_.trim)
    if (parts.length == 2) {
      val humidity = parts(1) match {
        case "NaN" => None
        case num   => Try(num.toInt).toOption
      }
      Some(Measurement(parts(0), humidity))
    } else {
      None
    }
  }

  def processFile(content: String): List[Measurement] = {
    Source.fromString(content)
      .getLines()
      .drop(1) // Skip the header
      .flatMap(parseLine)
      .toList
  }

  def mergeStats(statsMap: Map[String, SensorStats], measurement: Measurement): Map[String, SensorStats] = {
    statsMap + (measurement.sensorId -> statsMap
      .getOrElse(measurement.sensorId, SensorStats(measurement.sensorId))
      .merge(measurement))
  }

  def main(args: Array[String]): Unit = {
    // Simulate CSV content directly as strings
    val csv1 = """sensor-id,humidity
s1,10
s2,88
s1,NaN"""
    val csv2 = """sensor-id,humidity
s2,80
s3,NaN
s2,78
s1,98"""

    // List of CSV files as strings
    val filesContent = List(csv1, csv2)

    // Process all CSV contents and combine results
    val measurements = filesContent.flatMap(processFile)
    val statsMap = measurements.foldLeft(Map[String, SensorStats]())(mergeStats)
    val totalMeasurements = measurements.length
    val totalFailures = measurements.count(_.humidity.isEmpty)

    println(s"Num of processed files: ${filesContent.length}")
    println(s"Num of processed measurements: $totalMeasurements")
    println(s"Num of failed measurements: $totalFailures")
    println("\nSensors with highest avg humidity:")

    val sortedStats = statsMap.values.toList.sortBy(s => (-s.avg.getOrElse(Double.NaN), s.sensorId))

    println("sensor-id,min,avg,max")
    sortedStats.foreach { s =>
      val minStr = s.min.map(_.toString).getOrElse("NaN")
      val avgStr = s.avg.map(_.formatted("%.2f")).getOrElse("NaN")
      val maxStr = s.max.map(_.toString).getOrElse("NaN")
      println(s"${s.sensorId},$minStr,$avgStr,$maxStr")
    }
  }
}
