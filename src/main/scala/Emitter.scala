package com.seantheprogrammer.bacon

import scala.ref.WeakReference

trait Emitter[+A] {
  def onValue(f: A => Unit)(implicit obs: Observing): Unit =
    obs.observe(this)(f)

  //Public only for testing purposes
  def countSubscribers = filteredSubscribers.size

  protected[this] def emit(a: A) {
    for (WeakReference(subscriber) <- subscribers)
      subscriber.react(a)
  }

  private[this] var subscribers: List[WeakReference[Reactive[A]]] = Nil

  private[bacon] def subscribe(r: Reactive[A]) {
    synchronized {
      subscribers = WeakReference(r) :: subscribers
    }
  }

  private[bacon] def unsubscribe(r: Reactive[A]) {
    synchronized {
      subscribers = filteredSubscribers.filter(_.get.get != r)
    }
  }

  private[this] def filteredSubscribers = subscribers.filter(_.get.nonEmpty)
}
