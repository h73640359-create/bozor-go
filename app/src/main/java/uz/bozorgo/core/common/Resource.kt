package uz.bozorgo.core.common

/**
 * Barcha repository / ViewModel qatlamlari uchun yagona natija turi.
 *
 * Loading  — ma'lumot yuklanmoqda
 * Success  — muvaffaqiyatli natija
 * Error    — xatolik (foydalanuvchiga ko'rsatish uchun xabar bilan)
 */
sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val isNetworkError: Boolean = false,
    ) : Resource<Nothing>()

    val isLoading: Boolean get() = this is Loading
    fun getOrNull(): T? = (this as? Success)?.data
}

inline fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> = when (this) {
    is Resource.Success -> Resource.Success(transform(data))
    is Resource.Error -> this
    is Resource.Loading -> this
}

suspend fun <T> runSafely(
    isNetworkCall: Boolean = false,
    block: suspend () -> T,
): Resource<T> = try {
    Resource.Success(block())
} catch (t: Throwable) {
    Resource.Error(
        message = t.message ?: "Noma'lum xatolik",
        cause = t,
        isNetworkError = isNetworkCall,
    )
}
