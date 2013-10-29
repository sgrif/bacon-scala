package com.seantheprogrammer.bacon

trait Observable[+A] {
  def toEmitter: Emitter[A]

  def onValue(f: A => Unit)(implicit obs: Observing): Unit =
    obs.observe(this)(f)
}
