package com.seantheprogrammer.bacon

import scala.ref.WeakReference
import scala.collection.mutable

trait EventStream[A] extends Events[A] { outer =>
  def map[B](f: A => B) = new Mapped(f)

  def collect[B](pf: PartialFunction[A, B]) = new Collected(pf)

  def collectFirst[B](pf: PartialFunction[A, B]) = new CollectedFirst(pf)

  def drop(count: Int) = new Dropped(count)

  def dropWhile(f: A => Boolean) = new DroppedWhile(f)

  def either[B](that: Events[B]): Events[Either[A, B]] =
    this.map(Left.apply) ++ that.map(Right.apply)

  def filter(f: A => Boolean) = new Filtered(f)

  def filterNot(f: A => Boolean) = new Filtered((a: A) => !f(a))

  def flatMap[B](f: A => Events[B]) = new Flattened(f)

  def foldLeft[B](init: B)(f: (B, A) => B) = new Folded(init, f)

  def take(count: Int) = new Taken(count)

  def takeWhile(f: A => Boolean) = new TakenWhile(f)

  def union[B >: A](that: Events[B]) = new Combined(this, that)

  def zip[B](that: Events[B]) = new Zipped(that)

  trait ChildReactor extends Reactor[A] {
    outer.subscribe(this)

    def disconnect() {
      outer.unsubscribe(this)
    }
  }

  trait ChildStream[B] extends ChildReactor with EventStream[B]

  class Combined[A](private val first: Events[A], private val second: Events[A])
  extends EventStream[A] with Reactor[A] {
    first.subscribe(this)
    second.subscribe(this)
    def react(a: A) = emit(a)
  }

  class Mapped[B](f: A => B) extends ChildStream[B] {
    def react(a: A) = emit(f(a))
  }

  class Collected[B](pf: PartialFunction[A, B]) extends ChildStream[B] {
    def react(a: A) = pf.runWith(emit)(a)
  }

  class CollectedFirst[B](pf: PartialFunction[A, B]) extends ChildStream[B] {
    def react(a: A) = if (pf.runWith(emit)(a)) disconnect()
  }

  class Dropped(private var count: Int) extends ChildStream[A] {
    def react(a: A) = count match {
      case 0 => emit(a)
      case _ => count -= 1
    }
  }

  class DroppedWhile(f: A => Boolean) extends ChildStream[A] {
    private var dropping = true

    def react(a: A) {
      dropping &&= f(a)
      if (!dropping)
        emit(a)
    }
  }

  class Filtered(f: A => Boolean) extends ChildStream[A] {
    def react(a: A) = if (f(a)) emit(a)
  }

  class Flattened[B](f: A => Events[B]) extends ChildStream[B] {
    private var currentChild: Option[Events[B]] = None

    def react(a: A) {
      for (child <- currentChild) child.unsubscribe(childReactor)
      currentChild = Some(f(a))
      currentChild.get.subscribe(childReactor)
    }

    override def disconnect() {
      for (child <- currentChild)
        child.unsubscribe(childReactor)
      super.disconnect()
    }

    private val childReactor = new Reactor[B] {
      def react(b: B) = emit(b)
    }
  }

  class Folded[B](init: B, f: (B, A) => B) extends Property[B] with ChildReactor {
    protected[this] var currentValue: B = init

    def react(a: A) {
      currentValue = f(currentValue, a)
      invalidate()
    }
  }

  class Taken(private var count: Int) extends ChildStream[A] {
    def react(a: A) = count match {
      case 0 => disconnect()
      case _ => {
        count -= 1
        emit(a)
      }
    }
  }

  class TakenWhile(f: A => Boolean) extends ChildStream[A] {
    def react(a: A) {
      if (f(a))
        emit(a)
      else
        disconnect()
    }
  }

  class Zipped[B](bEvents: Events[B]) extends EventStream[(A, B)] {
    private val as: mutable.Queue[A] = mutable.Queue.empty
    private val bs: mutable.Queue[B] = mutable.Queue.empty

    private val aReactor = new Reactor[A] {
      def react(a: A) {
        if (bs.nonEmpty)
          emit((a, bs.dequeue))
        else
          as += a
      }
    }

    private val bReactor = new Reactor[B] {
      def react(b: B) {
        if (as.nonEmpty)
          emit((as.dequeue, b))
        else
          bs += b
      }
    }

    outer.subscribe(aReactor)
    bEvents.subscribe(bReactor)
  }
}
