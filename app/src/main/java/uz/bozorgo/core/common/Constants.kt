package uz.bozorgo.core.common

/** Ilova bo'yicha umumiy konstantalar. */
object Constants {
    /** Yetkazib berish bepul bo'ladigan minimal summa (UZS, demo). */
    const val FREE_DELIVERY_THRESHOLD = 100_000.0

    /** Minimal summadan past bo'lganda yetkazib berish narxi. */
    const val DELIVERY_FEE = 15_000.0

    /** Tezkor yetkazib berish narxi. */
    const val EXPRESS_DELIVERY_FEE = 25_000.0

    /** Sahifalab yuklash — bir sahifadagi mahsulotlar soni. */
    const val PAGE_SIZE = 24

    /** Mahsulot rasmlari joylashgan assets papkasi. */
    const val ASSET_IMAGE_PREFIX = "file:///android_asset/"

    /** Seed fayllari. */
    const val SEED_CATALOG = "seed/catalog.json"
    const val SEED_CATEGORIES = "seed/categories.json"
    const val SEED_BANNERS = "seed/banners.json"

    /** Seed versiyasi: katalog yangilanganda oshiriladi. */
    const val SEED_VERSION = 1
}
