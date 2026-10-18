package uz.bozorgo.data.repository

import uz.bozorgo.core.util.isValidUzPhone
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import uz.bozorgo.core.common.Resource
import uz.bozorgo.core.di.IoDispatcher
import uz.bozorgo.data.local.datastore.UserPreferences
import uz.bozorgo.data.remote.ApiConfig
import uz.bozorgo.data.remote.ApiService
import uz.bozorgo.data.remote.dto.OtpRequestDto
import uz.bozorgo.data.remote.dto.OtpVerifyDto
import uz.bozorgo.domain.model.User
import uz.bozorgo.domain.repository.UserRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Telefon raqami orqali autentifikatsiya.
 *
 * HAQIQIY SMS: ishlashi uchun `backend/` dagi SMS provayderi sozlanishi kerak
 * (qarang: `docs/BACKEND.md`). Provayder ulanmaganda demo rejim ishlaydi va
 * bu foydalanuvchiga aniq ko'rsatiladi — hech qachon "haqiqiy SMS yuborildi"
 * deb aytilmaydi.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val preferences: UserPreferences,
    private val apiService: ApiService,
    private val apiConfig: ApiConfig,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : UserRepository {

    override val isLoggedIn: Flow<Boolean> = preferences.isLoggedIn

    override val currentUser: Flow<User> = combine(
        preferences.userId,
        preferences.userName,
        preferences.userPhone,
        preferences.isAdmin,
    ) { id, name, phone, isAdmin ->
        User(id = id, name = name, phone = phone, isAdmin = isAdmin)
    }

    override val isAdmin: Flow<Boolean> = preferences.isAdmin

    override suspend fun requestOtp(phone: String): Resource<String> = withContext(ioDispatcher) {
        if (!isValidPhone(phone)) {
            return@withContext Resource.Error("Telefon raqami noto'g'ri")
        }
        if (!apiConfig.isConfigured) {
            // Demo rejim: kod qurilmada generatsiya qilinadi (SMS yuborilmaydi)
            val code = generateCode()
            return@withContext Resource.Success(code)
        }
        try {
            val response = apiService.requestOtp(OtpRequestDto(phone = phone))
            if (response.success) {
                Resource.Success(response.debugCode ?: "")
            } else {
                Resource.Error(response.message ?: "Kod yuborib bo'lmadi")
            }
        } catch (t: Throwable) {
            Resource.Error(
                message = t.message ?: "Server bilan bog'lanib bo'lmadi",
                cause = t,
                isNetworkError = true,
            )
        }
    }

    override suspend fun verifyOtp(phone: String, code: String, name: String?): Resource<User> =
        withContext(ioDispatcher) {
            if (!isValidPhone(phone)) {
                return@withContext Resource.Error("Telefon raqami noto'g'ri")
            }
            if (code.length != 6) {
                return@withContext Resource.Error("Kod 6 xonali bo'lishi kerak")
            }
            if (apiConfig.isConfigured) {
                try {
                    val response = apiService.verifyOtp(
                        OtpVerifyDto(phone = phone, code = code, name = name),
                    )
                    val user = User(
                        id = response.user.id,
                        name = response.user.name ?: name.orEmpty(),
                        phone = response.user.phone,
                        isAdmin = response.user.isAdmin,
                    )
                    preferences.saveSession(user, response.token)
                    return@withContext Resource.Success(user)
                } catch (t: Throwable) {
                    return@withContext Resource.Error(
                        message = t.message ?: "Kodni tekshirib bo'lmadi",
                        cause = t,
                        isNetworkError = true,
                    )
                }
            }
            // Demo rejim: server bo'lmasa, istalgan 6 xonali kod qabul qilinadi
            val user = User(
                id = "local-" + UUID.randomUUID().toString().take(8),
                name = name.orEmpty().ifBlank { "Foydalanuvchi" },
                phone = phone,
                isAdmin = false,
            )
            preferences.saveSession(user, token = "demo-token")
            Resource.Success(user)
        }

    override suspend fun updateProfile(name: String, phone: String) = withContext(ioDispatcher) {
        preferences.updateProfile(name, phone)
    }

    override suspend fun logout() = withContext(ioDispatcher) {
        preferences.clearSession()
    }

    companion object {
        fun isValidPhone(phone: String): Boolean = isValidUzPhone(phone)

        private fun generateCode(): String =
            (1..6).map { Random.nextInt(0, 10) }.joinToString("")
    }
}
