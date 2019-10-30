import scala.collection.mutable.ArrayBuffer

case class Storage(cats: ArrayBuffer[Node[Cat]], dogs: ArrayBuffer[Node[Dog]]) {

  def merge[T <: TCloneable[T]](el: Node[T]): Unit = {
    if (el.branchFrom != null)
      refreshVersion(el)
    else
      insert(el)
  }

  private def refreshVersion[T <: TCloneable[T]](el: Node[T]): Unit = {
    el.branchFrom.next = el
    el.branchFrom = null
    el.collection(el.index) = el
  }

  private def insert[T <: TCloneable[T]](newVal: Node[T]): Unit = {
    val coll = (newVal.get(z => z) match {
      case _: Cat =>
        cats
      case _: Dog =>
        dogs
    }).asInstanceOf[ArrayBuffer[Node[T]]]
    newVal.collection = coll
    newVal.index = coll.length
    coll += newVal
  }

}



