package com.webspider.main.actors

import com.webspider.core.{Link, Task}

sealed trait Message
case class ProcessTask(task: Task) extends Message
case class ProcessLink(link: Link) extends Message
case class LinkProcessingDone(link: Link) extends Message
case class AddToNextProcess(parent: Link, child: Link) extends Message
case class SaveLink(link: Link) extends Message

case object ProcessQueuedLinks extends Message
case object FinishTask extends Message
case object ShowStats extends Message
