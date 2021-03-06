package com.seantheprogrammer.bacon

import scala.collection.mutable

trait Observing { self =>
  implicit val obs = this

  private val refs: mutable.Set[Observer[_]] = new mutable.HashSet[Observer[_]]

  trait Observer[A] extends Reactor[A] with Disposable {
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

  private class SingleObserver[A](e: Emitter[A], f: A => Unit)
  extends SimpleObserver[A](e, f) {
    override def react(a: A) {
      super.react(a)
      dispose()
    }
  }

  def observe[A](observable: Observable[A])(f: A => Unit): Observer[A] = {
    val emitter = observable.toEmitter
    addObserver(emitter, new SimpleObserver[A](emitter, f))
  }

  def observeOnce[A](observable: Observable[A])(f: A => Unit): Observer[A] = {
    val emitter = observable.toEmitter
    addObserver(emitter, new SingleObserver[A](emitter, f))
  }

  private def addObserver[A](e: Emitter[A], obs: Observer[A]): Observer[A] = {
    self.synchronized {
      refs += obs
    }

    e.subscribe(obs)
    obs
  }
}
