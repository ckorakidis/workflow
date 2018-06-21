package com.example.workflow.api

import com.example.workflow.api.util.LocalDateTimeEncoder
import com.example.workflow.domain.WorkflowExecution
import com.example.workflow.service.WorkflowExecutionService
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.auto._
import io.finch.circe._
import io.finch.syntax.{ get, post, put }
import io.finch.{ Endpoint, _ }

trait WorkflowExecutionApi {

  def getWorkflowExecution: Endpoint[WorkflowExecution]

  def increaseState: Endpoint[WorkflowExecution]

  def addWorkflowExecution: Endpoint[WorkflowExecution]

  val api = getWorkflowExecution :+: increaseState :+: addWorkflowExecution
}

object WorkflowExecutionApi {
  implicit val LocalDateTimeFormat: LocalDateTimeEncoder = new LocalDateTimeEncoder
  implicit val customConfig: Configuration = Configuration.default.withDefaults

  def apply(service: WorkflowExecutionService): WorkflowExecutionApi = WorkflowExecutionApiImp(service)

  case class WorkflowExecutionApiImp(service: WorkflowExecutionService) extends WorkflowExecutionApi {

    override def getWorkflowExecution: Endpoint[WorkflowExecution] =
      get("execution" :: path[Long]) { id: Long =>
        service.get(id).map(
          o => if (o.isEmpty)
            NotFound(new NoSuchElementException("workflow execution does not exist"))
          else
            Ok(o.get)
        )
      }

    override def increaseState: Endpoint[WorkflowExecution] =
      put("execution" :: path[Long] :: "increase") { id: Long =>
        service
          .increaseState(id)
          .map(
            o => if (o.isEmpty)
              InternalServerError(new IllegalStateException("Could not increase step of the given execution"))
            else
              Ok(o.get)
          )
      }

    private def postedWorkflowExecution: Endpoint[WorkflowExecution] = jsonBody[WorkflowExecution]

    override def addWorkflowExecution: Endpoint[WorkflowExecution] =
      post("execution" :: postedWorkflowExecution) { workflowExecution: WorkflowExecution =>
        service.add(workflowExecution).map(Ok)
      }
  }
}

