package com.example.workflow.repository

import com.example.workflow.domain.Identifiable
import com.twitter.util.{ Future, Try }

trait InMemoryRepository[T <: Identifiable] extends Repository[T] {

  private var db = scala.collection.mutable.ListBuffer.empty[T]

  override def add(item: T): Future[T] = {
    val existing = db.find(_.id == item.id)

    if (existing.nonEmpty)
      Future.value(existing.get)
    else {
      db += item

      Future.value(item)
    }
  }

  override def get(id: Long): Future[Option[T]] =
    Future.value(db.find(_.id == id))

  override def update(T: T): Future[Option[T]] =
    Future {
      for {
        n <- db.zipWithIndex.find { case (x, s) => x.id == T.id }.map(_._2)
        _ <- Try { db.update(n, T) }.toOption
        nT <- db.find(_.id == T.id)
      } yield nT
    }

  override def delete(item: T): Future[Option[T]] =
    Future {
      for {
        n <- db.zipWithIndex.find { case (x, s) => x.id == item.id }.map(_._2)
        x <- Try { db.remove(n) }.toOption
      } yield x
    }

  override def getAll: Future[List[T]] = Future.value(db.toList)

  override def deleteAll: Future[List[T]] = {
    val contents = db.toList
    db = scala.collection.mutable.ListBuffer.empty[T]
    Future.value(contents)
  }
}