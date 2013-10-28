import com.seantheprogrammer.bacon.{ReactorModule, Observing, EventSource}
import org.scalatest._

class ReactorSpecs extends FunSpec with Matchers
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

//   describe ("loopUntil") {
//     it ("loops through the body until the given reactive emits") {
//       var loopsRun = 0
//       var completed = false
//       val halter = EventSource[Unit]
//       val events = EventSource[Unit]

//       Reactor.loop { self =>
//         self.loopUntil(halter) {
//           self await events
//           loopsRun += 1
//         }
//         completed = true
//       }

//       events << ()

//       loopsRun should be (1)
//       completed should be (false)

//       events << ()
//       System.gc()
//       events << ()
//       halter << ()
//       events << ()

//       loopsRun should be (3)
//       completed should be (true)
//     }
//   }
}
