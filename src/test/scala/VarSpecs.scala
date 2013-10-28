import com.seantheprogrammer.bacon.{Var, Observing}
import org.scalatest._

class VarSpecs extends FunSpec with Matchers with Observing {
  it ("holds an initial value") {
    val x = Var(2)
    x() should be (2)
  }

  it ("can be updated to a new value") {
    val x = Var(2)
    x() = 1
    x() should be (1)
  }

  it ("emits changes in value") {
    val x = Var(3)
    var emitted: Int = 0
    observe(x) { v => emitted = v }
    x() = 1
    emitted should be (1)
    System.gc()
    x() = 2
    emitted should be (2)
  }

  it ("can be converted to an event stream") {
    val x = Var(1)
    val es = x.toEventStream.map(_ + 1)
    var emitted: List[Int] = Nil
    observe(es) { v => emitted = v :: emitted }

    x() = 2
    x() = 3
    x() = 4

    emitted should be (List(5, 4, 3))
  }
}
