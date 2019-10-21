import org.scalameter.Bench.LocalTime
import org.scalameter.Warmer.Default
import org.scalameter.{Key, config}

object IsolationSnapshotBenchmark extends LocalTime {
  val standardConfig = config(
    Key.exec.minWarmupRuns -> 5,
    Key.exec.maxWarmupRuns -> 10,
    Key.exec.benchRuns -> 10,
    Key.verbose -> true
  ) withWarmer new Default

  val seqtime = standardConfig measure {
    Main.start()
  }
  println(s"Tx: $seqtime ms")

}
