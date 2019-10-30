import scala.collection.mutable.ArrayBuffer

case class Storage(cats: ArrayBuffer[Node[Cat]], dogs: ArrayBuffer[Node[Dog]]) {

  def doSnapshot(): Storage = Storage(cats, dogs)

  def refreshVersion[T <: TCloneable[T]](el: Node[T]): Unit = {
    if (el.index != -1) {
      el.branchFrom.next = el
      el.branchFrom = null
      el.collection(el.index) = el
    } else {
      insert(el)
    }
  }

  private def insert[T <: TCloneable[T]](newVal: Node[T]): Unit = {
    val coll = (newVal.get(z=>z) match {
      case c: Cat =>
        cats
      case d: Dog =>
        dogs
    }).asInstanceOf[ArrayBuffer[Node[T]]]
    newVal.collection  = coll
    newVal.index = coll.length
    coll += newVal
  }

}



