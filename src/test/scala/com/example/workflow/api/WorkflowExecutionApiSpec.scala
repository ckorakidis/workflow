package com.example.workflow.api

import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import com.example.workflow.domain.{ Workflow, WorkflowExecution }
import com.example.workflow.repository.{ Repository, WorkflowExecutionRepository, WorkflowRepository }
import com.example.workflow.service.WorkflowExecutionService.WorkflowExecutionServiceImp
import com.twitter.finagle.http.Status.NotFound
import com.twitter.io.Buf
import com.twitter.util.Await
import io.finch.{ Application, Input }
import net.dericbourg.daily.utils.twitter.util.future.TwitterFutures
import org.scalatest.{ BeforeAndAfterEach, _ }

class WorkflowExecutionApiSpec extends FunSpec with BeforeAndAfterEach with TwitterFutures {

  private val repository: Repository[WorkflowExecution] = WorkflowExecutionRepository

  private val wfRepository: Repository[Workflow] = WorkflowRepository

  private val api: WorkflowExecutionApi = WorkflowExecutionApi(WorkflowExecutionServiceImp(repository, wfRepository))

  override def beforeEach() {

    Await.ready(wfRepository.deleteAll)
    Await.ready(repository.deleteAll)
  }

  it("adds execution for existing Workflow") {
    whenReady(wfRepository.add(Workflow(1, 1))) { _ =>

      val input = Input.post("/execution")
        .withBody[Application.Json](
          Buf.Utf8("{\"id\":1, \"workflowId\":1}"), Some(StandardCharsets.UTF_8)
        )

      val result = api.addWorkflowExecution(input).awaitValueUnsafe().get

      assert(result.id == 1)
      assert(result.workflowId == 1)
      assert(result.currentStep == 0)

      whenReady(repository.get(1)) { added =>

        val result = added.get
        assert(result.id == 1)
        assert(result.workflowId == 1)
        assert(result.currentStep == 0)
        assert(result.timestamp.until(LocalDateTime.now(), ChronoUnit.SECONDS) < 10)
      }
    }
  }

  it("gets existing execution") {
    whenReady(wfRepository.add(Workflow(1, 1))) { _ =>
      whenReady(repository.add(WorkflowExecution(1, 1))) { _ =>

        val input = Input.get("/execution/1")

        val result = api.getWorkflowExecution(input).awaitValueUnsafe().get

        assert(result.id == 1)
        assert(result.workflowId == 1)
        assert(result.currentStep == 0)
        assert(result.timestamp.until(LocalDateTime.now(), ChronoUnit.SECONDS) < 10)
      }
    }
  }

  it("does not get non-existing execution") {

    val input = Input.get("/execution/1")

    val result = api.getWorkflowExecution(input).awaitOutput().get
    assert(result.get().status == NotFound)
  }

  it("Does not add execution for non-existing Workflow") {
    val input = Input.post("/execution")
      .withBody[Application.Json](
        Buf.Utf8("{\"id\":1, \"workflowId\":1}"), Some(StandardCharsets.UTF_8)
      )

    val result = api.addWorkflowExecution(input).awaitOutput().get
    assert(result.throwable.isInstanceOf[IllegalArgumentException])
  }

  it("Increases state of existing execution") {
    whenReady(wfRepository.add(Workflow(1, 1))) { _ =>
      whenReady(repository.add(WorkflowExecution(1, 1))) { _ =>

        val input = Input.put("/execution/1/increase")

        val result = api.increaseState(input).awaitValueUnsafe().get

        assert(result.id == 1)
        assert(result.workflowId == 1)
        assert(result.currentStep == 1)

      }
    }
  }

  it("Does not Increase state of existing execution if it reached max") {
    whenReady(wfRepository.add(Workflow(1, 1))) { _ =>
      whenReady(repository.add(WorkflowExecution(1, 1, 1, LocalDateTime.now()))) { _ =>

        val input = Input.put("/execution/1/increase")

        val result = api.increaseState(input).awaitOutput().get

        assert(result.throwable.isInstanceOf[IllegalStateException])

      }
    }
  }

  it("Does not increase state of non-existing execution") {
    val input = Input.put("/execution/1/increase")

    val result = api.increaseState(input).awaitOutput().get

    assert(result.throwable.isInstanceOf[NoSuchElementException])
  }

}
