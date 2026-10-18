package uz.bozorgo.data.remote

/**
 * Server manzili sozlamasi.
 *
 * `API_BASE_URL` `local.properties` yoki `BOZORGO_API_BASE_URL` muhit
 * o'zgaruvchisi orqali beriladi. Bo'sh bo'lsa — ilova to'liq oflayn
 * (lokal baza) rejimida ishlaydi va server chaqiruvlari bajarilmaydi.
 */
data class ApiConfig(
    val rawBaseUrl: String,
) {
    val isConfigured: Boolean get() = rawBaseUrl.isNotBlank()

    /** Retrofit uchun tayyor manzil (oxiri `/` bilan). */
    val effectiveBaseUrl: String
        get() {
            if (!isConfigured) return PLACEHOLDER
            val withSlash = if (rawBaseUrl.endsWith("/")) rawBaseUrl else "$rawBaseUrl/"
            return if (withSlash.startsWith("http")) withSlash else "https://$withSlash"
        }

    companion object {
        /** Server sozlanmaganda ishlatiladigan (chaqirilmaydigan) manzil. */
        const val PLACEHOLDER = "https://api.bozorgo.local/"
    }
}
