import java.util.concurrent.{Executors, TimeUnit}

import TransactionAux.{commit, insert, queryAll, queryOne}
import org.scalatest._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

class ExampleSpec extends FlatSpec with Matchers {

  "Transaction" should "update and insert entities concurrently" in {
    implicit val s: Storage = TestAux.initStorage
    val numJobs = 10000
    val numThreads = 8

    implicit val ec1 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(numThreads))

    val tasksCats = for (i <- 1 to numJobs) yield Future {
      Tx { implicit tx =>
        insert(new Cat(i))

        val option = queryOne(_.cats){ c =>
          !c.processed
        }
        option match {
          case Some(x) =>
            x.update(z => {
              z.processed = true
            })
            val legs = x.get(z => z.legs)
            insert(new Dog(legs))
          case None =>
        }
        commit
      }
    }

    val tasks = tasksCats
    val aggregated = Future.sequence(tasks)
    Await.result(aggregated, Duration(20, TimeUnit.SECONDS))
    Tx { implicit tx: Tx =>

      val cats = queryAll(_.cats) { c =>
        !c.processed
      }
      cats.foreach(c => {
        c.update(z => z.processed = true)
        insert(new Dog(c.get(f => f.legs)))
      })

      commit
    }
    Tx { implicit tx =>
      val tails = queryAll(_.dogs) {
        _ => true
      }.map(r => r.get(z => z.tails))
        .sortBy(r => r)

      val legs = queryAll(_.cats) {
        _ => true
      }.map(r => r.get(z => z.legs))
        .sortBy(r => r)

      tails should not be empty
      legs should not be empty

      legs shouldEqual tails

    }
  }
}
