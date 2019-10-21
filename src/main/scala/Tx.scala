class Tx(body: Tx => Unit, storage: Storage) {

  var state: List[Node[_]] = List()

  def query[T <: TCloneable[T]](collection: Storage => List[Node[T]], predicate: T => Boolean): NodeTracker[T] = {
    val head = collection(storage).filter(z => predicate(z.value)).head
    new NodeTracker(head)
  }

  def store(n: Node[_]): Unit = {
    state = n :: state
  }

  def run(): Unit = body(this)

  def commit[T <: TCloneable[T]](): Unit = {
    var resetTx = false
    storage.synchronized {
      for (el <- state) {
        if (el.branchFrom.next != null) {
          state = List()
          resetTx = true
          //println("restart Tx")
        }
      }
      if (!resetTx)
        state.foreach {
          el => storage.refreshVersion(el.asInstanceOf[Node[T]])
        }
    }
    if (resetTx)
      run()
    //println("commit")
  }
}

object Tx {
  def apply(query: Tx => Unit)(implicit storage: Storage): Unit = new Tx(query, storage).run()
}