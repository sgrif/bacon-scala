package com.seantheprogrammer.bacon

trait Emitter[+A] {
  def onValue(f: A => Unit)(implicit obs: Observing): Unit =
    obs.observe(this)(f)

  //Public only for testing purposes
  def countSubscribers = subscribers.size

  protected[this] def emit(a: A) {
    subscribers.foreach(_.react(a))
  }

  private[this] val subscribers: WeakList[Reactive[A]] = new WeakList

  private[bacon] def subscribe(r: Reactive[A]) = subscribers.add(r)

  private[bacon] def unsubscribe(r: Reactive[A]) = subscribers.remove(r)
}
