package leakcanary.internal

import com.squareup.haha.perflib.ClassInstance
import com.squareup.haha.perflib.Instance
import leakcanary.HahaHelper.asString
import leakcanary.HahaHelper.classInstanceValues
import leakcanary.HahaHelper.fieldValue

/**
 * Represents a [leakcanary.KeyedWeakReference] read from the heap dump.
 */
sealed class KeyedWeakReferenceMirror(
  fields: List<ClassInstance.FieldValue>,
  heapDumpUptimeMillis: Long
) {
  val key = asString(fieldValue<Any>(fields, "key")!!)
  val name = asString(fieldValue<Any>(fields, "name")!!)
  val className = asString(fieldValue<Any>(fields, "className")!!)
  val watchDurationMillis = heapDumpUptimeMillis - fieldValue<Long>(fields, "watchUptimeMillis")!!

  companion object {
    fun fromInstance(
      weakRef: Instance,
      heapDumpUptimeMillis: Long
    ): KeyedWeakReferenceMirror {
      val values = classInstanceValues(weakRef)
      val referent = fieldValue<Instance>(values, "referent")
      return if (referent != null) {
        HasReferent(values, heapDumpUptimeMillis, referent)
      } else {
        Cleared(values, heapDumpUptimeMillis)
      }
    }

  }
}

class Cleared(
  fields: List<ClassInstance.FieldValue>,
  heapDumpUptimeMillis: Long
) : KeyedWeakReferenceMirror(fields, heapDumpUptimeMillis)

class HasReferent(
  fields: List<ClassInstance.FieldValue>,
  heapDumpUptimeMillis: Long,
  val referent: Instance
) : KeyedWeakReferenceMirror(fields, heapDumpUptimeMillis)
