package com.example.workflow.repository

import com.example.workflow.domain.Identifiable
import com.twitter.util.Future

trait Repository[T <: Identifiable] {

  def add(item: T): Future[T]

  def get(id: Long): Future[Option[T]]

  def update(T: T): Future[Option[T]]

  def delete(item: T): Future[Option[T]]

  def getAll: Future[List[T]]

  def deleteAll: Future[List[T]]
}