import com.seantheprogrammer.bacon.{Property, Var, Observing}
import org.scalatest._

class DynamicPropertySpecs extends FunSpec with Matchers with Observing {
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
    observe(d) { v => emitted = v :: emitted }

    x() = 2
    y() = 2
    x() = 5

    emitted should be (List(7, 4, 3))
  }

  it ("only evaluates its body when invalidated")(pending)
  it ("does not subscribe to properties in branches that are not evaluated")(pending)
  it ("evaluates its body lazily")(pending)
}
