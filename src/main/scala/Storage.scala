case class Storage(var cats: List[Node[Cat]], dogs: List[Node[Dog]]) {
  def doSnapshot(): Storage = Storage(cats, dogs)

  def refreshVersion[T <: TCloneable[T]](el: Node[T]): Unit = {
    val newC = el :: el.collection.filter(z => z != el.branchFrom)
    el.branchFrom.next = el
    el.branchFrom = null
    cats = newC.asInstanceOf[List[Node[Cat]]]
    //el.collection = newC
  }
}

