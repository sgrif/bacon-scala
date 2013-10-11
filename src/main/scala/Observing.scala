package com.seantheprogrammer.bacon

import scala.collection.mutable

trait Observing { self =>
  private val refs: mutable.Set[Observer[_]] = new mutable.HashSet[Observer[_]]

  trait Observer[A] extends Reactive[A] with Disposable {
    protected val emitter: Emitter[A]

    override def dispose() {
      super.dispose()
      self.synchronized {
        refs -= this
      }
      emitter.unsubscribe(this)
    }
  }

  private class SimpleObserver[A](protected val emitter: Emitter[A], f: A => Unit)
  extends Observer[A] {
    def react(a: A) = f(a)
  }

  private class SelfObserver[A](protected val emitter: Emitter[A],
    f: (Observer[A], A) => Unit) extends Observer[A] {
    override def react(a: A) = f(this, a)
  }

  def observe[A](emitter: Emitter[A])(f: A => Unit): Observer[A] = {
    addObserver(emitter, new SimpleObserver[A](emitter, f))
  }

  def observeWithObserver[A](emitter: Emitter[A])(f: (Observer[A], A) => Unit) {
    addObserver(emitter, new SelfObserver[A](emitter, f))
  }

  private def addObserver[A](e: Emitter[A], obs: Observer[A]): Observer[A] = {
    self.synchronized {
      refs += obs
    }

    e.subscribe(obs)
    obs
  }
}
