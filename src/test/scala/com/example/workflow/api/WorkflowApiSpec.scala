package com.example.workflow.api

import java.nio.charset.StandardCharsets

import com.example.workflow.domain.Workflow
import com.example.workflow.repository.{ Repository, WorkflowRepository }
import com.example.workflow.service.WorkflowService.WorkflowServiceImp
import com.twitter.io.Buf
import com.twitter.util.Await
import io.finch.{ Application, Input }
import net.dericbourg.daily.utils.twitter.util.future.TwitterFutures
import org.scalatest.{ BeforeAndAfterEach, _ }

class WorkflowApiSpec extends FunSpec with BeforeAndAfterEach with TwitterFutures {

  private val repository: Repository[Workflow] = WorkflowRepository

  private val api: WorkflowApi = WorkflowApi(WorkflowServiceImp(repository))

  override def beforeEach() {
    Await.ready(repository.deleteAll)
  }

  it("adds a posted workflow") {
    val input = Input.post("/workflow")
      .withBody[Application.Json](
        Buf.Utf8("{\"id\":1, \"steps\":2}"), Some(StandardCharsets.UTF_8)
      )

    val result = api.addWorkflow(input)

    assert(result.awaitValueUnsafe().contains(Workflow(1, 2)))
    whenReady(repository.get(1)) { added =>
      assert(added.get == Workflow(1, 2))
    }
  }

  it("does not add a posted workflow with same id like existing one") {
    api.addWorkflow(Input.post("/workflow")
      .withBody[Application.Json](
        Buf.Utf8("{\"id\":1, \"steps\":2}"), Some(StandardCharsets.UTF_8)
      )).awaitValueUnsafe()

    val input = Input.post("/workflow")
      .withBody[Application.Json](
        Buf.Utf8("{\"id\":1, \"steps\":3}"), Some(StandardCharsets.UTF_8)
      )

    val result = api.addWorkflow(input)

    assert(result.awaitValueUnsafe().contains(Workflow(1, 2)))

    whenReady(repository.get(1)) { added =>
      assert(added.get == Workflow(1, 2))
    }
  }

}
