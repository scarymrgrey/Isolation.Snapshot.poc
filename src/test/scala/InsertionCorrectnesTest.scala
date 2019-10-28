import java.util.concurrent.locks.{ReadWriteLock, ReentrantReadWriteLock}
import java.util.concurrent.{Executors, TimeUnit}

import TransactionAux.{commit, insert, queryAll, queryOne}
import org.scalatest._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

class ExampleSpec extends FlatSpec with Matchers {

  "A Stack" should "pop values in last-in-first-out order" in {
    implicit val s: Storage = TestAux.initStorage
    val numJobs = 100
    val numThreads = 8

    implicit val ec1 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(numThreads))

    val tasksCats = for (i <- 1 to numJobs) yield Future {
      Tx { implicit tx =>
        insert(Cat(i))

        val option = queryOne(_.cats) { c =>
          !c.processed.get()
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

      val cats = queryAll(_.cats) { c =>
        !c.processed.get()
      }

      cats.foreach(c => {
        //insert(Dog(c.get(_.legs)))
      })

      commit(0)
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
