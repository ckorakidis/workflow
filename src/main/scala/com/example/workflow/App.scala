package com.example.workflow

import com.example.workflow.api.util.{ ErrorEncoder, LocalDateTimeEncoder }
import com.example.workflow.api.{ WorkflowApi, WorkflowExecutionApi }
import com.example.workflow.repository.{ WorkflowExecutionRepository, WorkflowRepository }
import com.example.workflow.service.{ WorkflowExecutionService, WorkflowService }
import com.twitter.app.Flag
import com.twitter.finagle.Http
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.auto._
import io.finch.circe._
import io.finch.{ Application, BadRequest, NotFound, InternalServerError }
import com.twitter.conversions.time._

object App extends TwitterServer {

  implicit val LocalDateTimeFormat: LocalDateTimeEncoder = new LocalDateTimeEncoder
  implicit val customConfig: Configuration = Configuration.default.withDefaults
  implicit val errorEncoder: Encoder[Exception] = ErrorEncoder()

  val port: Flag[Int] = flag("port", 8080, "TCP port for HTTP server")

  def main(): Unit = {

    val workflowRepository = WorkflowRepository()
    val workflowService = WorkflowService(WorkflowRepository())
    val workflowApi = WorkflowApi(workflowService).api

    val workflowExecutionRepository = WorkflowExecutionRepository()
    val workflowExecutionService = WorkflowExecutionService(workflowExecutionRepository, workflowRepository)
    val executionApi = WorkflowExecutionApi(workflowExecutionService).api

    val api = (workflowApi :+: executionApi).handle {
      case e: NoSuchElementException => NotFound(e)
      case e: IllegalArgumentException => BadRequest(e)
      case e: IllegalStateException => BadRequest(e)
      case e: Exception => InternalServerError(e)
    }

    val server =
      Http.server
        .withStatsReceiver(statsReceiver)
        .serve(s":${port()}", api.toServiceAs[Application.Json])
    closeOnExit(server)

    workflowExecutionService.keepClean(1.minute, 60)

    Await.ready(adminHttpServer)
  }
}