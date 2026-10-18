package uz.bozorgo.core.di

import coil.ImageLoader
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt bergan [ImageLoader] ni olish uchun kirish nuqtasi.
 *
 * `BozorGoApp` shu orqali yuklovchini Coil uchun global qilib o'rnatadi —
 * shunda `AsyncImage` ham xotira + disk keshidan foydalanadi va
 * "Sozlamalar → Keshni tozalash" haqiqatan rasmlar keshini tozalaydi.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ImageLoaderEntryPoint {
    fun imageLoader(): ImageLoader
}
