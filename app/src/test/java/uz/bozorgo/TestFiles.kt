package uz.bozorgo

import java.io.File

/**
 * Unit testlar Gradle tomonidan `app/` papkasidan ishga tushiriladi,
 * shuning uchun seed fayllar modul ichidagi nisbiy yo'l bilan o'qiladi.
 *
 * Fayl topilmasa test aniq xabar bilan muvaffaqiyatsiz tugaydi —
 * jim o'tib ketmaydi.
 */
object TestFiles {

    private val candidates = listOf(
        "src/main/assets/seed",
        "../app/src/main/assets/seed",
        "../data",
        "../../data",
    )

    fun seed(name: String): File {
        for (dir in candidates) {
            val file = File(dir, name)
            if (file.isFile) return file
        }
        error(
            "Seed fayli topilmadi: $name. Tekshirilgan papkalar: $candidates. " +
                "Avval `python3 data/scripts/sync_assets.py` ni ishga tushiring.",
        )
    }

    /** Rasm papkasini topadi (masalan "products" yoki "categories"). */
    fun assetDir(path: String): File? {
        for (dir in listOf(
                "src/main/assets",
                "../app/src/main/assets",
                "../data/images",
                "../../data/images",
            )) {
            val file = File(dir, path)
            if (file.isDirectory) return file
        }
        return null
    }

    fun assetImage(path: String): File? {
        for (dir in listOf(
                "src/main/assets",
                "../app/src/main/assets",
                "../data/images",
                "../../data/images",
            )) {
            val file = File(dir, path)
            if (file.isFile) return file
        }
        return null
    }
}
