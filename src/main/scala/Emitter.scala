package com.seantheprogrammer.bacon

trait Emitter[+A] {
  def onValue(f: A => Unit)(implicit obs: Observing): Unit =
    obs.observe(this)(f)

  //Public only for testing purposes
  def countSubscribers = subscribers.size

  def hasSubscribers = subscribers.nonEmpty

  protected[this] def emit(a: A) {
    subscribers.foreach(_.react(a))
  }

  private[this] val subscribers: WeakList[Reactor[A]] = new WeakList

  private[bacon] def subscribe(r: Reactor[A]) = subscribers.add(r)

  private[bacon] def unsubscribe(r: Reactor[A]) = subscribers.remove(r)
}
