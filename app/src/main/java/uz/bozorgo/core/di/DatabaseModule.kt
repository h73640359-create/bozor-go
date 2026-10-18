package uz.bozorgo.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uz.bozorgo.data.local.db.AppDatabase
import uz.bozorgo.data.local.db.dao.AddressDao
import uz.bozorgo.data.local.db.dao.CartDao
import uz.bozorgo.data.local.db.dao.FavoriteDao
import uz.bozorgo.data.local.db.dao.OrderDao
import uz.bozorgo.data.local.db.dao.ProductDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            // Demo loyiha: sxema o'zgarganda lokal baza qayta yaratiladi.
            // Production loyihada bu yerda migratsiya yozilishi kerak.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()

    @Provides
    fun provideCartDao(db: AppDatabase): CartDao = db.cartDao()

    @Provides
    fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideOrderDao(db: AppDatabase): OrderDao = db.orderDao()

    @Provides
    fun provideAddressDao(db: AppDatabase): AddressDao = db.addressDao()
}
