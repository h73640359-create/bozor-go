package uz.bozorgo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import coil.Coil
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import uz.bozorgo.core.di.ImageLoaderEntryPoint

/**
 * BozorGo ilovasi.
 *
 * Hilt orqali dependency injection ishga tushiriladi.
 * Bildirishnoma kanallari ham shu yerda yaratiladi (Android 8+).
 */
@HiltAndroidApp
class BozorGoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        installCoilImageLoader()
        createNotificationChannels()
    }

    /**
     * Hilt bergan ImageLoader'ni Coil uchun asosiy (global) qilib o'rnatadi.
     * Shunda `AsyncImage` ham xotira + disk keshdan foydalanadi va
     * "Keshni tozalash" sozlamasi haqiqatan rasmlar keshini tozalaydi.
     */
    private fun installCoilImageLoader() {
        runCatching {
            val loader = EntryPointAccessors.fromApplication(
                this,
                ImageLoaderEntryPoint::class.java,
            ).imageLoader()
            Coil.setImageLoader(loader)
        }.onFailure { error ->
            Log.w(TAG, "ImageLoader o'rnatilmadi, Coil standart sozlamada ishlaydi", error)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val orders = NotificationChannel(
            CHANNEL_ORDERS,
            getString(R.string.channel_orders_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = getString(R.string.channel_orders_description)
        }

        val promo = NotificationChannel(
            CHANNEL_PROMO,
            getString(R.string.channel_promo_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.channel_promo_description)
        }

        manager.createNotificationChannels(listOf(orders, promo))
    }

    companion object {
        private const val TAG = "BozorGoApp"
        const val CHANNEL_ORDERS = "bozorgo_orders"
        const val CHANNEL_PROMO = "bozorgo_promo"
    }
}
