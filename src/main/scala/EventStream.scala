package com.seantheprogrammer.bacon

import scala.ref.WeakReference
import scala.collection.mutable

trait EventStream[A] extends Events[A] {
  def map[B](f: A => B) = new Mapped(this, f)

  def collect[B](pf: PartialFunction[A, B]) = new Collected(this, pf)

  def collectFirst[B](pf: PartialFunction[A, B]) =
    new FirstCollected(this, pf)

  def drop(count: Int) = new Dropped(this, count)

  def dropWhile(f: A => Boolean) = new DroppedWhile(this, f)

  def either[B](that: Events[B]): Events[Either[A, B]] =
    this.map(Left.apply) ++ that.map(Right.apply)

  def filter(f: A => Boolean) = new Filtered(this, f)

  def filterNot(f: A => Boolean) = new Filtered(this, (a: A) => !f(a))

  def flatMap[B](f: A => Events[B]) = new Flattened(this, f)

  def foldLeft[B](init: B)(f: (B, A) => B) = new Folded(this, init, f)

  def take(count: Int) = new Taken(this, count)

  def takeWhile(f: A => Boolean) = new TakenWhile(this, f)

  def union[B >: A](that: Events[B]) = new Combined(this, that)

  def zip[B](that: Events[B]) = new Zipped(this, that)

  abstract class ChildOf1[A](parent: Events[A]) extends Reactor[A] {
    parent.subscribe(this)
  }

  class Combined[A](private val first: Events[A], private val second: Events[A])
  extends EventStream[A] with Reactor[A] {
    first.subscribe(this)
    second.subscribe(this)
    def react(a: A) = emit(a)
  }

  class Mapped[A, B](parent: Events[A], f: A => B)
  extends ChildOf1[A](parent) with EventStream[B] {
    def react(a: A) = emit(f(a))
  }

  class Collected[A, B](parent: Events[A], pf: PartialFunction[A, B])
  extends ChildOf1[A](parent) with EventStream[B] {
    def react(a: A) = pf.runWith(emit)(a)
  }

  class FirstCollected[A, B](
    parent: Events[A],
    pf: PartialFunction[A, B]
  ) extends ChildOf1[A](parent) with EventStream[B] {
    def react(a: A) {
      if(pf.runWith(emit)(a))
        parent.unsubscribe(this)
    }
  }

  class Dropped[A](parent: Events[A], private var count: Int)
  extends ChildOf1[A](parent) with EventStream[A] {
    def react(a: A) = count match {
      case 0 => emit(a)
      case _ => count -= 1
    }
  }

  class DroppedWhile[A](parent: Events[A], f: A => Boolean)
  extends ChildOf1[A](parent) with EventStream[A] {
    private var dropping = true

    def react(a: A) {
      dropping &&= f(a)
      if (!dropping)
        emit(a)
    }
  }

  class Filtered[A](parent: Events[A], f: A => Boolean)
  extends ChildOf1[A](parent) with EventStream[A] {
    def react(a: A) = if (f(a)) emit(a)
  }

  class Flattened[A, B](parent: Events[A], f: A => Events[B])
  extends ChildOf1[A](parent) with EventStream[B] {
    def react(a: A) {
      f(a).subscribe(childReactor)
    }

    private val childReactor = new Reactor[B] {
      def react(b: B) = emit(b)
    }
  }

  class Folded[A, B](parent: Events[A], init: B, f: (B, A) => B)
  extends ChildOf1[A](parent) with EventStream[B] {
    private var current: B = init
    emit(current)

    def react(a: A) {
      current = f(current, a)
      emit(current)
    }
  }

  class Taken[A](parent: Events[A], private var count: Int)
  extends ChildOf1[A](parent) with EventStream[A] {
    def react(a: A) = count match {
      case 0 => parent.unsubscribe(this)
      case _ => {
        count -= 1
        emit(a)
      }
    }
  }

  class TakenWhile[A](parent: Events[A], f: A => Boolean)
  extends ChildOf1[A](parent) with EventStream[A] {
    def react(a: A) {
      if (f(a))
        emit(a)
      else
        parent.unsubscribe(this)
    }
  }

  class Zipped[A, B](aEvents: Events[A], bEvents: Events[B])
  extends EventStream[(A, B)] {
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

    aEvents.subscribe(aReactor)
    bEvents.subscribe(bReactor)
  }
}
