package com.seantheprogrammer.bacon

import scala.util.continuations._

trait ReactorDSL {
  this: Disposable =>

  def await[A](e: Emitter[A]): A @suspendable = shift { (k: A => Unit) =>
    val r = new Reactive[A] {
      def react(a: A) = {
        if (!isDisposed) k(a)
        e.unsubscribe(this)
      }
    }
    e.subscribe(r)
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
