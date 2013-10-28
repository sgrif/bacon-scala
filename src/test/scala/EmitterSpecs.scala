import com.seantheprogrammer.bacon.{Observing, EventSource}
import org.scalatest._

class EmitterSpecs extends FunSpec with Matchers with Observing {
  it ("does not prevent subscribed objects from being GC'd") {
    val ints = EventSource[Int]
    var mapped = ints.map(_ + 1)
    ints.filter(_ < 3)

    ints.countSubscribers should be (2)
    System.gc()
    ints.countSubscribers should be (1)
    mapped = null
    System.gc()
    ints.countSubscribers should be (0)
  }

  it ("creates a strong reference when directly observed") {
    val ints = EventSource[Int]
    ints.map(_ + 1)
    val observer = observe(ints) { _ => }

    ints.countSubscribers should be (2)
    System.gc()
    ints.countSubscribers should be (1)
    observer.dispose()
    ints.countSubscribers should be (0)
  }
}
