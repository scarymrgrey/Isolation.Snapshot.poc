
import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import TransactionAux._

case class Cat(var legs: Int) extends TCloneable[Cat] {
  override def doClone(): Cat = Cat(legs)
}

case class Dog(var tails: Int) extends TCloneable[Dog] {
  override def doClone(): Dog = Dog(tails)
}

object Main extends App {
  def start(): Unit = {

    val cat = Node(Cat(0), null, null, null, 0)
    val cats: List[Node[Cat]] = List(cat)
    cat.collection = cats

    val dog = Node(Dog(0), null, null, null, 0)
    val dogs: List[Node[Dog]] = List(dog)
    dog.collection = dogs
    implicit val s: Storage = Storage(cats, dogs)

    val numJobs = 500000
    val numThreads = 8

    implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(numThreads))

    val tasks = for (i <- 1 to numJobs) yield Future {
      //println("Start Tx: " + i)
      Tx { implicit tx =>

        val cat = query(_.cats) {
          _.legs >= 0
        }
        val legs = cat.get(_.legs)
        cat.update(z => z.legs = legs + 1)

        commit
      }

      //println("End Tx: " + i)
    }

    val aggregated = Future.sequence(tasks)
    val oneToNSum = Await.result(aggregated, Duration(15, TimeUnit.SECONDS))

    Tx { implicit tx =>

      val q = query(_.cats) {
        _.legs >= 0
      }

      println(q.get(_.legs))
      //println("finish")
    }
  }

  start()
}


