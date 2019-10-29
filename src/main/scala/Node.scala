import java.util.concurrent.atomic.AtomicReference

import scala.collection.mutable.ArrayBuffer

class Node[T <: TCloneable[T]](value: => T,
                               @volatile var branchFrom: Node[T],
                               @volatile var next: Node[T],
                               var collection: ArrayBuffer[Node[T]],
                               var index: Int,
                               v: Int) {
  def getValue: T = value

  def update(upd: T => Unit): Node[T] = {
    val newVal = value.doClone()
    upd(newVal)
    new Node(newVal, this, null, collection, index, v + 1)
  }

  def get[Z](prop: T => Z): Z = prop(value)
}

