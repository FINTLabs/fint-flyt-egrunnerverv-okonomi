package no.novari.flyt.egrunnerverv.okonomi.domain.logging

import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

fun Any.toMaskedLogMap(): Map<String, Any?> {
    val kClass = this::class
    return kClass.memberProperties.associate { prop ->
        val annotation = prop.findAnnotation<LogMasked>()
        val rawValue = prop.getter.call(this)
        val value =
            if (annotation != null) {
                maskValue(rawValue, annotation)
            } else {
                rawValue
            }
        prop.name to value
    }
}

private fun maskValue(
    value: Any?,
    annotation: LogMasked,
): Any? {
    if (value == null) {
        return null
    }

    if (value !is String) {
        return "***"
    }

    return when (annotation.type) {
        MaskType.FULL -> {
            "********"
        }

        MaskType.KEEP_LAST -> {
            val keep = annotation.last.coerceAtLeast(0)
            val suffix = value.takeLast(keep.coerceAtMost(value.length))
            suffix.padStart(value.length, '*')
        }

        MaskType.KEEP_FIRST -> {
            val keep = annotation.first.coerceAtLeast(0)
            val prefix = value.take(keep.coerceAtMost(value.length))
            prefix.padEnd(value.length, '*')
        }

        MaskType.KEEP_FIRST_AND_LAST -> {
            val firstCount = annotation.first.coerceAtLeast(0)
            val lastCount = annotation.last.coerceAtLeast(0)
            if (firstCount + lastCount >= value.length) {
                value
            } else {
                val prefix = value.take(firstCount)
                val suffix = value.takeLast(lastCount)
                val maskedMiddleLength = (value.length - firstCount - lastCount).coerceAtLeast(0)
                prefix + "*".repeat(maskedMiddleLength) + suffix
            }
        }
    }
}
