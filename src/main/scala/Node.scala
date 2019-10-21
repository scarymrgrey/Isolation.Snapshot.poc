case class Node[T <: TCloneable[T]](value: T,
                                    var branchFrom: Node[T],
                                    var next: Node[T],
                                    var collection: List[Node[T]],
                                    v: Int) {

  def update(upd: T => Unit): Node[T] = {
    val newVal = value.doClone()
    upd(newVal)
    Node(newVal, this, null, collection, v + 1)
  }

  def get[Z](prop: T => Z): Z = prop(value)
}
