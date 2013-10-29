package com.seantheprogrammer.bacon

case class Val[A](protected[this] val currentValue: A) extends Property[A]
