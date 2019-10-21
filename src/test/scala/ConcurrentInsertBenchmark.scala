import java.util.concurrent.{Executors, TimeUnit}

import TransactionAux.{commit, insert, query}
import org.scalameter.Bench.LocalTime
import org.scalameter.Warmer.Default
import org.scalameter.{Key, config}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}


object ConcurrentInsertBenchmark extends LocalTime {
  def runInsert(): Unit = {
    implicit val s: Storage = TestAux.initStorage
    val numJobs = 20000
    val numThreads = 4

    implicit val ec1 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(numThreads))

    val tasksCats = for (i <- 1 to numJobs) yield Future {
      Tx { implicit tx =>

        insert(Cat(i))

        query(_.cats) { c=>
          !c.processed && c.legs % i == 0
        }.headOption match {
          case Some(x) => x.update(z => {
            z.processed = true
            insert(Dog(z.legs))
          })
          case None =>
        }
        commit
      }
    }(ec1)

    val tasks = tasksCats
    val aggregated = Future.sequence(tasks)
    Await.result(aggregated, Duration(20, TimeUnit.SECONDS))

    Tx { implicit tx =>
      query(_.dogs) {
        _ => true
      } map(r => r.get(z => z.tails)) sortBy (r => r) foreach println

    }
    println()
  }

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 5,
    Key.exec.maxWarmupRuns -> 10,
    Key.exec.benchRuns -> 10,
    Key.verbose -> true
  ) withWarmer new Default

  val seqtime = standardConfig measure {
    runInsert()
  }
  println(s"Tx: $seqtime ms")
}
