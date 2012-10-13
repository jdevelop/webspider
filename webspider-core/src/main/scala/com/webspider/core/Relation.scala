package com.webspider.core

import java.util.UUID

case class Relation(recordId: UUID) {

  private var parents = Set[UUID]()

  private var children = Set[UUID]()

  def addChild(childId: UUID) {
    children += childId
  }

  def addParent(parentId: UUID) {
    parents += parentId
  }

  def getParents = parents

  def getChildren = children

}
