package com.seantheprogrammer.bacon

trait Events[+A] extends Emitter[A] {
  def map[B](f: A => B): Events[B]
  def collect[B](pf: PartialFunction[A, B]): Events[B]
  def collectFirst[B](pf: PartialFunction[A, B]): Events[B]
  def drop(count: Int): Events[A]
  def dropWhile(f: A => Boolean): Events[A]
  def either[B](that: Events[B]): Events[Either[A, B]]
  def filter(f: A => Boolean): Events[A]
  def flatMap[B](f: A => Events[B]): Events[B]
  def foldLeft[B](init: B)(f: (B, A) => B): Property[B]
  def take(count: Int): Events[A]
  def takeWhile(f: A => Boolean): Events[A]
  def union[B >: A](that: Events[B]): Events[B]
  def zip[B](that: Events[B]): Events[(A, B)]

  def ++[B >: A](that: Events[B]): Events[B] = this.union(that)
}
