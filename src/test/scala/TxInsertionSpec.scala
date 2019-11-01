import java.util.concurrent.{Executors, TimeUnit}
import TransactionAux.{commit, insert, queryAll, queryOne}
import org.scalatest._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration.Duration

class TxInsertionSpec extends FlatSpec with Matchers {

  "Transaction" should "update and insert entities concurrently" in {
    implicit val s: Storage = TestAux.initStorage
    val numJobs = 50000
    val numThreads = 2

    implicit val ec1: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(numThreads))

    val tasksCats = for (i <- 1 to numJobs) yield Future {
      Tx { implicit tx =>

        insert(Cat(i))

        queryOne(_.cats) { c =>
          !c.processed
        }.foreach(cat => {
          cat.update(z => {
            z.processed = true
          })
          val legs = cat.get(z => z.legs)
          insert(Dog(legs))
        })

        commit
      }
    }

    val tasks = tasksCats
    val aggregated = Future.sequence(tasks)
    Await.result(aggregated, Duration(100, TimeUnit.SECONDS))

    Tx { implicit tx: Tx =>

      val cats = queryAll(_.cats) { c =>
        !c.processed
      }
      cats.foreach(c => {
        c.update(z => z.processed = true)
        insert(Dog(c.get(f => f.legs)))
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
