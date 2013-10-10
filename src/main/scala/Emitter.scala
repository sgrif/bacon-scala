package com.seantheprogrammer.bacon

import scala.ref.WeakReference

trait Emitter[+A] {
  def map[B](f: A => B): Emitter[B]
  def collect[B](pf: PartialFunction[A, B]): Emitter[B]
  def collectFirst[B](pf: PartialFunction[A, B]): Emitter[B]
  def drop(count: Int): Emitter[A]
  def dropWhile(f: A => Boolean): Emitter[A]
  def either[B](that: Emitter[B]): Emitter[Either[A, B]]
  def filter(f: A => Boolean): Emitter[A]
  def flatMap[B](f: A => Emitter[B]): Emitter[B]
  def foldLeft[B](init: B)(f: (B, A) => B): Emitter[B]
  def take(count: Int): Emitter[A]
  def takeWhile(f: A => Boolean): Emitter[A]
  def union[B >: A](that: Emitter[B]): Emitter[B]
  def zip[B](that: Emitter[B]): Emitter[(A, B)]

  def ++[B >: A](that: Emitter[B]): Emitter[B] = this.union(that)

  def onValue(f: A => Unit)(implicit obs: Observing): Unit =
    obs.observe(this)(f)

  def lastValue = _lastValue

  protected[this] var _lastValue: Option[A] = None

  protected[this] def emit(a: A) {
    _lastValue = Some(a)
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
