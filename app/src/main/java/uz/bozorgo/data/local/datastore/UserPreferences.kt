package uz.bozorgo.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import uz.bozorgo.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

private val Context.bozorGoDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "bozorgo_preferences",
)

/**
 * Foydalanuvchi sozlamalari va profiling asosiy ma'lumotlari (DataStore).
 *
 * Bu yerda faqat kichik, tez-tez o'zgaradigan sozlamalar saqlanadi.
 * Maxfiy ma'lumotlar (parol, to'lov ma'lumotlari) saqlanmaydi.
 */
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
) {

    private object Keys {
        val SEED_VERSION = intPreferencesKey("seed_version")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val IS_ADMIN = booleanPreferencesKey("is_admin")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val SEARCH_HISTORY = stringPreferencesKey("search_history")
        val SELECTED_ADDRESS_ID = stringPreferencesKey("selected_address_id")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LAST_SYNC_AT = longPreferencesKey("last_sync_at")
    }

    private val dataStore: DataStore<Preferences> get() = context.bozorGoDataStore

    // ------------------------------ Seed ----------------------------------

    val seedVersion: Flow<Int> = read { it[Keys.SEED_VERSION] ?: 0 }

    suspend fun setSeedVersion(version: Int) {
        dataStore.edit { it[Keys.SEED_VERSION] = version }
    }

    // ------------------------------ Profil --------------------------------

    val authToken: Flow<String> = read { it[Keys.AUTH_TOKEN].orEmpty() }

    val isLoggedIn: Flow<Boolean> = authToken.map { it.isNotBlank() }

    val userId: Flow<String> = read { it[Keys.USER_ID].orEmpty() }

    val userName: Flow<String> = read { it[Keys.USER_NAME].orEmpty() }

    val userPhone: Flow<String> = read { it[Keys.USER_PHONE].orEmpty() }

    val isAdmin: Flow<Boolean> = read { it[Keys.IS_ADMIN] ?: false }

    suspend fun currentUser(): User {
        val prefs = dataStore.data.first()
        return User(
            id = prefs[Keys.USER_ID].orEmpty(),
            name = prefs[Keys.USER_NAME].orEmpty(),
            phone = prefs[Keys.USER_PHONE].orEmpty(),
            isAdmin = prefs[Keys.IS_ADMIN] ?: false,
        )
    }

    suspend fun saveSession(user: User, token: String) {
        dataStore.edit { prefs ->
            prefs[Keys.USER_ID] = user.id
            prefs[Keys.USER_NAME] = user.name
            prefs[Keys.USER_PHONE] = user.phone
            prefs[Keys.IS_ADMIN] = user.isAdmin
            prefs[Keys.AUTH_TOKEN] = token
        }
    }

    suspend fun updateProfile(name: String, phone: String) {
        dataStore.edit { prefs ->
            prefs[Keys.USER_NAME] = name
            prefs[Keys.USER_PHONE] = phone
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.USER_NAME)
            prefs.remove(Keys.USER_PHONE)
            prefs.remove(Keys.IS_ADMIN)
            prefs.remove(Keys.AUTH_TOKEN)
        }
    }

    // --------------------------- Qidiruv tarixi ---------------------------

    val searchHistory: Flow<List<String>> = read { prefs ->
        val json = prefs[Keys.SEARCH_HISTORY].orEmpty()
        if (json.isBlank()) {
            emptyList()
        } else {
            runCatching {
                gson.fromJson<List<String>>(json, object : TypeToken<List<String>>() {}.type)
            }.getOrNull() ?: emptyList()
        }
    }

    suspend fun addSearchQuery(query: String) {
        val normalized = query.trim()
        if (normalized.length < 2) return
        val updated = searchHistory.first().toMutableList().apply {
            removeAll { it.equals(normalized, ignoreCase = true) }
            add(0, normalized)
        }.take(10)
        dataStore.edit { it[Keys.SEARCH_HISTORY] = gson.toJson(updated) }
    }

    suspend fun removeSearchQuery(query: String) {
        val updated = searchHistory.first().filterNot { it.equals(query, ignoreCase = true) }
        dataStore.edit { it[Keys.SEARCH_HISTORY] = gson.toJson(updated) }
    }

    suspend fun clearSearchHistory() {
        dataStore.edit { it.remove(Keys.SEARCH_HISTORY) }
    }

    // ------------------------- Boshqa sozlamalar --------------------------

    val selectedAddressId: Flow<String> = read { it[Keys.SELECTED_ADDRESS_ID].orEmpty() }

    suspend fun setSelectedAddressId(id: String) {
        dataStore.edit { it[Keys.SELECTED_ADDRESS_ID] = id }
    }

    val notificationsEnabled: Flow<Boolean> = read { it[Keys.NOTIFICATIONS_ENABLED] ?: true }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    val language: Flow<String> = read { it[Keys.LANGUAGE].orEmpty() }

    val themeMode: Flow<String> = read { it[Keys.THEME_MODE].orEmpty() }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setLanguage(code: String) {
        dataStore.edit { it[Keys.LANGUAGE] = code }
    }

    val lastSyncAt: Flow<Long> = read { it[Keys.LAST_SYNC_AT] ?: 0L }

    suspend fun setLastSyncAt(timestamp: Long) {
        dataStore.edit { it[Keys.LAST_SYNC_AT] = timestamp }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private fun <T> read(transform: suspend (Preferences) -> T): Flow<T> =
        dataStore.data
            .catch { error ->
                if (error is java.io.IOException) emit(emptyPreferences()) else throw error
            }
            .map { transform(it) }
}
