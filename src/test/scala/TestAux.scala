import scala.collection.mutable.ArrayBuffer

object TestAux {
  def initStorage: Storage = {
    val cat = Node(Cat(0), null, null, null, 0, 0)
    val cats: ArrayBuffer[Node[Cat]] = ArrayBuffer(cat)
    cat.collection = cats

    val dog = Node(Dog(0), null, null, null, 0, 0)
    val dogs: ArrayBuffer[Node[Dog]] = ArrayBuffer(dog)
    dog.collection = dogs
    Storage(cats, dogs)
  }
}
