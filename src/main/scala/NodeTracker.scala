class NodeTracker[T <: TCloneable[T]](var node: Node[T]) {

  def update(upd: T => Unit)(implicit tx: Tx): Unit = {
    node = node.update(upd)
    tx.store(node)
  }

  def get[Z](prop: T => Z): Z = node.get(prop)
}

