import scala.collection.mutable.ArrayBuffer

object TestAux {
  def initStorage: Storage = {
    val cats: ArrayBuffer[Node[Cat]] = ArrayBuffer()
    val dogs: ArrayBuffer[Node[Dog]] = ArrayBuffer()
    Storage(cats, dogs)
  }
}

