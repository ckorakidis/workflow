package com.example.workflow.service

import java.time.LocalDateTime

import com.example.workflow.domain.{ Workflow, WorkflowExecution }
import com.example.workflow.repository.{ InMemoryRepository, Repository }
import com.twitter.finagle.util.DefaultTimer
import com.twitter.util.{ Duration, Future }

trait WorkflowExecutionService {
  def get(id: Long): Future[Option[WorkflowExecution]]

  def increaseState(id: Long): Future[Option[WorkflowExecution]]

  def add(workflowExecution: WorkflowExecution): Future[WorkflowExecution]

  def keepClean(checkIntervals: Duration, ttlSeconds: Int): Future[Unit]
}

object WorkflowExecutionService {

  def apply(
    repository: Repository[WorkflowExecution],
    wfRepository: Repository[Workflow]
  ): WorkflowExecutionService =
    WorkflowExecutionServiceImp(repository, wfRepository)

  case class WorkflowExecutionServiceImp(
    repository: Repository[WorkflowExecution],
    wfRepository: Repository[Workflow]
  )
      extends WorkflowExecutionService {

    override def get(id: Long): Future[Option[WorkflowExecution]] =
      repository.get(id)

    override def increaseState(id: Long): Future[Option[WorkflowExecution]] = {
      get(id)
        .flatMap[Option[WorkflowExecution]](o =>
          if (o.nonEmpty) {
            wfRepository
              .get(o.get.workflowId)
              .flatMap[Option[WorkflowExecution]](wf =>
                if (wf.get.steps > o.get.currentStep)
                  repository.update(
                  WorkflowExecution(id, o.get.workflowId, o.get.currentStep + 1, o.get.timestamp)
                )
                else
                  Future.exception(new IllegalStateException("No more steps to increase")))
          } else
            Future.exception(new NoSuchElementException("Could not find Workflow Execution with given id")))
    }

    override def keepClean(checkIntervals: Duration, ttlSeconds: Int): Future[Unit] = {

      def isLiableForRemoval(wfe: WorkflowExecution, wf: Workflow): Boolean =
        (wfe.currentStep >= wf.steps) && wfe.timestamp.isBefore(LocalDateTime.now().minusSeconds(ttlSeconds))

      def clean: Future[Unit] = {
        repository
          .getAll
          .onSuccess(l => l.map(wfe => wfRepository
            .get(wfe.workflowId)
            .onSuccess(wf => if (wf.nonEmpty && isLiableForRemoval(wfe, wf.get))
              repository.delete(wfe)))).unit
      }

      for {
        _ <- Future.sleep(checkIntervals)(DefaultTimer)
        _ <- clean
        _ <- keepClean(checkIntervals, ttlSeconds)
      } yield ()
    }

    override def add(workflowExecution: WorkflowExecution): Future[WorkflowExecution] = {
      wfRepository
        .get(workflowExecution.workflowId)
        .flatMap[WorkflowExecution](o =>
          if (o.nonEmpty)
            repository.add(workflowExecution)
          else
            Future.exception(new IllegalArgumentException("Could not find referenced workflow")))
    }
  }
}