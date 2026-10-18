package uz.bozorgo.ui.screens.settings

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uz.bozorgo.core.util.humanReadableSize
import uz.bozorgo.data.local.datastore.UserPreferences
import uz.bozorgo.data.remote.ApiConfig
import uz.bozorgo.domain.repository.ProductRepository
import javax.inject.Inject

data class SettingsUiState(
    val language: String = "",
    val themeMode: String = "system",
    val notificationsEnabled: Boolean = true,
    val cacheSize: String = "",
    val catalogCount: Int = 0,
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    val isServerConfigured: Boolean = false,
    val showClearCacheDialog: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val preferences: UserPreferences,
    private val productRepository: ProductRepository,
    private val imageLoader: ImageLoader,
    private val apiConfig: ApiConfig,
) : ViewModel() {

    private val _cacheSize = MutableStateFlow("")
    private val _catalogCount = MutableStateFlow(0)
    private val _showClearCacheDialog = MutableStateFlow(false)

    init {
        refreshCacheSize()
        refreshCatalogCount()
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        preferences.language,
        preferences.themeMode,
        preferences.notificationsEnabled,
    ) { language, theme, notifications ->
        SettingsUiState(
            language = language,
            themeMode = theme.ifBlank { "system" },
            notificationsEnabled = notifications,
        )
    }
        .combine(_cacheSize) { state, cache -> state.copy(cacheSize = cache) }
        .combine(_catalogCount) { state, count -> state.copy(catalogCount = count) }
        .combine(_showClearCacheDialog) { state, show -> state.copy(showClearCacheDialog = show) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SettingsUiState(
                versionName = versionName(),
                versionCode = versionCode(),
                isServerConfigured = apiConfig.isConfigured,
            ),
        )

    fun setLanguage(code: String) {
        viewModelScope.launch {
            preferences.setLanguage(code)
            val tags = if (code.isBlank()) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(code)
            }
            AppCompatDelegate.setApplicationLocales(tags)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setNotificationsEnabled(enabled) }
    }

    fun clearSearchHistory() {
        viewModelScope.launch { preferences.clearSearchHistory() }
    }

    fun requestClearCache() {
        _showClearCacheDialog.value = true
    }

    fun dismissClearCache() {
        _showClearCacheDialog.value = false
    }

    fun clearCache() {
        viewModelScope.launch {
            _showClearCacheDialog.value = false
            withContext(Dispatchers.IO) {
                runCatching { imageLoader.memoryCache?.clear() }
                runCatching { imageLoader.diskCache?.clear() }
            }
            refreshCacheSize()
        }
    }

    fun reseedCatalog() {
        viewModelScope.launch {
            runCatching { productRepository.ensureSeeded() }
            refreshCatalogCount()
        }
    }

    private fun refreshCacheSize() {
        viewModelScope.launch {
            // DiskCache hajmini Okio orqali emas, Coil'ning o'z hisobidan olamiz
            val size = withContext(Dispatchers.IO) { imageLoader.diskCache?.size ?: 0L }
            _cacheSize.value = humanReadableSize(size)
        }
    }

    private fun refreshCatalogCount() {
        viewModelScope.launch {
            _catalogCount.value = runCatching { productRepository.countTotal() }.getOrDefault(0)
        }
    }

    private fun Context.packageInfo(): android.content.pm.PackageInfo =
        packageManager.getPackageInfo(packageName, 0)

    private fun versionName(): String = runCatching {
        application.packageInfo().versionName ?: "1.0.0"
    }.getOrDefault("1.0.0")

    private fun versionCode(): Int = runCatching {
        val info = application.packageInfo()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            info.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            info.versionCode
        }
    }.getOrDefault(1)
}
