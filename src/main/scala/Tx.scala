import java.util.concurrent.atomic.AtomicReference
import java.time.{Instant, LocalDateTime}
import java.time.LocalDateTime
import java.time.temporal.ChronoField
import scala.collection.mutable.ArrayBuffer

class Tx(body: Tx => Unit, storage: Storage) {

  var state: List[Node[_]] = List()

  def queryAll[T <: TCloneable[T]](collection: Storage => ArrayBuffer[Node[T]], predicate: T => Boolean): Seq[NodeTracker[T]] = {
    collection(storage).filter(z => predicate(z.getValue)).map(z => new NodeTracker(z)).toSeq
  }

  def queryOne[T <: TCloneable[T]](collection: Storage => ArrayBuffer[Node[T]], predicate: T => Boolean, n: Int): Option[NodeTracker[T]] = {
    val buffer = collection(storage).toArray
    //storage.synchronized {
    buffer.find(z => predicate(z.getValue)).map(z => {
      println(s"Tx($n) INNER_MAP TS(${System.nanoTime()}): " + z.getValue.toString)
      val newZ = z
      println(s"Tx($n) newZ(${newZ.getValue}) = z.get(${z.getValue}) TS(${System.nanoTime()})")
      new NodeTracker(newZ)
    })

    //}
  }

  def store(n: Node[_]): Unit = {
    state = n :: state
  }

  def insert[T <: TCloneable[T]](newVal: T): Unit = {
    state = new Node(newVal, null, null, null, -1, 0) :: state
  }

  def run(): Unit = body(this)

  def commit[T <: TCloneable[T]](n: Int): Unit = {
    var resetTx = false
    storage.synchronized {
      println(s"Tx($n) Start_Commit Ts(${System.nanoTime()})")
      for (el <- state) {
        if (el.index != -1 && el.branchFrom.next != null) {
          state = List()
          resetTx = true
          println(s"Tx($n) Reset Ts(${System.nanoTime()})")
        }
      }
      if (!resetTx) {
        state.foreach {
          el =>
            el.getValue match {
              case d: Dog =>
                val alreadyHas = storage.dogs.exists(z => z.getValue.tails == d.tails)
                if (alreadyHas)
                  println("alert!")
              case _ =>
            }

            storage.refreshVersion(el.asInstanceOf[Node[T]])
        }
        println(s"Tx($n) End_Commit Ts(${System.nanoTime()})")
      }
    }
    if (resetTx)
      run()
  }

}

object Tx {
  def apply(query: Tx => Unit)(implicit storage: Storage): Unit = new Tx(query, storage).run()
}