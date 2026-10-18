package uz.bozorgo.core.util

import kotlin.math.abs

/** Fayl hajmini odam o'qiy oladigan shaklga keltirish: 2.4 MB, 512 KB … */
fun humanReadableSize(bytes: Long): String {
    val value = abs(bytes)
    if (value < 1024) return "$value B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var size = value / 1024.0
    var unitIndex = 0
    while (size >= 1024.0 && unitIndex < units.lastIndex) {
        size /= 1024.0
        unitIndex++
    }
    val formatted = if (size >= 10) size.toLong().toString() else "%.1f".format(size)
    return "$formatted ${units[unitIndex]}"
}
