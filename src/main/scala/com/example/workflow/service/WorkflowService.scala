package com.example.workflow.service

import com.example.workflow.domain.Workflow
import com.example.workflow.repository._
import com.twitter.util.Future

trait WorkflowService {

  def add(workflow: Workflow): Future[Workflow]
}

object WorkflowService {

  def apply(storage: Repository[Workflow]): WorkflowService = WorkflowServiceImp(storage)

  case class WorkflowServiceImp(repository: Repository[Workflow]) extends WorkflowService {

    override def add(workflow: Workflow): Future[Workflow] = {
      repository.add(workflow)
    }
  }
}