package com.seantheprogrammer.bacon

import scala.collection.mutable

trait Observing { self =>
  private val refs: mutable.Set[Observer[_]] = new mutable.HashSet[Observer[_]]

  private class Observer[A](emitter: Emitter[A], f: A => Unit)
  extends Reactive[A] {
    def react(a: A) = f(a)

    def dispose() {
      self.synchronized {
        refs -= this
      }
      emitter.unsubscribe(this)
    }
  }

  def observe[A](emitter: Emitter[A])(f: A => Unit) {
    val observer = new Observer[A](emitter, f)

    self.synchronized {
      refs += observer
    }

    emitter.subscribe(observer)
  }
}
