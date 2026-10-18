package uz.bozorgo.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import uz.bozorgo.ui.navigation.BozorGoApp
import uz.bozorgo.ui.theme.BozorGoTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private var notificationsPermissionGranted by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> notificationsPermissionGranted = granted }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Katalog birinchi marta bazaga yozilgunga qadar splash ekran turadi
        splashScreen.setKeepOnScreenCondition { viewModel.isPreparing.value }

        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

        setContent {
            val themeMode = viewModel.themeMode.collectAsStateWithLifecycle().value
            val darkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            BozorGoTheme(darkTheme = darkTheme) {
                val isPreparing = viewModel.isPreparing.collectAsStateWithLifecycle().value
                BozorGoApp(
                    cartBadgeCount = viewModel.cartCount.collectAsStateWithLifecycle().value,
                    isOnline = viewModel.isOnline.collectAsStateWithLifecycle().value,
                    isPreparing = isPreparing,
                    catalogCount = viewModel.catalogCount.collectAsStateWithLifecycle().value,
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            notificationsPermissionGranted = true
            return
        }
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            notificationsPermissionGranted = true
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
