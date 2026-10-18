package uz.bozorgo.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import uz.bozorgo.core.di.IoDispatcher
import uz.bozorgo.data.local.db.dao.AddressDao
import uz.bozorgo.data.local.db.entity.AddressEntity
import uz.bozorgo.domain.model.Address
import uz.bozorgo.domain.repository.AddressRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepositoryImpl @Inject constructor(
    private val addressDao: AddressDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AddressRepository {

    override fun observeAddresses(): Flow<List<Address>> =
        addressDao.observeAddresses().map { list -> list.map { it.toDomain() } }

    override fun observeDefault(): Flow<Address?> =
        addressDao.observeDefault().map { it?.toDomain() }

    override suspend fun getById(id: String): Address? = withContext(ioDispatcher) {
        addressDao.getById(id)?.toDomain()
    }

    override suspend fun save(address: Address) = withContext(ioDispatcher) {
        if (address.isDefault) {
            addressDao.clearDefault()
        }
        addressDao.insert(address.toEntity())
        if (address.isDefault) {
            addressDao.markDefault(address.id)
        }
    }

    override suspend fun delete(id: String) = withContext(ioDispatcher) {
        addressDao.delete(id)
    }
}

private fun AddressEntity.toDomain(): Address = Address(
    id = id,
    title = title,
    city = city,
    district = district,
    street = street,
    house = house,
    apartment = apartment,
    comment = comment,
    isDefault = isDefault,
)

private fun Address.toEntity(): AddressEntity = AddressEntity(
    id = id,
    title = title,
    city = city,
    district = district,
    street = street,
    house = house,
    apartment = apartment,
    comment = comment,
    isDefault = isDefault,
)
