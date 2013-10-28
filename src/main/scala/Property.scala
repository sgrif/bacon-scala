package com.seantheprogrammer.bacon

trait Property[A] extends Emitter[A] {
  protected[this] def currentValue: A

  def apply(): A = currentValue

  def invalidate() {
    emit(currentValue)
  }

  def toEventStream: EventStream[A] = new PropertyEventStream

  private class PropertyEventStream extends EventStream[A] with Reactive[A] {
    Property.this.subscribe(this)

    def react(a: A) = emit(a)
  }
}
