import java.util.concurrent.{Executors, TimeUnit}

import TransactionAux.{commit, insert, queryOne, queryAll}
import org.scalameter.Bench.LocalTime
import org.scalameter.Warmer.Default
import org.scalameter.{Key, config}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


object ConcurrentInsertBenchmark extends LocalTime {
  def runInsert(): Unit = {
    implicit val s: Storage = TestAux.initStorage
    val numJobs = 30000
    val numThreads = 4

    implicit val ec1 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(numThreads))

    val tasksCats = for (i <- 1 to numJobs) yield Future {
      Tx { implicit tx =>

        insert(Cat(i))

        val option = queryOne(_.cats) { c =>
          !c.processed.get()  //&& c.legs % i == 0
        }
        option match {
          case Some(x) =>
            x.update(z => {
              z.processed.set(true)
            })
            val legs = x.get(z => z.legs)
            insert(Dog(legs))
          case None =>
        }
        commit(i)
      }
    }

    val tasks = tasksCats
    val aggregated = Future.sequence(tasks)
    Await.result(aggregated, Duration(20, TimeUnit.SECONDS))

    Tx { implicit tx =>
      queryAll(_.dogs) {
        _ => true
      }.map(r => r.get(z => z.tails))
        .sortBy(r => r)
        .foreach(println)
    }
  }

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 3,
    Key.exec.maxWarmupRuns -> 5,
    Key.exec.benchRuns -> 5,
    Key.verbose -> true
  ) withWarmer new Default

  val seqtime = standardConfig measure {
    runInsert()
  }
  println(s"Tx: $seqtime ms")
}
