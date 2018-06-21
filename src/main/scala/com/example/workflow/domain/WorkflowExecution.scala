package com.example.workflow.domain

import java.time.LocalDateTime

case class WorkflowExecution(
  id: Long,
  workflowId: Long,
  currentStep: Int = 0,
  timestamp: LocalDateTime = LocalDateTime.now()
) extends Identifiable