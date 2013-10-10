package com.seantheprogrammer.bacon

import scala.ref.WeakReference

trait Emitter[+A] {
  def onValue(f: A => Unit)(implicit obs: Observing): Unit =
    obs.observe(this)(f)

  protected[this] def emit(a: A) {
    for (WeakReference(subscriber) <- subscribers)
      subscriber.react(a)
  }

  private[this] var subscribers: List[WeakReference[Reactor[A]]] = Nil

  def subs: List[WeakReference[Reactor[_]]] = subscribers

  private[bacon] def subscribe(r: Reactor[A]) {
    synchronized {
      subscribers = WeakReference(r) :: subscribers
    }
  }

  private[bacon] def unsubscribe(r: Reactor[A]) {
    synchronized {
      subscribers = subscribers.filter { subscriberReference =>
        val sub = subscriberReference.get
        sub.nonEmpty && sub.get != r
      }
    }
  }
}
