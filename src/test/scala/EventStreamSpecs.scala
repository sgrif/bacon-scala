import com.seantheprogrammer.bacon.{Observing, EventSource, Emitter}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec

class EventStreamSpecs extends FunSpec with ShouldMatchers with Observing {
  describe("Emitting events") {
    it("should notify subscribers") {
      var sentEvents: List[Int] = Nil
      val ints = EventSource[Int]

      observe(ints) { i => sentEvents = i :: sentEvents }
      ints << 1
      ints << 2
      ints << 3

      sentEvents should be (3 :: 2 :: 1 :: Nil)
    }

    def testUnion(
        ints: EventSource[Int],
        strings: EventSource[String],
        both: Emitter[Any]
      ) {
      var sentEvents: List[Any] = Nil

      observe(both) { e => sentEvents = e :: sentEvents }
      ints << 1
      strings << "2"
      ints << 3

      sentEvents should be (3 :: "2" :: 1 :: Nil)
    }

    it ("can be combined") {
      val ints = EventSource[Int]
      val strings = EventSource[String]
      val both = ints ++ strings

      testUnion(ints, strings, both)
    }

    it ("aliases ++ as union") {
      val ints = EventSource[Int]
      val strings = EventSource[String]
      val both = ints.union(strings)

      testUnion(ints, strings, both)
    }

    it ("can be mapped") {
      var sentEvents: List[String] = Nil
      val ints = EventSource[Int]
      val mapped = ints.map(_.toString)

      observe(mapped) { s => sentEvents = s :: sentEvents }
      ints << 1
      ints << 2
      ints << 3

      sentEvents should be ("3" :: "2" :: "1" :: Nil)
    }

    it ("can be collected") {
      var sentEvents: List[Int] = Nil
      val anys = EventSource[Any]
      val collected = anys.collect({ case i: Int => i + 1})

      observe(collected) { s => sentEvents = s :: sentEvents }
      anys << 1
      anys << "2"
      anys << 3

      sentEvents should be (4 :: 2 :: Nil)
    }

    it ("can collect the first matching element") {
      var sentEvents: List[Int] = Nil
      val ints = EventSource[Int]
      val evenAdded = ints.collectFirst {
        case i if i % 2 == 0 => i + 1
      }

      observe(evenAdded) { s => sentEvents = s :: sentEvents }
      ints << 1
      ints << 2
      ints << 4

      sentEvents should be (3 :: Nil)
    }

    it ("can drop the first x events") {
      var sentEvents: List[Int] = Nil
      val ints = EventSource[Int]
      val dropped = ints.drop(2)

      observe(dropped) { s => sentEvents = s :: sentEvents }
      ints << 1
      ints << 2
      ints << 3

      sentEvents should be (3 :: Nil)
    }

    it ("can ignore events until a predicate is matched") {
      var sentEvents: List[Int] = Nil
      val ints = EventSource[Int]
      val dropped = ints.dropWhile(_ < 5)

      observe(dropped) { s => sentEvents = s :: sentEvents }
      ints << 1
      ints << 5
      ints << 6

      sentEvents should be (6 :: 5 :: Nil)
    }

    it ("can wrap two streams in an either") {
      var sentEvents: List[Either[Int, String]] = Nil
      val ints = EventSource[Int]
      val strings = EventSource[String]
      val both = ints either strings

      observe(both) { e => sentEvents = e :: sentEvents }
      ints << 1
      strings << "2"
      System.gc()
      ints << 3
      strings << "4"

      sentEvents should be (Right("4") :: Left(3) :: Right("2") :: Left(1) :: Nil)
    }

    it ("can be filtered") {
      var sentEvents: List[Int] = Nil
      val ints = EventSource[Int]
      val filtered = ints.filter(_ > 2)

      observe(filtered) { s => sentEvents = s :: sentEvents }
      ints << 1
      ints << 2
      ints << 3

      sentEvents should be (3 :: Nil)
    }

    it ("can be reverse filtered") {
      var sentEvents: List[Int] = Nil
      val ints = EventSource[Int]
      val filtered = ints.filterNot(_ > 2)

      observe(filtered) { s => sentEvents = s :: sentEvents }
      ints << 1
      ints << 2
      ints << 3

      sentEvents should be (2 :: 1 :: Nil)
    }

    it ("can be flat mapped") {
      var sentEvents: List[Int] = Nil
      val intsmap = Map("1" -> EventSource[Int], "2" -> EventSource[Int])
      val strings = EventSource[String]
      val ints = strings.flatMap(intsmap)

      observe(ints) { s => sentEvents = s :: sentEvents }
      strings << "1"
      intsmap("1") << 1
      intsmap("2") << 2
      System.gc() // Ensure intermediate steps aren't getting GC'd
      strings << "2"
      intsmap("1") << 3
      intsmap("2") << 4

      sentEvents should be (4 :: 3 :: 1 :: Nil)
    }

    it ("can be folded") {
      var sentEvents: List[Int] = Nil
      val ints = EventSource[Int]
      val sums = ints.foldLeft(0) { _ + _ }

      observe(sums) { s => sentEvents = s :: sentEvents }
      ints << 1
      ints << 2
      ints << 3

      sentEvents should be (6 :: 3 :: 1 :: Nil)
    }

    it ("can be given a callback with an implicit observer") {
      implicit val observing = new Object with Observing

      var sentEvents: List[Int] = Nil
      val ints = EventSource[Int]

      ints.onValue { i => sentEvents = i :: sentEvents }
      ints << 1
      ints << 2
      ints << 3

      sentEvents should be (3 :: 2 :: 1 :: Nil)
    }

    it ("stores the most recently emitted event") {
      val ints = EventSource[Int]
      val mapped = ints.map(_ + 1)
      val sums = mapped.foldLeft(0) { _ + _ }

      ints.lastValue should be (None)
      mapped.lastValue should be (None)
      sums.lastValue should be (Some(0))

      ints << 1

      ints.lastValue should be (Some(1))
      mapped.lastValue should be (Some(2))
      sums.lastValue should be (Some(2))

      ints << 5

      ints.lastValue should be (Some(5))
      mapped.lastValue should be (Some(6))
      sums.lastValue should be (Some(8))
    }

    it ("can be given a callback for a limited number of events") {
      var sentEvents: List[Int] = Nil
      val ints = EventSource[Int]
      val taken = ints.take(2)

      observe(taken) { i => sentEvents = i :: sentEvents }
      ints << 1
      ints << 2
      ints << 3

      sentEvents should be (2 :: 1 :: Nil)
    }

    it ("can emit events until a predicate is matched") {
      var sentEvents: List[Int] = Nil
      val ints = EventSource[Int]
      val taken = ints.takeWhile(_ < 2)

      observe(taken) { i => sentEvents = i :: sentEvents }
      ints << 1
      ints << 2
      ints << 3

      sentEvents should be (1 :: Nil)
    }

    it ("can zip two emitters together") {
      var sentEvents: List[(Int, String)] = Nil
      val ints = EventSource[Int]
      val strings = EventSource[String]
      val zipped = ints zip strings

      observe(zipped) { e => sentEvents = e :: sentEvents }
      ints << 1
      ints << 2
      strings << "foo"
      ints << 3
      System.gc() // Ensure hidden children are not being GC'ed
      strings << "bar"
      strings << "baz"
      strings << "qux"

      sentEvents should be (List((3, "baz"), (2, "bar"), (1, "foo")))
    }
  }
}
