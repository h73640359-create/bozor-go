package uz.bozorgo

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import org.junit.Test
import uz.bozorgo.core.common.Constants
import uz.bozorgo.domain.model.Product
import java.io.File
import java.security.MessageDigest

/**
 * Rasmlar bo'yicha avtomatlashtirilgan tekshiruvlar:
 *
 *  - har bir mahsulotning rasm manbasi bor va fayl haqiqatan mavjud;
 *  - rasmlar WebP formatida, bo'sh emas va hajmi me'yorda;
 *  - bitta rasm yuzlab mahsulotlarga biriktirilmagan;
 *  - rasm mahsulot identifikatori bo'yicha bog'langan (nom bo'yicha taxmin emas);
 *  - har bir kategoriyaning karta rasmi va zaxira (fallback) rasmi bor;
 *  - uzoq (internet) manzillari to'g'ri formatda;
 *  - domen modeli mavjud fayllarga yo'naltiradi.
 *
 * Tekshiruv muvaffaqiyatsiz bo'lsa — aniq xabar chiqadi, jim o'tib ketmaydi.
 */
class ImageAssetsTest {

    private val gson = Gson()

    /** Bir chizma rasm bilan bo'lishilishi mumkin bo'lgan eng ko'p mahsulot soni. */
    private val maxSharedImage = 5

    /** Bir foto bilan bo'lishilishi mumkin bo'lgan eng ko'p mahsulot soni. */
    private val maxSharedPhoto = 45

    private val catalog: JsonObject by lazy {
        gson.fromJson(TestFiles.seed("catalog.json").readText(), JsonObject::class.java)
    }

    private val products: List<JsonObject> by lazy {
        gson.fromJson<List<JsonObject>>(
            catalog.getAsJsonArray("products").toString(),
            object : TypeToken<List<JsonObject>>() {}.type,
        )
    }

    private val categories: List<JsonObject> by lazy {
        val root = gson.fromJson(TestFiles.seed("categories.json").readText(), JsonObject::class.java)
        gson.fromJson<List<JsonObject>>(
            root.getAsJsonArray("categories").toString(),
            object : TypeToken<List<JsonObject>>() {}.type,
        )
    }

    private val productsDir: File by lazy {
        TestFiles.assetDir("products") ?: error(
            "Rasm papkasi topilmadi: products. Avval `python3 data/scripts/sync_assets.py` ni ishga tushiring.",
        )
    }

    // ---------------------------------------------------------------- 1

    @Test
    fun everyProductHasLocalImageFile() {
        val missing = mutableListOf<String>()
        val broken = mutableListOf<String>()

        products.forEach { product ->
            val id = product["id"].asString
            val image = product["image"]?.asString.orEmpty()
            if (image.isBlank()) {
                missing += "$id (rasm maydoni bo'sh)"
                return@forEach
            }
            val file = TestFiles.assetImage(image)
            if (file == null) {
                missing += "$id -> $image"
                return@forEach
            }
            if (!isWebp(file)) broken += "$id -> $image (WebP emas)"
            if (file.length() < 500) broken += "$id -> $image (hajmi ${file.length()} bayt)"
            if (file.length() > 250_000) broken += "$id -> $image (juda katta: ${file.length()} bayt)"
        }

        assertWithMessage("Rasm fayli topilmagan mahsulotlar (${missing.size} ta): ${missing.take(10)}")
            .that(missing).isEmpty()
        assertWithMessage("Yaroqsiz rasm fayllari (${broken.size} ta): ${broken.take(10)}")
            .that(broken).isEmpty()
    }

    @Test
    fun imageFileNamesMatchProductOrSubcategory() {
        // Rasm yo mahsulot ID siga (chizma illustratsiya), yo kichik bo'lim
        // fotosiga bog'langan — nom bo'yicha taxmin qilinmaydi.
        val mismatched = products.mapNotNull { product ->
            val id = product["id"].asString
            val subcategory = product["subcategoryId"].asString
            val image = product["image"]?.asString.orEmpty()
            val ok = image == "products/$id.webp" ||
                (image.startsWith("products/${subcategory}__") && image.endsWith(".webp"))
            if (ok) null else "$id -> $image"
        }
        assertWithMessage(
            "Rasm nomi mahsulot ID si/kichik bo'limiga mos kelmagan " +
                "(${mismatched.size} ta): ${mismatched.take(10)}",
        ).that(mismatched).isEmpty()
    }

    @Test
    fun photoImagesExistAndAreWebp() {
        val photos = products.map { it["image"]?.asString.orEmpty() }.filter { "__" in it }.toSet()
        val broken = photos.mapNotNull { path ->
            val file = TestFiles.assetImage(path)
            if (file == null) "$path (fayl yo'q)"
            else if (!isWebp(file)) "$path (WebP emas)"
            else if (file.length() < 2000) "$path (juda kichik: ${file.length()} bayt)"
            else null
        }
        assertWithMessage("Foto rasmlarda muammo: ${broken.take(10)}").that(broken).isEmpty()
        // Katalogda foto rasmlar bo'lishi kerak (kamida bir nechta kichik bo'lim uchun)
        assertWithMessage("Kamida 5 ta kichik bo'lim uchun foto rasm bo'lishi kerak")
            .that(photos.size).isAtLeast(5)
    }

    @Test
    fun imageFolderContainsEveryReferencedImage() {
        val files = productsDir.listFiles { file -> file.extension == "webp" }.orEmpty()
        assertWithMessage("Rasm papkasi bo'sh yoki topilmadi: ${productsDir.path}")
            .that(files).isNotEmpty()

        val referenced = products.map { it["image"].asString }.toSet()
        val missing = referenced.filter { TestFiles.assetImage(it) == null }
        assertWithMessage("Katalogdagi rasm fayli topilmadi: ${missing.take(10)}")
            .that(missing).isEmpty()

        // Har bir mahsulot uchun rasm manbasi bor (1000 ta) — takrorlanishi mumkin,
        // lekin bitta rasm yuzlab mahsulotda bo'lishi mumkin emas.
        assertWithMessage("Har bir mahsulot rasmga bog'langan bo'lishi kerak")
            .that(referenced.isNotEmpty()).isTrue()
        assertWithMessage("Kamida 1000 ta mahsulot rasmga bog'langan bo'lishi kerak")
            .that(products.size).isAtLeast(1000)
    }

    // ---------------------------------------------------------------- 2

    @Test
    fun imagesAreNotReusedAcrossManyProducts() {
        val byHash = mutableMapOf<String, MutableList<String>>()
        products.forEach { product ->
            val image = product["image"]?.asString.orEmpty()
            val file = TestFiles.assetImage(image) ?: return@forEach
            byHash.getOrPut(md5(file)) { mutableListOf() }.add(product["id"].asString)
        }

        // Chizma illustratsiyalar: deyarli har mahsulotda o'ziga xos.
        val imageById = products.associateBy { it["id"].asString }
        val drawingGroups = byHash.values.filter { ids ->
            "__" !in imageById.getValue(ids.first())["image"].asString
        }
        val tooMany = drawingGroups.filter { it.size > maxSharedImage }
        assertWithMessage(
            "Bir xil chizma $maxSharedImage dan ortiq mahsulotda ishlatilgan: " +
                tooMany.take(5),
        ).that(tooMany).isEmpty()

        // Foto rasmlar bir kichik bo'lim ichida bo'lishiladi (o'nlab mahsulotda),
        // lekin yuzlab mahsulotda emas.
        val photoGroups = byHash.values.filter { ids ->
            "__" in imageById.getValue(ids.first())["image"].asString
        }
        val photoTooMany = photoGroups.filter { it.size > maxSharedPhoto }
        assertWithMessage(
            "Bir foto $maxSharedPhoto dan ortiq mahsulotda ishlatilgan: ${photoTooMany.take(3)}",
        ).that(photoTooMany).isEmpty()

        // Rasm bo'lishilganda ham faqat bitta kichik bo'lim ichida bo'lishi kerak.
        val crossSubcategory = byHash.filter { (_, ids) ->
            val subcategories = products.filter { it["id"].asString in ids }
                .map { it["subcategoryId"].asString }
                .toSet()
            subcategories.size > 1
        }
        assertWithMessage(
            "Bir xil rasm turli kichik bo'limlardagi mahsulotlarda ishlatilgan: " +
                crossSubcategory.values.take(5),
        ).that(crossSubcategory).isEmpty()

        assertWithMessage("Rasmlar yetarlicha xilma-xil bo'lishi kerak")
            .that(byHash.size).isAtLeast(products.size / 2)
    }

    // ---------------------------------------------------------------- 3

    @Test
    fun everyCategoryHasCardImageAndFallback() {
        val missingCard = mutableListOf<String>()
        val missingFallback = mutableListOf<String>()

        categories.forEach { category ->
            val id = category["id"].asString
            val card = TestFiles.assetImage("categories/$id.webp")
            val fallback = TestFiles.assetImage("fallback/$id.webp")
            if (card == null || !isWebp(card)) missingCard += id
            if (fallback == null || !isWebp(fallback)) missingFallback += id
        }

        assertWithMessage("Karta rasmi yo'q kategoriyalar: $missingCard").that(missingCard).isEmpty()
        assertWithMessage("Zaxira rasmi yo'q kategoriyalar: $missingFallback").that(missingFallback).isEmpty()
        assertWithMessage("Kamida 15 ta kategoriya bo'lishi kerak").that(categories.size).isAtLeast(15)
    }

    @Test
    fun everyProductCategoryHasFallbackImage() {
        val categoryIds = categories.map { it["id"].asString }.toSet()
        val orphans = products.map { it["categoryId"].asString }.toSet() - categoryIds
        assertWithMessage("Noma'lum kategoriyali mahsulotlar: $orphans").that(orphans).isEmpty()

        val missing = products.mapNotNull { product ->
            val categoryId = product["categoryId"].asString
            if (TestFiles.assetImage("fallback/$categoryId.webp") == null) {
                "${product["id"].asString} ($categoryId)"
            } else {
                null
            }
        }
        assertWithMessage("Zaxira rasmi yo'q mahsulotlar: ${missing.take(10)}").that(missing).isEmpty()
    }

    // ---------------------------------------------------------------- 4

    @Test
    fun remoteImageUrlsAreWellFormed() {
        val invalid = products.mapNotNull { product ->
            val url = product["imageUrl"]?.asString.orEmpty()
            if (url.isBlank()) return@mapNotNull null
            val ok = (url.startsWith("https://") || url.startsWith("http://")) &&
                url.removePrefix("https://").removePrefix("http://").substringBefore("/").contains(".")
            if (ok) null else "${product["id"].asString} -> $url"
        }
        assertWithMessage("Noto'g'ri uzoq rasm manzili: ${invalid.take(10)}").that(invalid).isEmpty()
    }

    // ---------------------------------------------------------------- 5

    @Test
    fun domainModelResolvesExistingFiles() {
        val sample = products.take(50)
        sample.forEach { raw ->
            val product = Product(
                id = raw["id"].asString,
                name = raw["name"].asString,
                categoryId = raw["categoryId"].asString,
                categoryName = raw["categoryName"].asString,
                subcategoryId = raw["subcategoryId"].asString,
                subcategoryName = raw["subcategoryName"].asString,
                brand = raw["brand"].asString,
                imageAssetPath = raw["image"].asString,
                imageUrl = raw["imageUrl"]?.asString.orEmpty(),
                description = raw["description"].asString,
                price = raw["price"].asDouble,
                unit = raw["unit"].asString,
                unitLabel = raw["unitLabel"].asString,
                stock = raw["stock"].asInt,
                inStock = raw["inStock"].asBoolean,
                rating = raw["rating"].asDouble,
                reviewCount = raw["reviewCount"].asInt,
            )

            val model = product.imageModel()
            assertWithMessage("Rasm manbasi asset prefiksi bilan boshlanishi kerak: $model")
                .that(model.startsWith(Constants.ASSET_IMAGE_PREFIX)).isTrue()

            val relative = model.removePrefix(Constants.ASSET_IMAGE_PREFIX)
            val file = TestFiles.assetImage(relative)
            assertWithMessage("Model ko'rsatgan fayl topilmadi: $relative").that(file).isNotNull()

            val fallback = product.fallbackImageModel()
            assertWithMessage("Zaxira rasm kategoriyaga mos bo'lishi kerak: $fallback")
                .that(fallback).isEqualTo("${Constants.FALLBACK_IMAGE_PREFIX}${product.categoryId}.webp")
            val fallbackFile = TestFiles.assetImage(fallback.removePrefix(Constants.ASSET_IMAGE_PREFIX))
            assertWithMessage("Zaxira rasm fayli topilmadi: $fallback").that(fallbackFile).isNotNull()
        }
    }

    @Test
    fun emptyImageFallsBackToCategoryImage() {
        val product = Product(
            id = "prd-test",
            name = "Test mahsulot",
            categoryId = categories.first()["id"].asString,
            categoryName = "Test",
            subcategoryId = "test",
            subcategoryName = "Test",
            brand = "Test",
            imageAssetPath = "",
            imageUrl = "",
            description = "Test",
            price = 1000.0,
            unit = "dona",
            unitLabel = "1 dona",
            stock = 1,
            inStock = true,
            rating = 4.0,
            reviewCount = 0,
        )

        assertWithMessage("Bo'sh rasm uchun ham model bo'sh bo'lmasligi kerak (kulrang kvadrat qolmaydi)")
            .that(product.imageModel()).isEqualTo(product.fallbackImageModel())
        assertThat(product.imageModelOrNull() ?: product.fallbackImageModel())
            .isEqualTo(product.fallbackImageModel())
    }

    // ---------------------------------------------------------------- yordamchilar

    private fun isWebp(file: File): Boolean {
        val header = file.inputStream().use { input ->
            val bytes = ByteArray(12)
            val read = input.read(bytes)
            if (read < 12) return false
            bytes
        }
        return String(header, 0, 4) == "RIFF" && String(header, 8, 4) == "WEBP"
    }

    private fun md5(file: File): String {
        val digest = MessageDigest.getInstance("MD5")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
