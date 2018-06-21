package com.example.workflow.repository

import com.example.workflow.domain.WorkflowExecution

object WorkflowExecutionRepository extends Repository[WorkflowExecution] with InMemoryRepository[WorkflowExecution] {

  def apply(): Repository[WorkflowExecution] = InMemoryStorage

  object InMemoryStorage extends InMemoryRepository[WorkflowExecution]

}