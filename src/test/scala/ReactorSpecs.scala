import com.seantheprogrammer.bacon.{ReactorModule, Observing, EventSource}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec

class ReactorSpecs extends FunSpec with ShouldMatchers
with ReactorModule with Observing {
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
      System.gc()
      events << ()
      reactor.dispose()
      events << ()

      loopsRun should be (3)
    }
  }
}
