package com.seantheprogrammer.bacon

import scala.util.continuations._

trait ReactorModule {
  this: Observing =>

  trait ReactorDSL {
    self: Disposable =>

    def await[A](e: Emitter[A]): A @suspendable = shift { (k: A => Unit) =>
      observeWithObserver(e) { (obs, v) =>
        obs.dispose()
        if (!self.isDisposed)
          k(v)
      }
    }
  }

  object Reactor {
    def loop(op: ReactorDSL => Unit @suspendable) = new Reactor {
      def body = while (!isDisposed) op(this)
    }
  }

  abstract class Reactor extends Disposable with ReactorDSL {
    def body(): Unit @suspendable

    reset {
      body()
    }
  }
}
