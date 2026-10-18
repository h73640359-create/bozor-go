package uz.bozorgo.data.seed

import android.content.res.AssetManager
import com.google.gson.Gson
import uz.bozorgo.core.common.Constants
import uz.bozorgo.data.seed.dto.BannersSeedDto
import uz.bozorgo.data.seed.dto.CatalogSeedDto
import uz.bozorgo.data.seed.dto.CategoriesSeedDto
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

/**
 * APK ichidagi (`assets/seed/`) katalog fayllarini o'qish.
 *
 * Fayllar `data/catalog.json` dan `data/scripts/sync_assets.py` yordamida
 * ko'chiriladi. Internet bo'lmasa ham katalog to'liq mavjud bo'ladi.
 */
@Singleton
class SeedImporter @Inject constructor(
    private val assets: AssetManager,
    private val gson: Gson,
) {

    fun readCatalog(): CatalogSeedDto = read(Constants.SEED_CATALOG, CatalogSeedDto::class.java)

    fun readCategories(): CategoriesSeedDto = read(Constants.SEED_CATEGORIES, CategoriesSeedDto::class.java)

    fun readBanners(): BannersSeedDto = read(Constants.SEED_BANNERS, BannersSeedDto::class.java)

    private fun <T> read(path: String, clazz: Class<T>): T {
        assets.open(path).use { stream ->
            InputStreamReader(stream, StandardCharsets.UTF_8).use { reader ->
                return gson.fromJson(reader, clazz)
                    ?: throw IllegalStateException("Seed faylini o'qib bo'lmadi: $path")
            }
        }
    }
}
