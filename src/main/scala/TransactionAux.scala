object TransactionAux {
  def query[T <: TCloneable[T]](collection: Storage => List[Node[T]])(predicate: T => Boolean)(implicit t: Tx): NodeTracker[T] = t.query(collection,predicate)

  def store(n: Node[_ <: TCloneable[_]])(implicit t: Tx): Unit = t.store(n)

  def commit(implicit t: Tx): Unit = t.commit()
}
