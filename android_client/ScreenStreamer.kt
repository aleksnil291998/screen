package com.example.screenstreamer

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.concurrent.thread

class ScreenStreamer(
    private val mediaProjection: MediaProjection,
    private val serverUrl: String,
    private val streamId: String,
    private val onStatusChange: (String) -> Unit
) {
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var webSocketClient: WebSocketClient? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isStreaming = false
    
    // Настройки трансляции
    private val width = 1280
    private val height = 720
    private val dpi = 320
    
    fun start() {
        if (isStreaming) return
        
        try {
            // Создаем WebSocket соединение
            webSocketClient = WebSocketClient("$serverUrl/ws/stream/$streamId/")
            webSocketClient?.connect()
            
            onStatusChange("Подключение к серверу...")
            
            // Ждем подключения WebSocket
            thread {
                var connected = false
                for (i in 1..50) { // 5 секунд максимум
                    if (webSocketClient?.isConnected() == true) {
                        connected = true
                        break
                    }
                    Thread.sleep(100)
                }
                
                if (connected) {
                    handler.post {
                        startCapture()
                        onStatusChange("Трансляция запущена")
                        isStreaming = true
                    }
                } else {
                    handler.post {
                        onStatusChange("Ошибка подключения к серверу")
                    }
                }
            }
        } catch (e: Exception) {
            onStatusChange("Ошибка: ${e.message}")
        }
    }
    
    private fun startCapture() {
        // Создаем ImageReader для получения скриншотов
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        
        // Создаем виртуальный дисплей
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenStream",
            width,
            height,
            dpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader!!.surface,
            null,
            null
        )
        
        // Запускаем цикл захвата кадров
        thread {
            while (isStreaming) {
                try {
                    val image = imageReader?.acquireLatestImage()
                    image?.use {
                        val bitmap = imageToBitmap(it)
                        val jpegBytes = bitmapToJpeg(bitmap, 80) // 80% качество
                        
                        // Отправляем через WebSocket
                        webSocketClient?.send(jpegBytes)
                    }
                    
                    // Ограничиваем FPS (15 кадров в секунду)
                    Thread.sleep(67) // ~15 FPS
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    private fun imageToBitmap(image: Image): Bitmap {
        val width = image.width
        val height = image.height
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width
        
        val bitmap = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height,
            Bitmap.Config.ARGB_8888
        )
        
        bitmap.copyPixelsFromBuffer(buffer)
        
        // Обрезаем до нужного размера
        return Bitmap.createBitmap(bitmap, 0, 0, width, height)
    }
    
    private fun bitmapToJpeg(bitmap: Bitmap, quality: Int): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }
    
    fun stop() {
        isStreaming = false
        
        virtualDisplay?.release()
        imageReader?.close()
        webSocketClient?.close()
        
        virtualDisplay = null
        imageReader = null
        webSocketClient = null
        
        onStatusChange("Трансляция остановлена")
    }
}
