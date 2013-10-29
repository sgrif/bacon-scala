package com.seantheprogrammer.bacon

private[bacon] class PropertyEventStream[A](p: Property[A]) extends EventStream[A] with Invalidator {
  p.addChild(this)

  override protected def invalidate() {
    super.invalidate()
    emit(p())
  }

  override def subscribe(r: Reactor[A]) = {
    super.subscribe(r)
    r.react(p())
  }
}
