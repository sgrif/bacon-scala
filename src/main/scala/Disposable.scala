package com.seantheprogrammer.bacon

trait Disposable {
  def isDisposed: Boolean = _isDisposed

  private[this] var _isDisposed = false

  def dispose() {
    _isDisposed = true
  }
}
