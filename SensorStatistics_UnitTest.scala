import org.scalatest.funsuite.AnyFunSuite

class SensorStatisticsTest extends AnyFunSuite {
  test("SensorStatistics should correctly calculate min/avg/max humidity") {
    val csvFiles = List(
      """
      sensor-id,humidity
      s1,10
      s2,88
      s1,NaN
      """,
      """
      sensor-id,humidity
      s2,80
      s3,NaN
      s2,78
      s1,98
      """
    )

    def loadMockData(fileContent: String): List[SensorStatistics.Measurement] = {
      val lines = fileContent.stripMargin.trim.split("\n").toList.tail
      lines.flatMap(SensorStatistics.parseLine)
    }

    // Collecting all measurements from mock data
    val measurements = csvFiles.flatMap(loadMockData)

    // Creating stats
    val statsMap = measurements.foldLeft(Map[String, SensorStatistics.SensorStats]())(SensorStatistics.mergeStats)
    
    // Getting sorted stats for assertion
    val sortedStats = statsMap.values.toList.sortBy(s => (-s.avg.getOrElse(Double.NaN), s.sensorId))

    // Assertions on the sorted stats
    assert(sortedStats.head.sensorId == "s2")
    assert(sortedStats.head.min.contains(78))
    assert(sortedStats.head.avg.exists(_ == 82.0))
    assert(sortedStats.head.max.contains(88))

    assert(sortedStats(1).sensorId == "s1")
    assert(sortedStats(1).min.contains(10))
    assert(sortedStats(1).avg.exists(_ == 54.0))
    assert(sortedStats(1).max.contains(98))

    assert(sortedStats.last.sensorId == "s3")
    assert(sortedStats.last.min.isEmpty)
    assert(sortedStats.last.avg.isEmpty)
    assert(sortedStats.last.max.isEmpty)
  }

  test("SensorStatistics should count files and measurements correctly") {
    val csvFiles = List(
      """
      sensor-id,humidity
      s1,10
      s2,88
      s1,NaN
      """,
      """
      sensor-id,humidity
      s2,80
      s3,NaN
      s2,78
      s1,98
      """
    )

    def loadMockData(fileContent: String): List[SensorStatistics.Measurement] = {
      val lines = fileContent.stripMargin.trim.split("\n").toList.tail
      lines.flatMap(SensorStatistics.parseLine)
    }

    // Collecting all measurements from mock data
    val measurements = csvFiles.flatMap(loadMockData)

    // Counting the number of measurements and failed measurements
    val totalMeasurements = measurements.length
    val totalFailures = measurements.count(_.humidity.isEmpty)

    assert(totalMeasurements == 7)
    assert(totalFailures == 2)
  }
}
