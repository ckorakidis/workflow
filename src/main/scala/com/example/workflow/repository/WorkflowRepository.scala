package com.example.workflow.repository

import com.example.workflow.domain.Workflow

object WorkflowRepository extends Repository[Workflow] with InMemoryRepository[Workflow] {

  def apply(): Repository[Workflow] = InMemoryStorage

  object InMemoryStorage extends InMemoryRepository[Workflow]

}