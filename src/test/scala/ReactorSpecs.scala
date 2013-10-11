import com.seantheprogrammer.bacon.{Reactor, EventSource}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec

class ReactorSpecs extends FunSpec with ShouldMatchers {
  describe ("looping") {
    it ("loops through its body until disposed") {
      var loopsRun = 0
      val events = EventSource[Unit]
      val reactor = Reactor.loop { self =>
        self await events
        loopsRun += 1
      }

      events << ()

      loopsRun should be (1)

      events << ()
      events << ()
      reactor.dispose()
      events << ()

      loopsRun should be (3)
    }
  }
}
