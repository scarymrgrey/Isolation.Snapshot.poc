import scala.collection.mutable.ArrayBuffer
import util.control.Breaks._

class Tx(body: Tx => Unit, storage: Storage) {

  var state: List[Node[_]] = List()

  def queryAll[T <: TCloneable[T]](collection: Storage => ArrayBuffer[Node[T]], predicate: T => Boolean): Seq[NodeTracker[T]] = {
    collection(storage)
      .withFilter(z => z.test(predicate))
      .map(z => NodeTracker(z))
  }

  def queryOne[T <: TCloneable[T]](collection: Storage => ArrayBuffer[Node[T]], predicate: T => Boolean): Option[NodeTracker[T]] = {
    collection(storage)
      .withFilter(z => z.test(predicate))
      .map(z => NodeTracker(z))
      .headOption
  }

  def store(n: Node[_]): Unit = {
    state = n :: state
  }

  def insert[T <: TCloneable[T]](newVal: T): Unit = {
    state = Node(newVal, null, null, null, 0, 0) :: state
  }

  def run(): Unit = body(this)

  def commit[T <: TCloneable[T]](): Unit = {
    var resetTx = false
    for (el <- state) {
      breakable {
        if (el.branchFrom != null) {
          val locked = el.collection(el.index).tryLock()
          if (!locked || el.branchFrom.next != null) {
            state = List()
            resetTx = true
            break
          }
        }
      }
    }
    if (!resetTx) {
      state.foreach {
        el => storage.merge(el.asInstanceOf[Node[T]])
      }
    }

    if (resetTx)
      run()
  }

}

object Tx {
  def apply(query: Tx => Unit)(implicit storage: Storage): Unit = new Tx(query, storage).run()
}