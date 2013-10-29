package com.seantheprogrammer.bacon

trait Invalidator {
  private[this] val children = new WeakList[Invalidator]

  protected def invalidate() {
    children.foreach(_.invalidate())
  }

  protected[bacon] def addChild(p: Invalidator) = children.add(p)

  protected[bacon] def removeChild(p: Invalidator) = children.remove(p)
}
