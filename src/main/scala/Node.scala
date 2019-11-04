import java.util.concurrent.locks.{Lock, ReadWriteLock, ReentrantLock, ReentrantReadWriteLock}

import scala.collection.mutable.ArrayBuffer

case class Node[T <: TCloneable[T]](value: T,
                                    branchFrom: Node[T],
                                    var next: Node[T],
                                    var collection: ArrayBuffer[Node[T]],
                                    var index: Int,
                                    v: Int) {

  private var locked = false

  def update(upd: T => Unit): Node[T] = {
    val newVal = value.doClone()
    upd(newVal)
    Node(newVal, this, null, collection, index, v + 1)
  }

  def get[Z](prop: T => Z): Z = prop(value)

  def test(predicate: T => Boolean): Boolean = predicate(value)

  def tryLock(): Boolean = {
    this.synchronized {
      if (!locked) {
        locked = true
        locked
      }
      else
        false
    }
  }
}


