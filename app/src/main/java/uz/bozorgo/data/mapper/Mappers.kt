package uz.bozorgo.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import uz.bozorgo.data.local.db.entity.CartWithProduct
import uz.bozorgo.data.local.db.entity.FavoriteWithProduct
import uz.bozorgo.data.local.db.entity.OrderEntity
import uz.bozorgo.data.local.db.entity.ProductEntity
import uz.bozorgo.data.seed.dto.BannerSeedDto
import uz.bozorgo.data.seed.dto.CategorySeedDto
import uz.bozorgo.data.seed.dto.ProductSeedDto
import uz.bozorgo.domain.model.Banner
import uz.bozorgo.domain.model.CartItem
import uz.bozorgo.domain.model.Category
import uz.bozorgo.domain.model.DeliveryMethod
import uz.bozorgo.domain.model.Order
import uz.bozorgo.domain.model.OrderItem
import uz.bozorgo.domain.model.OrderStatus
import uz.bozorgo.domain.model.PaymentMethod
import uz.bozorgo.domain.model.Product
import uz.bozorgo.domain.model.ProductOption
import uz.bozorgo.domain.model.Subcategory

/** Ma'lumotlar qatlamidagi modellarni domen modellarga (va aksincha) o'girish. */
object Mappers {

    private val fallbackGson = Gson()
    private val optionType = object : TypeToken<List<ProductOption>>() {}.type
    private val stringListType = object : TypeToken<List<String>>() {}.type
    private val orderItemType = object : TypeToken<List<OrderItem>>() {}.type

    // ------------------------------- Product --------------------------------

    fun ProductEntity.toDomain(gson: Gson = fallbackGson): Product = Product(
        id = id,
        name = name,
        categoryId = categoryId,
        categoryName = categoryName,
        subcategoryId = subcategoryId,
        subcategoryName = subcategoryName,
        brand = brand,
        imageAssetPath = imageAssetPath,
        imageUrl = imageUrl,
        description = description,
        price = price,
        discountPrice = discountPrice,
        discountPercent = discountPercent,
        unit = unit,
        unitValue = unitValue,
        unitLabel = unitLabel,
        stock = stock,
        inStock = inStock,
        rating = rating,
        reviewCount = reviewCount,
        isPopular = isPopular,
        isNew = isNew,
        isDeal = isDeal,
        options = decodeOptions(optionsJson, gson),
        tags = decodeStringList(tagsJson, gson),
        createdAt = createdAt,
        isDemo = isDemo,
    )

    fun Product.toEntity(gson: Gson = fallbackGson): ProductEntity = ProductEntity(
        id = id,
        name = name,
        categoryId = categoryId,
        categoryName = categoryName,
        subcategoryId = subcategoryId,
        subcategoryName = subcategoryName,
        brand = brand,
        imageAssetPath = imageAssetPath,
        imageUrl = imageUrl,
        description = description,
        price = price,
        discountPrice = discountPrice,
        discountPercent = discountPercent,
        unit = unit,
        unitValue = unitValue,
        unitLabel = unitLabel,
        stock = stock,
        inStock = inStock,
        rating = rating,
        reviewCount = reviewCount,
        isPopular = isPopular,
        isNew = isNew,
        isDeal = isDeal,
        optionsJson = gson.toJson(options),
        tagsJson = gson.toJson(tags),
        createdAt = createdAt,
        isDemo = isDemo,
    )

    fun ProductSeedDto.toEntity(gson: Gson = fallbackGson): ProductEntity = ProductEntity(
        id = id,
        name = name,
        categoryId = categoryId,
        categoryName = categoryName,
        subcategoryId = subcategoryId,
        subcategoryName = subcategoryName,
        brand = brand,
        imageAssetPath = image,
        imageUrl = imageUrl.orEmpty(),
        description = description,
        price = price,
        discountPrice = discountPrice,
        discountPercent = discountPercent,
        unit = unit,
        unitValue = unitValue,
        unitLabel = unitLabel,
        stock = stock,
        inStock = inStock,
        rating = rating,
        reviewCount = reviewCount,
        isPopular = isPopular,
        isNew = isNew,
        isDeal = isDeal,
        optionsJson = gson.toJson(options.map { ProductOption(it.name, it.values) }),
        tagsJson = gson.toJson(tags),
        createdAt = createdAt,
        isDemo = isDemo,
    )

    // ------------------------------ Category -------------------------------

    fun CategorySeedDto.toDomain(): Category = Category(
        id = id,
        name = name,
        icon = icon,
        colorHex = color,
        productCount = productCount,
        subcategories = subcategories.map {
            Subcategory(id = it.id, name = it.name, icon = it.icon, productCount = it.productCount)
        },
    )

    fun BannerSeedDto.toDomain(): Banner = Banner(
        id = id,
        title = title,
        subtitle = subtitle,
        imageAssetPath = image,
        actionType = actionType,
        actionValue = actionValue,
        startColorHex = startColor,
        endColorHex = endColor,
    )

    // -------------------------------- Cart ---------------------------------

    fun CartWithProduct.toDomain(gson: Gson = fallbackGson): CartItem = CartItem(
        product = product.toDomain(gson),
        quantity = cart.quantity,
        selectedOption = cart.selectedOption.takeIf { it.isNotBlank() },
    )

    fun FavoriteWithProduct.toDomain(gson: Gson = fallbackGson): Product = product.toDomain(gson)

    // ------------------------------- Orders --------------------------------

    fun OrderEntity.toDomain(gson: Gson = fallbackGson): Order = Order(
        id = id,
        orderNumber = orderNumber,
        status = OrderStatus.fromKey(status),
        createdAt = createdAt,
        items = runCatching { gson.fromJson<List<OrderItem>>(itemsJson, orderItemType) }
            .getOrNull() ?: emptyList(),
        itemsTotal = itemsTotal,
        discount = discount,
        deliveryFee = deliveryFee,
        total = total,
        deliveryMethod = runCatching { DeliveryMethod.valueOf(deliveryMethod.uppercase()) }
            .getOrElse { DeliveryMethod.STANDARD },
        paymentMethod = runCatching { PaymentMethod.valueOf(paymentMethod.uppercase()) }
            .getOrElse { PaymentMethod.CASH },
        recipientName = recipientName,
        phone = phone,
        addressTitle = addressTitle,
        addressText = addressText,
        comment = comment,
        isSynced = isSynced,
        isPaid = isPaid,
    )

    fun Order.toEntity(gson: Gson = fallbackGson): OrderEntity = OrderEntity(
        id = id,
        orderNumber = orderNumber,
        status = status.key,
        createdAt = createdAt,
        itemsJson = gson.toJson(items),
        itemsTotal = itemsTotal,
        discount = discount,
        deliveryFee = deliveryFee,
        total = total,
        deliveryMethod = deliveryMethod.key,
        paymentMethod = paymentMethod.key,
        recipientName = recipientName,
        phone = phone,
        addressTitle = addressTitle,
        addressText = addressText,
        comment = comment,
        isSynced = isSynced,
        isPaid = isPaid,
    )

    // ------------------------------- yordamchi -----------------------------

    private fun decodeOptions(json: String, gson: Gson): List<ProductOption> =
        runCatching { gson.fromJson<List<ProductOption>>(json, optionType) }.getOrNull() ?: emptyList()

    private fun decodeStringList(json: String, gson: Gson): List<String> =
        runCatching { gson.fromJson<List<String>>(json, stringListType) }.getOrNull() ?: emptyList()
}
