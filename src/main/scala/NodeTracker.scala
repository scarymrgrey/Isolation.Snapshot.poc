class NodeTracker[T <: TCloneable[T]](var node: Node[T]) {
  def update(upd: T => Unit)(n: Int)(implicit tx: Tx): Unit = {
    val newNode = node.update(upd)
    println(s"Tx($n) NodeTracker INNER_UPDATE: " + node.getValue.toString + " INTO " + newNode.getValue.toString)
    node = newNode
    tx.store(newNode)
  }

  def get[Z](prop: T => Z): Z = node.get(prop)
}

