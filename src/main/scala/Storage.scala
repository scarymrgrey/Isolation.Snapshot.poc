import scala.collection.mutable.ArrayBuffer

case class Storage(cats: ArrayBuffer[Node[Cat]], dogs: ArrayBuffer[Node[Dog]]) {

  def doSnapshot(): Storage = Storage(cats, dogs)

  def refreshVersion[T <: TCloneable[T]](el: Node[T]): Unit = {
    el.branchFrom.next = el
    el.branchFrom = null
    el.collection(el.index) = el
  }

  def insert[T <: TCloneable[T]](newVal: T): Unit = {
    this.synchronized {
      newVal match {
        case cat: Cat =>
          cats += Node(cat, null, null, cats, cats.length, 0)
        case dog: Dog =>
          dogs += Node(dog, null, null, dogs, dogs.length, 0)
      }
    }
  }
}

