package com.seantheprogrammer.bacon

import scala.ref.WeakReference

private[bacon] class WeakList[A <: AnyRef] extends Traversable[A] {
  private[this] var els: List[WeakReference[A]] = Nil

  override def foreach[U](f: A => U): Unit = {
    for (WeakReference(el) <- els)
      f(el)
  }

  def add(a: A) {
    synchronized {
      els = WeakReference(a) :: els
    }
  }

  def remove(a: A) {
    synchronized {
      els = els.filter(e => e.get.nonEmpty && e.get.get != a)
    }
  }
}
