package com.example.esportstracker.data.auth

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/** Lưu ảnh đại diện trong bộ nhớ riêng của app: avatars/user_<id>.jpg */
object AvatarStore {
    private val _version = MutableStateFlow(0L)
    val version: StateFlow<Long> = _version.asStateFlow()

    private fun file(context: Context, userId: Long): File {
        val dir = File(context.filesDir, "avatars")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "user_$userId.jpg")
    }

    fun fileIfExists(context: Context, userId: Long): File? =
        file(context, userId).takeIf { it.exists() }

    suspend fun save(context: Context, userId: Long, uri: Uri): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val bitmap = decode(context.applicationContext, uri, 512)
                    ?: return@withContext false
                file(context, userId).outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 88, it)
                }
                _version.value = System.currentTimeMillis()
                true
            } catch (e: Exception) {
                false
            }
        }

    suspend fun delete(context: Context, userId: Long) {
        withContext(Dispatchers.IO) {
            file(context, userId).delete()
            _version.value = System.currentTimeMillis()
        }
    }

    private fun decode(context: Context, uri: Uri, maxSide: Int): Bitmap? {
        if (Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val w = info.size.width
                val h = info.size.height
                val longest = maxOf(w, h)
                if (longest > maxSide) {
                    val scale = maxSide.toFloat() / longest
                    decoder.setTargetSize(
                        (w * scale).toInt().coerceAtLeast(1),
                        (h * scale).toInt().coerceAtLeast(1)
                    )
                }
            }
        }
        // Android 8.x
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide * 2) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    }
}