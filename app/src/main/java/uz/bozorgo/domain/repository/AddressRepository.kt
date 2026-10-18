package uz.bozorgo.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.bozorgo.domain.model.Address

interface AddressRepository {

    fun observeAddresses(): Flow<List<Address>>

    fun observeDefault(): Flow<Address?>

    suspend fun getById(id: String): Address?

    suspend fun save(address: Address)

    suspend fun delete(id: String)
}
