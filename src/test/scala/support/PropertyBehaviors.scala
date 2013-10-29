import com.seantheprogrammer.bacon.{Property, Observing}
import org.scalatest._

trait PropertyBehaviors { this: FunSpec with Matchers with Observing =>
  def aProperty(factory: Int => Property[Int]) {
    it ("holds an initial value") {
      val x = factory(2)
      x() should be (2)
    }

    it ("immediately emits its current value to new subscribers") {
      val x = factory(1)
      var emitted: List[Int] = Nil
      x.onValue { v => emitted = v :: emitted }
      emitted should be (1 :: Nil)
    }
  }
}
