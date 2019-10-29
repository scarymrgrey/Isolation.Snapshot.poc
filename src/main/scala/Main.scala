
import java.util.concurrent.{Executors, TimeUnit}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import TransactionAux._

import scala.collection.mutable.ArrayBuffer

class Cat(var legs: Int) extends TCloneable[Cat] {
  @volatile var processed = false
  val uuid = java.util.UUID.randomUUID.toString.take(4)

  override def doClone(): Cat = new Cat(legs)

  override def toString: String = s"cat($legs,$processed)@$uuid"
}

class Dog(var tails: Int) extends TCloneable[Dog] {
  override def doClone(): Dog = new Dog(tails)
}

object Main extends App {
  def start(): Unit = {

    val cat = new Node(new Cat(0), null, null, null, 0, 0)
    val cats = ArrayBuffer(cat)
    cat.collection = cats

    val dog = new Node(new Dog(0), null, null, null, 0, 0)
    val dogs: ArrayBuffer[Node[Dog]] = ArrayBuffer(dog)
    dog.collection = dogs
    implicit val s: Storage = Storage(cats, dogs)

    val numJobs = 20000
    val numThreads = 1

    val ec1 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

    val tasksCats = for (i <- 1 to numJobs / 2) yield Future {
      Tx { implicit tx =>

        insert(new Cat(666))

        val cat = queryOne(_.cats) {
          _.legs >= 0
        }

        commit
      }
    }(ec1)

    implicit val ec2: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
    val tasksDogs = for (i <- 1 to numJobs / 2) yield Future {
      Tx { implicit tx =>

        val dog = queryOne(_.dogs) {
          _.tails >= 0
        }

        dog.foreach(_.update(z => z.tails = z.tails + 1))

        commit
      }
    }(ec2)

    val tasks = tasksCats ++ tasksDogs
    val aggregated = Future.sequence(tasks) //(ec1)
    Await.result(aggregated, Duration(15, TimeUnit.SECONDS))

    Tx { implicit tx =>

      val q = queryOne(_.cats) {
        _.legs >= 0
      }

      println(q.head.get(_.legs))

      val dogsTails = queryOne(_.dogs) {
        _.tails >= 0
      }
      println(dogsTails.head.get(_.tails))
    }
  }

  start()
}


