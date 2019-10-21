
import java.util.concurrent.{Executors, TimeUnit}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import TransactionAux._

import scala.collection.mutable.ArrayBuffer

case class Cat(var legs: Int) extends TCloneable[Cat] {
  override def doClone(): Cat = Cat(legs)
}

case class Dog(var tails: Int) extends TCloneable[Dog] {
  override def doClone(): Dog = Dog(tails)
}

object Main extends App {
  def start(): Unit = {

    val cat = Node(Cat(0), null, null, null,0, 0)
    val cats: ArrayBuffer[Node[Cat]] = ArrayBuffer(cat)
    cat.collection = cats

    val dog = Node(Dog(0), null, null, null,0, 0)
    val dogs: ArrayBuffer[Node[Dog]] = ArrayBuffer(dog)
    dog.collection = dogs
    implicit val s: Storage = Storage(cats, dogs)

    val numJobs = 20000
    val numThreads = 1

    val ec1 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

    val tasksCats = for (i <- 1 to numJobs / 2) yield Future {
      Tx { implicit tx =>

        insert(Cat(666))

        val cat = query(_.cats) {
          _.legs >= 0
        }
        // val legs = cat.get(_.legs)
        //cat.update(z => z.legs = z.legs + 1)

        commit
      }
    }(ec1)

    implicit val ec2: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
    val tasksDogs = for (i <- 1 to numJobs/2) yield Future {
      Tx { implicit tx =>

        val dog = query(_.dogs) {
          _.tails >= 0
        }

        dog.foreach(_.update(z => z.tails = z.tails + 1))

        //commit
      }
    }(ec2)

    val tasks = tasksCats ++ tasksDogs
    val aggregated = Future.sequence(tasks)//(ec1)
    Await.result(aggregated, Duration(15, TimeUnit.SECONDS))


    Tx { implicit tx =>

      val q = query(_.cats) {
        _.legs >= 0
      }

      println(q.head.get(_.legs))

      val dogsTails = query(_.dogs) {
        _.tails >= 0
      }
      println(dogsTails.head.get(_.tails))
      //println("finish")
    }
  }

  start()
}


