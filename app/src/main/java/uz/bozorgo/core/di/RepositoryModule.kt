package uz.bozorgo.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.bozorgo.data.repository.AddressRepositoryImpl
import uz.bozorgo.data.repository.CartRepositoryImpl
import uz.bozorgo.data.repository.CatalogRepositoryImpl
import uz.bozorgo.data.repository.FavoriteRepositoryImpl
import uz.bozorgo.data.repository.OrderRepositoryImpl
import uz.bozorgo.data.repository.ProductRepositoryImpl
import uz.bozorgo.data.repository.UserRepositoryImpl
import uz.bozorgo.domain.repository.AddressRepository
import uz.bozorgo.domain.repository.CartRepository
import uz.bozorgo.domain.repository.CatalogRepository
import uz.bozorgo.domain.repository.FavoriteRepository
import uz.bozorgo.domain.repository.OrderRepository
import uz.bozorgo.domain.repository.ProductRepository
import uz.bozorgo.domain.repository.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindCatalogRepository(impl: CatalogRepositoryImpl): CatalogRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindAddressRepository(impl: AddressRepositoryImpl): AddressRepository
}
