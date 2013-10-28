package com.seantheprogrammer.bacon

class Var[A] private (initial: A) extends Property[A] {
  protected[this] var currentValue = initial

  def update(a: A) = {
    currentValue = a
    invalidate()
  }
}

object Var {
  def apply[A](a: A) = new Var(a)
}
