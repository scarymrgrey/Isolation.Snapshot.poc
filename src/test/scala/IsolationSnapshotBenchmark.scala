import java.util.concurrent.{Executors, TimeUnit}

import TransactionAux.{commit, insert, query}
import org.scalameter.Bench.LocalTime
import org.scalameter.Warmer.Default
import org.scalameter.{Key, config}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object TestAux {
  def getStorage: Storage = {
    val cat = Node(Cat(0), null, null, null, 0, 0)
    val cats = ArrayBuffer(cat)
    cat.collection = cats

    val dog = Node(Dog(0), null, null, null, 0, 0)
    val dogs = ArrayBuffer(dog)
    dog.collection = dogs
    Storage(cats, dogs)
  }
}

object IsolationSnapshotBenchmark extends LocalTime {

  def runInsert(): Unit = {
    implicit val s: Storage = TestAux.getStorage
    val numJobs = 20000
    val numThreads = 4

    implicit val ec1 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(numThreads))

    val tasksCats = for (i <- 1 to numJobs / 2) yield Future {
      Tx { implicit tx =>

        insert(Cat(666))

        val cat = query(_.cats) {
          _.legs >= 0
        }
        commit
      }
    }(ec1)
    val tasks = tasksCats
    val aggregated = Future.sequence(tasks)
    Await.result(aggregated, Duration(15, TimeUnit.SECONDS))
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
