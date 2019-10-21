import scala.collection.mutable.ArrayBuffer

class Tx(body: Tx => Unit, storage: Storage) {

  var state: List[Node[_]] = List()

  def query[T <: TCloneable[T]](collection: Storage => ArrayBuffer[Node[T]], predicate: T => Boolean): Seq[NodeTracker[T]] = {
    collection(storage).filter(z => predicate(z.value)).map(NodeTracker(_))
  }

  def store(n: Node[_]): Unit = {
    state = n :: state
  }

  def insert[T <: TCloneable[T]](newVal: T): Unit = {
    storage.insert(newVal)
  }

  def run(): Unit = body(this)

  def commit[T <: TCloneable[T]](): Unit = {
    var resetTx = false
    storage.synchronized {
      for (el <- state) {
        if (el.branchFrom.next != null) {
          state = List()
          resetTx = true
        }
      }
      if (!resetTx)
        state.foreach {
          el => storage.refreshVersion(el.asInstanceOf[Node[T]])
        }
    }
    if (resetTx)
      run()
  }
}

object Tx {
  def apply(query: Tx => Unit)(implicit storage: Storage): Unit = new Tx(query, storage).run()
}