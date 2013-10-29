import com.seantheprogrammer.bacon.{Property, Var, Observing}
import org.scalatest._

class DynamicPropertySpecs extends FunSpec with Matchers with Observing with PropertyBehaviors {
  it should behave like aProperty(makeProperty)

  it ("gets an initial value from evaluating the body") {
    val x = Var(1)
    val d = Property { x() + 1 }

    d() should be (2)
  }

  it ("is updated when its child properties update") {
    val x = Var(1)
    val y = Var(1)
    val d = Property { x() + y() }

    var emitted: List[Int] = Nil
    d.onValue { v => emitted = v :: emitted }
    System.gc()

    x() = 2
    y() = 2
    x() = 5

    emitted should be (List(7, 4, 3, 2))
  }

  it ("only evaluates its body when invalidated") {
    var bodyEvaluated = false
    val x = Var(1)
    val d = Property { x(); bodyEvaluated = true }

    d()
    bodyEvaluated = false
    d()
    bodyEvaluated should be (false)

    x() = 2
    d()
    bodyEvaluated should be (true)
  }

  it ("does not subscribe to properties in branches that are not evaluated") {
    val b = Var(true)
    val x = Var(1)
    val y = Var(1)
    var bodyEvaluated = false
    val d = Property { if (b()) x() else y(); bodyEvaluated = true }
    d.onValue { _ => }

    bodyEvaluated = false
    y() = 2
    bodyEvaluated should be (false)
    x() = 2
    bodyEvaluated should be (true)

    b() = false
    bodyEvaluated = false
    x() = 1
    bodyEvaluated should be (false)
    y() = 1
    bodyEvaluated should be (true)
  }

  it ("evaluates its body lazily") {
    val x = Var(1)
    var bodyEvaluated = false
    val d = Property { x() + 1; bodyEvaluated = true }

    d()
    bodyEvaluated = false

    x() = 2
    x() = 3
    x() = 4
    bodyEvaluated should be (false)
    d()
    bodyEvaluated should be (true)
  }

  it ("evaluates its body when it receives a subscriber") {
    val x = Var(1)
    var bodyEvaluated = false
    val d = Property { x() + 1; bodyEvaluated = true }

    x() = 2
    x() = 3
    x() = 4

    bodyEvaluated should be (false)
    d.onValue { _ => }
    bodyEvaluated should be (true)
  }

  it ("does not reevaluate the body on subscriber if already evaluated") {
    var bodyEvaluated = false
    val d = Property { bodyEvaluated = true }
    d()
    bodyEvaluated = false

    d.onValue { _ => }
    bodyEvaluated should be (false)
  }

  private def makeProperty[A](a: A): Property[A] = {
    val x = Var(a)
    Property { x() }
  }
}
