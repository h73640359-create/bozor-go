package uz.bozorgo

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import org.junit.Test
import java.io.File

/**
 * Katalog seed faylining majburiy talablari:
 *  - kamida 1000 ta noyob mahsulot;
 *  - ID lar takrorlanmasligi;
 *  - kategoriya ID lari categories.json da mavjudligi;
 *  - nom, narx, tavsif, rasm, birlik to'ldirilganligi;
 *  - demo ma'lumot ekani aniq belgilanganligi.
 */
class CatalogSeedTest {

    private val gson = Gson()

    private val catalog: JsonObject by lazy {
        gson.fromJson(TestFiles.seed("catalog.json").readText(), JsonObject::class.java)
    }

    private val products: List<JsonObject> by lazy {
        gson.fromJson<List<JsonObject>>(
            catalog.getAsJsonArray("products").toString(),
            object : TypeToken<List<JsonObject>>() {}.type,
        )
    }

    private val categoryIds: Set<String> by lazy {
        val root = gson.fromJson(TestFiles.seed("categories.json").readText(), JsonObject::class.java)
        root.getAsJsonArray("categories").map { it.asJsonObject["id"].asString }.toSet()
    }

    @Test
    fun catalogContainsAtLeastOneThousandProducts() {
        assertThat(products.size).isAtLeast(1000)
        assertThat(catalog["productCount"].asInt).isEqualTo(products.size)
    }

    @Test
    fun productIdsAreUnique() {
        val ids = products.map { it["id"].asString }
        val duplicates = ids.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        assertThat(duplicates).isEmpty()
        assertThat(ids.toSet()).hasSize(products.size)
    }

    @Test
    fun productNamesAreUniqueAndNonEmpty() {
        val names = products.map { it["name"].asString.trim() }
        assertThat(names.all { it.length >= 3 }).isTrue()
        // Bir xil nomdagi mahsulotlar ("nusxa") yo'qligi
        val duplicates = names.groupingBy { it.lowercase() }
            .eachCount().filter { it.value > 1 }.keys
        assertThat(duplicates).isEmpty()
    }

    @Test
    fun everyProductHasValidCategory() {
        val invalid = products.filter { it["categoryId"].asString !in categoryIds }
        assertThat(invalid.map { it["id"].asString }).isEmpty()
    }

    @Test
    fun everyProductHasRequiredFields() {
        products.forEach { product ->
            val id = product["id"].asString
            assertThat(product["name"].asString).isNotEmpty()
            assertThat(product["description"].asString.length).isAtLeast(30)
            assertThat(product["price"].asDouble).isGreaterThan(0.0)
            assertThat(product["categoryId"].asString).isNotEmpty()
            assertThat(product["subcategoryId"].asString).isNotEmpty()
            assertThat(product["image"].asString).isNotEmpty()
            assertThat(product["unit"].asString).isIn(VALID_UNITS)
            assertThat(product["brand"].asString).isNotEmpty()
            assertThat(product.has("stock")).isTrue()
            assertThat(product["stock"].asInt).isAtLeast(0)
            assertThat(product["rating"].asDouble).isIn(0.0..5.0)
            assertThat(product["reviewCount"].asInt).isAtLeast(0)
            assertThat(product["inStock"].asBoolean).isEqualTo(product["stock"].asInt > 0)

            val discount = if (product["discountPrice"].isJsonNull) null else product["discountPrice"].asDouble
            if (discount != null) {
                assertThat(discount).isLessThan(product["price"].asDouble)
            }
            assertThat(id).startsWith("prd-")
        }
    }

    @Test
    fun imagesExistInAssetsAndAreUniquePerProduct() {
        val images = products.map { it["image"].asString }
        assertThat(images.toSet()).hasSize(images.size)
        val missing = images.filter { TestFiles.assetImage(it) == null }
        assertThat(missing).isEmpty()
    }

    @Test
    fun categoryDistributionMatchesSpecification() {
        val expected = mapOf(
            "fruits-vegetables" to 100,
            "meat-poultry-fish" to 70,
            "dairy-eggs" to 70,
            "bakery-sweets" to 80,
            "beverages-water" to 70,
            "grocery-staples" to 100,
            "household-cleaning" to 100,
            "personal-care" to 80,
            "baby-products" to 50,
            "clothing-shoes" to 80,
            "phones-electronics" to 60,
            "kitchen-homeware" to 50,
            "sport-outdoor" to 40,
            "pet-supplies" to 30,
            "auto-other" to 20,
        )
        val actual = products.groupingBy { it["categoryId"].asString }.eachCount()
        expected.forEach { (category, count) ->
            assertThat(actual[category]).named("Kategoriya: $category").isEqualTo(count)
        }
        assertThat(actual.values.sum()).isEqualTo(products.size)
    }

    @Test
    fun seedIsMarkedAsDemoData() {
        assertThat(catalog["isDemoData"].asBoolean).isTrue()
        assertThat(catalog["notice"].asString).contains("demo")
        assertThat(catalog["currency"].asString).isEqualTo("UZS")
    }

    @Test
    fun bannersSeedIsValid() {
        val root = gson.fromJson(TestFiles.seed("banners.json").readText(), JsonObject::class.java)
        val banners = root.getAsJsonArray("banners").map { it.asJsonObject }
        assertThat(banners).isNotEmpty()
        banners.forEach { banner ->
            assertThat(banner["id"].asString).isNotEmpty()
            assertThat(banner["title"].asString).isNotEmpty()
            assertThat(TestFiles.assetImage(banner["image"].asString)).isNotNull()
        }
    }

    @Test
    fun categoriesSeedIsValid() {
        val root = gson.fromJson(TestFiles.seed("categories.json").readText(), JsonObject::class.java)
        val categories = root.getAsJsonArray("categories").map { it.asJsonObject }
        assertThat(categories).hasSize(15)
        categories.forEach { category ->
            assertThat(category["id"].asString).isNotEmpty()
            assertThat(category["name"].asString).isNotEmpty()
            assertThat(category["image"].asString).isNotEmpty()
            assertThat(category["subcategories"].asJsonArray).isNotEmpty()
        }
    }

    private companion object {
        val VALID_UNITS = setOf("dona", "kg", "g", "ml", "l", "qadoq", "juft", "bog")
    }
}
