package com.seantheprogrammer.bacon

trait Reactive[-A] {
  def react(a: A): Unit
}
