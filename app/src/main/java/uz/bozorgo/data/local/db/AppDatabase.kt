package uz.bozorgo.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import uz.bozorgo.data.local.db.dao.AddressDao
import uz.bozorgo.data.local.db.dao.CartDao
import uz.bozorgo.data.local.db.dao.FavoriteDao
import uz.bozorgo.data.local.db.dao.OrderDao
import uz.bozorgo.data.local.db.dao.ProductDao
import uz.bozorgo.data.local.db.entity.AddressEntity
import uz.bozorgo.data.local.db.entity.CartEntity
import uz.bozorgo.data.local.db.entity.FavoriteEntity
import uz.bozorgo.data.local.db.entity.OrderEntity
import uz.bozorgo.data.local.db.entity.ProductEntity

@Database(
    entities = [
        ProductEntity::class,
        CartEntity::class,
        FavoriteEntity::class,
        OrderEntity::class,
        AddressEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun orderDao(): OrderDao
    abstract fun addressDao(): AddressDao

    companion object {
        const val NAME = "bozorgo.db"
    }
}
