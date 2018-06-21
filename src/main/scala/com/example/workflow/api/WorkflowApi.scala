package com.example.workflow.api

import com.example.workflow.domain.Workflow
import com.example.workflow.service.WorkflowService
import io.circe.generic.auto._
import io.finch.circe._
import io.finch.syntax.post
import io.finch.{ Endpoint, _ }

trait WorkflowApi {

  def addWorkflow: Endpoint[Workflow]

  val api = addWorkflow
}

object WorkflowApi {

  def apply(service: WorkflowService): WorkflowApi = WorkflowApiImp(service)

  case class WorkflowApiImp(service: WorkflowService) extends WorkflowApi {

    private def postedWorkflow: Endpoint[Workflow] = jsonBody[Workflow]

    override def addWorkflow: Endpoint[Workflow] = post("workflow" :: postedWorkflow) { workflow: Workflow =>
      service.add(workflow).map(Ok)
    }
  }
}

