package com.seantheprogrammer.bacon

import scala.util.DynamicVariable

class Dynamic[A] private[bacon] (f: => A) extends Property[A] {
  private[this] var parents: List[Property[_]] = Nil
  private[this] var value: A = currentValue

  protected[this] def currentValue = Dynamic.enclosing.withValue(Some(this -> Nil)) {
    parents.foreach(_.removeChild(this))
    val v = f
    parents = Dynamic.enclosing.value.get._2
    v
  }
}

object Dynamic {
  val enclosing: DynamicVariable[Option[(Dynamic[_], List[Property[_]])]] = new DynamicVariable(None)
}
