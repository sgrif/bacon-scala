package com.seantheprogrammer.bacon

object EventSource {
  def apply[A]: EventSource[A] = new EventSource[A]
}

class EventSource[A] extends EventStream[A] {
  def <<(a: A) = this.emit(a)
}
