package com.seantheprogrammer.bacon

class Var[A] private (initial: A) extends Emitter[A] {
  private[this] var currentValue = initial

  def apply(): A = currentValue

  def update(a: A) = {
    currentValue = a
    emit(a)
  }

  def toEventStream: EventStream[A] = new VarEventStream

  private class VarEventStream extends EventStream[A] with Reactive[A] {
    Var.this.subscribe(this)

    def react(a: A) = emit(a)
  }
}

object Var {
  def apply[A](a: A) = new Var(a)
}
