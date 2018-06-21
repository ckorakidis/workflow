package com.example.workflow.service

import com.example.workflow.domain.Workflow
import com.example.workflow.repository.{ InMemoryRepository, Repository }
import com.twitter.util.{ Await, Future }
import net.dericbourg.daily.utils.twitter.util.future.TwitterFutures
import org.scalatest._

class WorkflowServiceSpec extends FunSpec with BeforeAndAfterEach with TwitterFutures {

  private var repository: Repository[Workflow] = new InMemoryRepository[Workflow] {}
  private var service: WorkflowService = WorkflowService(repository)

  override def beforeEach() {

    Await.ready(repository.deleteAll)
  }

  it("adds and returns given entry") {
    val added: Future[Workflow] = service.add(Workflow(1, 1))

    whenReady(added) { added =>
      assert(added.id == 1)
      whenReady(repository.getAll) { all =>
        assert(all.size == 1)
      }
    }
  }

}
