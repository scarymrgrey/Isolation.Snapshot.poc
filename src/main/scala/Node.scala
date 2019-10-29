import scala.collection.mutable.ArrayBuffer

class Node[T <: TCloneable[T]](value: T,
                               var branchFrom: Node[T],
                               var next: Node[T],
                               var collection: ArrayBuffer[Node[T]],
                               var index: Int,
                               v: Int) {

  def update(upd: T => Unit): Node[T] = {
    val newVal = value.doClone()
    upd(newVal)
    new Node(newVal, this, null, collection, index, v + 1)
  }

  def get[Z](prop: T => Z): Z = prop(value)

  def test(predicate: T => Boolean): Boolean = predicate(value)

}


