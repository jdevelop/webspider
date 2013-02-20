package com.webspider.storage.bdbje.persist

import com.sleepycat.bind.tuple.{ TupleInput, TupleOutput, TupleBinding }
import java.util.UUID
import java.nio.ByteBuffer
import com.webspider.core.{ LinkStorageState, Link }

object LinkSerializer {

  val keySerializer: TupleBinding[UUID] = new LinkKeySerializer

  val linkSerializer: TupleBinding[Link] = new LinkSerializer

  private[this] class LinkKeySerializer extends TupleBinding[UUID] {

    def entryToObject(src: TupleInput): UUID = {
      val buf = Array[Byte](16)
      src.read(buf, 0, 16)
      val bb = ByteBuffer.wrap(buf)
      new UUID(bb.getLong(), bb.getLong())
    }

    def objectToEntry(src: UUID, out: TupleOutput) {
      val bb = ByteBuffer.wrap(Array[Byte](16))
      bb.putLong(src.getMostSignificantBits)
      bb.putLong(src.getLeastSignificantBits)
      out.write(bb.array())
    }

  }

  private[this] class LinkUrlSerializer[URLT] extends TupleBinding[URLT] {

    def entryToObject(src: TupleInput): URLT = {
      src.readInt()
      src.readString().asInstanceOf[URLT]
    }

    def objectToEntry(src: URLT, out: TupleOutput) {
      out.writeInt(src.hashCode())
      out.writeString(src.toString)
    }
  }

  private[this] class LinkSerializer extends TupleBinding[Link] {
    def entryToObject(src: TupleInput): Link = {
      val id = keySerializer.entryToObject(src)
      val link = src.readString()
      Link(link, id, storageState = LinkStorageState(src.readInt()))
    }

    def objectToEntry(src: Link, out: TupleOutput) {
      keySerializer.objectToEntry(src.id, out)
      out.writeString(src.link)
      out.writeInt(src.storageState.id)
    }
  }

}