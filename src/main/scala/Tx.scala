import java.util.concurrent.atomic.AtomicReference

import scala.collection.mutable.ArrayBuffer

class Tx(body: Tx => Unit, storage: Storage) {

  var state: List[Node[_]] = List()

  def queryAll[T <: TCloneable[T]](collection: Storage => ArrayBuffer[Node[T]], predicate: T => Boolean): Seq[NodeTracker[T]] = {
    collection(storage).filter(z => predicate(z.value)).map(NodeTracker(_)).toSeq
  }

  def queryOne[T <: TCloneable[T]](collection: Storage => ArrayBuffer[Node[T]], predicate: T => Boolean): Option[NodeTracker[T]] = {
    val buffer = collection(storage)
    //storage.synchronized {
    buffer.find(z => predicate(z.value)).map(NodeTracker(_))
    //}
  }

  def store(n: Node[_]): Unit = {
    state = n :: state
  }

  def insert[T <: TCloneable[T]](newVal: T): Unit = {
    state = Node(newVal, null, null, null, -1, 0) :: state
  }

  def run(): Unit = body(this)

  def commit[T <: TCloneable[T]](n: Int): Unit = {
    var resetTx = false
    storage.synchronized {
      for (el <- state) {
        if (el.index != -1 && el.branchFrom.next != null) {
          state = List()
          resetTx = true
          println("reset TX: " + n)
        }
      }
      if (!resetTx)
        state.foreach {

          el =>
            el.value match {
              case d: Dog =>
                val alreadyHas = storage.dogs.exists(z => z.value.tails == d.tails)
                if (alreadyHas)
                  println("alert!")
              case _ =>
            }

            storage.refreshVersion(el.asInstanceOf[Node[T]])
        }
    }
    if (resetTx)
      run()
  }
}

object Tx {
  def apply(query: Tx => Unit)(implicit storage: Storage): Unit = new Tx(query, storage).run()
}