import com.seantheprogrammer.bacon.{Val, Observing}
import org.scalatest._

class ValSpecs extends FunSpec with Matchers with Observing with PropertyBehaviors {
  it should behave like aProperty(Val.apply)
}
