package com.seantheprogrammer.bacon

trait Property[A] extends Observable[A] with Invalidator {
  protected[this] def currentValue: A

  def apply(): A = {
    Dynamic.enclosing.value = Dynamic.enclosing.value match {
      case Some((d, parents)) => {
        addChild(d)
        Some((d, this :: parents))
      }
      case None => None
    }
    currentValue
  }

  def toEventStream: EventStream[A] = new PropertyEventStream(this)

  def toEmitter = toEventStream
}

object Property {
  def apply[A](f: => A): Property[A] = new Dynamic(f)
}
