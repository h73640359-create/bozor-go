package uz.bozorgo.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.bozorgo.core.common.Resource
import uz.bozorgo.domain.model.User

interface UserRepository {

    val isLoggedIn: Flow<Boolean>
    val currentUser: Flow<User>
    val isAdmin: Flow<Boolean>

    suspend fun requestOtp(phone: String): Resource<String>

    suspend fun verifyOtp(phone: String, code: String, name: String? = null): Resource<User>

    suspend fun updateProfile(name: String, phone: String)

    suspend fun logout()
}
