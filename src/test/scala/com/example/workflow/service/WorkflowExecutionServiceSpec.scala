package com.example.workflow.service

import com.example.workflow.domain.{ Workflow, WorkflowExecution }
import com.example.workflow.repository.{ InMemoryRepository, Repository }
import com.twitter.conversions.time._
import com.twitter.util.{ Await, Future, Try }
import net.dericbourg.daily.utils.twitter.util.future.TwitterFutures
import net.dericbourg.daily.utils.twitter.utils.TwitterConverters._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

class WorkflowExecutionServiceSpec extends FunSpec with BeforeAndAfterEach with TwitterFutures {

  private val wfRepository: Repository[Workflow] = new InMemoryRepository[Workflow] {}
  private val repository: Repository[WorkflowExecution] = new InMemoryRepository[WorkflowExecution] {}
  private val service: WorkflowExecutionService = WorkflowExecutionService(repository, wfRepository)

  override def beforeEach() {

    Await.ready(wfRepository.deleteAll)
    Await.ready(repository.deleteAll)
  }

  it("adds and returns given entry") {
    val wfe = WorkflowExecution(1, 2)
    val addedWf: Future[Workflow] = wfRepository.add(Workflow(2, 1))

    whenReady(addedWf) { _ =>
      whenReady(service.add(wfe)) { addedWfe =>
        assert(addedWfe == wfe)
        whenReady(service.get(1)) { retrieved =>
          assert(retrieved.get == wfe)
        }
      }
    }
  }

  it("cannot add entry related to non-existing workflow; returns exception") {
    val added = service.add(WorkflowExecution(1, 2)).asScala.failed

    ScalaFutures.whenReady(added) { result =>
      assert(result.isInstanceOf[IllegalArgumentException])
    }
  }

  it("increases state of contained entry") {
    val wfe = WorkflowExecution(1, 2)
    val updatedWfe = WorkflowExecution(1, 2, 1, wfe.timestamp)
    val addedWf: Future[Workflow] = wfRepository.add(Workflow(2, 1))

    whenReady(addedWf) { _ =>
      whenReady(service.add(wfe)) { _ =>
        whenReady(service.increaseState(1)) { _ =>
          whenReady(service.get(1)) { retrieved =>
            assert(retrieved.get == updatedWfe)
          }
        }
      }
    }
  }

  it("cannot increase state of contained entry if reached max") {
    val wfe = WorkflowExecution(1, 2)
    val addedWf: Future[Workflow] = wfRepository.add(Workflow(2, 1))

    whenReady(addedWf) { _ =>
      whenReady(service.add(wfe)) { _ =>
        whenReady(service.increaseState(1)) { _ =>
          ScalaFutures.whenReady(service.increaseState(1).asScala.failed) { failure =>
            assert(failure.isInstanceOf[IllegalStateException])
          }
        }
      }
    }
  }

  it("cannot increase state of not contained entry") {
    ScalaFutures.whenReady(service.increaseState(1).asScala.failed) { failure =>
      assert(failure.isInstanceOf[NoSuchElementException])
    }
  }

  it("removes old completed executions") {
    val wfe = WorkflowExecution(1, 2)
    val addedWf: Future[Workflow] = wfRepository.add(Workflow(2, 1))

    whenReady(addedWf) { _ =>
      whenReady(service.add(wfe)) { _ =>
        whenReady(service.increaseState(1)) { _ =>
          whenReady(service.get(1)) { retrieved =>
            assert(retrieved.nonEmpty)
          }

          Try { Await.ready(service.keepClean(1.second, 1), 2.second) }

          whenReady(service.get(1)) { retrieved =>
            assert(retrieved.isEmpty)
          }
        }
      }
    }
  }

  it("does not remove old not completed executions") {
    val wfe = WorkflowExecution(1, 2)
    val addedWf: Future[Workflow] = wfRepository.add(Workflow(2, 1))

    whenReady(addedWf) { _ =>
      whenReady(service.add(wfe)) { _ =>
        whenReady(service.get(1)) { retrieved =>
          assert(retrieved.nonEmpty)
        }

        Try { Await.ready(service.keepClean(1.second, 1), 2.second) }

        whenReady(service.get(1)) { retrieved =>
          assert(retrieved.nonEmpty)
        }
      }
    }
  }

}
