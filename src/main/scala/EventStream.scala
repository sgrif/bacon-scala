package com.seantheprogrammer.bacon

import scala.ref.WeakReference
import scala.collection.mutable

trait EventStream[A] extends Emitter[A] {
  def map[B](f: A => B) = new Mapped(this, f)

  def collect[B](pf: PartialFunction[A, B]) = new Collected(this, pf)

  def collectFirst[B](pf: PartialFunction[A, B]) =
    new FirstCollected(this, pf)

  def drop(count: Int) = new Dropped(this, count)

  def dropWhile(f: A => Boolean) = new DroppedWhile(this, f)

  def either[B](that: Emitter[B]): Emitter[Either[A, B]] =
    this.map(Left.apply) ++ that.map(Right.apply)

  def filter(f: A => Boolean) = new Filtered(this, f)

  def filterNot(f: A => Boolean) = new Filtered(this, (a: A) => !f(a))

  def flatMap[B](f: A => Emitter[B]) = new Flattened(this, f)

  def foldLeft[B](init: B)(f: (B, A) => B) = new Folded(this, init, f)

  def take(count: Int) = new Taken(this, count)

  def takeWhile(f: A => Boolean) = new TakenWhile(this, f)

  def union[B >: A](that: Emitter[B]) = new Combined(this, that)

  def zip[B](that: Emitter[B]) = new Zipped(this, that)

  abstract class ChildOf1[A](parent: Emitter[A]) extends Reactor[A] {
    parent.subscribe(this)
  }

  class Combined[A](first: Emitter[A], second: Emitter[A])
  extends EventStream[A] with Reactor[A] {
    first.subscribe(this)
    second.subscribe(this)
    def react(a: A) = emit(a)
  }

  class Mapped[A, B](parent: Emitter[A], f: A => B)
  extends ChildOf1[A](parent) with EventStream[B] {
    def react(a: A) = emit(f(a))
  }

  class Collected[A, B](parent: Emitter[A], pf: PartialFunction[A, B])
  extends ChildOf1[A](parent) with EventStream[B] {
    def react(a: A) = pf.runWith(emit)(a)
  }

  class FirstCollected[A, B](
    parent: Emitter[A],
    pf: PartialFunction[A, B]
  ) extends ChildOf1[A](parent) with EventStream[B] {
    def react(a: A) {
      if(pf.runWith(emit)(a))
        parent.unsubscribe(this)
    }
  }

  class Dropped[A](parent: Emitter[A], private var count: Int)
  extends ChildOf1[A](parent) with EventStream[A] {
    def react(a: A) = count match {
      case 0 => emit(a)
      case _ => count -= 1
    }
  }

  class DroppedWhile[A](parent: Emitter[A], f: A => Boolean)
  extends ChildOf1[A](parent) with EventStream[A] {
    private var dropping = true

    def react(a: A) {
      dropping &&= f(a)
      if (!dropping)
        emit(a)
    }
  }

  class Filtered[A](parent: Emitter[A], f: A => Boolean)
  extends ChildOf1[A](parent) with EventStream[A] {
    def react(a: A) = if (f(a)) emit(a)
  }

  class Flattened[A, B](parent: Emitter[A], f: A => Emitter[B])
  extends ChildOf1[A](parent) with EventStream[B] {
    def react(a: A) {
      f(a).subscribe(childReactor)
    }

    private val childReactor = new Reactor[B] {
      def react(b: B) = emit(b)
    }
  }

  class Folded[A, B](parent: Emitter[A], init: B, f: (B, A) => B)
  extends ChildOf1[A](parent) with EventStream[B] {
    private var current: B = init
    emit(current)

    def react(a: A) {
      current = f(current, a)
      emit(current)
    }
  }

  class Taken[A](parent: Emitter[A], private var count: Int)
  extends ChildOf1[A](parent) with EventStream[A] {
    def react(a: A) = count match {
      case 0 => parent.unsubscribe(this)
      case _ => {
        count -= 1
        emit(a)
      }
    }
  }

  class TakenWhile[A](parent: Emitter[A], f: A => Boolean)
  extends ChildOf1[A](parent) with EventStream[A] {
    def react(a: A) {
      if (f(a))
        emit(a)
      else
        parent.unsubscribe(this)
    }
  }

  class Zipped[A, B](aEmitter: Emitter[A], bEmitter: Emitter[B])
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

    aEmitter.subscribe(aReactor)
    bEmitter.subscribe(bReactor)
  }
}
