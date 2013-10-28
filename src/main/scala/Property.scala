package com.seantheprogrammer.bacon

trait Property[A] extends Emitter[A] {
  protected[this] def currentValue: A

  private[this] val children = new WeakList[Property[_]]

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

  def invalidate() {
    children.foreach(_.invalidate())
    emit(currentValue)
  }

  def toEventStream: EventStream[A] = new PropertyEventStream

  protected[bacon] def addChild(p: Property[_]) = children.add(p)

  protected[bacon] def removeChild(p: Property[_]) = children.remove(p)

  private class PropertyEventStream extends EventStream[A] with Reactive[A] {
    Property.this.subscribe(this)

    def react(a: A) = emit(a)
  }
}

object Property {
  def apply[A](f: => A): Property[A] = new Dynamic(f)
}
