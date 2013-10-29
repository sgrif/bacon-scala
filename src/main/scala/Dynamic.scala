package com.seantheprogrammer.bacon

import scala.util.DynamicVariable

class Dynamic[A] private[bacon] (f: => A) extends Property[A] {
  private[this] var state: Option[State] = None

  protected[this] def currentValue = currentState.value

  private[this] def currentState: State = state.getOrElse {
    state = Some(determineState)
    currentState
  }

  private[this] def determineState = Dynamic.enclosing.withValue(Some(this -> Nil)) {
    State(f, Dynamic.enclosing.value.get._2)
  }

  override def invalidate() {
    for (s <- state)
      s.parents.foreach(_.removeChild(this))
    state = None
    super.invalidate()
  }

  private case class State(value: A, parents: List[Property[_]])
}

object Dynamic {
  val enclosing: DynamicVariable[Option[(Dynamic[_], List[Property[_]])]] = new DynamicVariable(None)
}
