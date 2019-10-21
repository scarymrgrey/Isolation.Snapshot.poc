import scala.collection.mutable.ArrayBuffer

object TransactionAux {
  def query[T <: TCloneable[T]](collection: Storage => ArrayBuffer[Node[T]])(predicate: T => Boolean)(implicit tx: Tx): Seq[NodeTracker[T]] = tx.query(collection, predicate)

  def insert[T <: TCloneable[T]](newVal: T)(implicit tx: Tx): Unit = tx.insert(newVal)

  def store(n: Node[_ <: TCloneable[_]])(implicit tx: Tx): Unit = tx.store(n)

  def commit(implicit tx: Tx): Unit = tx.commit()
}
