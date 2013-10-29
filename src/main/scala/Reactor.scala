package com.seantheprogrammer.bacon

trait Reactor[-A] {
  def react(a: A): Unit
}
