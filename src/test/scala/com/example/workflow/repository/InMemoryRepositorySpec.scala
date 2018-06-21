package com.example.workflow.repository

import com.example.workflow.domain.{ Identifiable, Workflow }
import com.twitter.util.{ Await, Future }
import net.dericbourg.daily.utils.twitter.util.future.TwitterFutures
import org.scalatest._

class InMemoryRepositorySpec extends FunSpec with BeforeAndAfterEach with TwitterFutures {

  private val repo: Repository[Workflow] = new InMemoryRepository[Workflow] {}

  override def beforeEach() {
    Await.ready(repo.deleteAll)
  }

  it("adds and returns given entry") {
    val added: Future[Workflow] = repo.add(Workflow(1, 1))

    whenReady(added) { added =>
      assert(added.id == 1)
      whenReady(repo.getAll) { all =>
        assert(all.size == 1)
      }
    }
  }

  it("does not add given entry if exists already with same id and returns existing") {
    val added: Future[Workflow] = repo.add(Workflow(1, 1))

    whenReady(added) { _ =>
      whenReady(repo.add(Workflow(1, 2))) { addedOrExisting =>
        assert(addedOrExisting == Workflow(1, 1))
        whenReady(repo.getAll) { all =>
          assert(all.size == 1)
        }
      }
    }
  }

  it("deletes contained entry") {
    val workflow = Workflow(1, 1)
    val added: Future[Workflow] = repo.add(workflow)

    whenReady(added) { _ =>
      whenReady(repo.delete(workflow)) { deleted =>
        assert(deleted.get.id == 1)
        whenReady(repo.getAll) { all =>
          assert(all.isEmpty)
        }
      }
    }
  }

  it("cannot delete not contained entry") {
    whenReady(repo.delete(Workflow(1, 1))) { deleted =>
      assert(deleted.isEmpty)
    }
  }

  it("updates contained entry") {
    val added: Future[Workflow] = repo.add(Workflow(1, 1))

    whenReady(added) { _ =>
      whenReady(repo.update(Workflow(1, 2))) { updated =>
        assert(updated.get.steps == 2)
        whenReady(repo.get(1)) { retrieved =>
          assert(retrieved.get.steps == 2)
        }
      }
    }
  }

  it("cannot update not contained entry") {
    whenReady(repo.update(Workflow(1, 2))) { updated =>
      assert(updated.isEmpty)
    }
  }

  it("gets contained entry") {
    val added: Future[Identifiable] = repo.add(Workflow(1, 1))

    whenReady(added) { _ =>
      whenReady(repo.get(1)) { retrieved =>
        assert(retrieved.get.id == 1)
      }
    }
  }

  it("cannot get not contained entry") {
    whenReady(repo.get(1)) { retrieved =>
      assert(retrieved.isEmpty)
    }
  }

  it("gets all contained entries") {
    val added: Future[Identifiable] = repo.add(Workflow(1, 1))

    whenReady(added) { _ =>
      whenReady(repo.add(Workflow(2, 1))) { _ =>
        whenReady(repo.getAll) { workflows =>
          assert(workflows.size == 2)
        }
      }
    }
  }

}
