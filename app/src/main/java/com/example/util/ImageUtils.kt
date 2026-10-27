package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max

object ImageUtils {

    suspend fun uriToBase64(context: Context, uri: Uri, maxDimension: Int = 1024): Pair<String, String>? =
        withContext(Dispatchers.IO) {
            try {
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap == null) return@withContext null

                val width = originalBitmap.width
                val height = originalBitmap.height
                val largest = max(width, height)

                val scaledBitmap = if (largest > maxDimension) {
                    val scaleFactor = maxDimension.toFloat() / largest
                    Bitmap.createScaledBitmap(
                        originalBitmap,
                        (width * scaleFactor).toInt(),
                        (height * scaleFactor).toInt(),
                        true
                    )
                } else {
                    originalBitmap
                }

                val outputStream = ByteArrayOutputStream()
                val compressFormat = if (mimeType.contains("png", ignoreCase = true)) {
                    Bitmap.CompressFormat.PNG
                } else {
                    Bitmap.CompressFormat.JPEG
                }
                scaledBitmap.compress(compressFormat, 85, outputStream)
                val byteArray = outputStream.toByteArray()
                outputStream.close()

                val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                Pair(base64String, if (compressFormat == Bitmap.CompressFormat.PNG) "image/png" else "image/jpeg")
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}
