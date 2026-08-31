package com.example.screenstreamer

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var etServerUrl: EditText
    private lateinit var etStreamId: EditText
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var tvStatus: TextView
    
    private var screenStreamer: ScreenStreamer? = null
    private var mediaProjectionManager: MediaProjectionManager? = null
    
    companion object {
        private const val PERMISSION_CODE = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Инициализация UI элементов
        etServerUrl = findViewById(R.id.etServerUrl)
        etStreamId = findViewById(R.id.etStreamId)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        tvStatus = findViewById(R.id.tvStatus)
        
        // Устанавливаем значения по умолчанию
        etServerUrl.setText("ws://192.168.1.100:8000") // Замените на IP вашего сервера
        etStreamId.setText("demo")
        
        // MediaProjectionManager для захвата экрана
        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        
        btnStart.setOnClickListener {
            requestPermission()
        }
        
        btnStop.setOnClickListener {
            stopStreaming()
        }
        
        updateButtons(false)
    }
    
    private fun requestPermission() {
        val permissionIntent = mediaProjectionManager?.createScreenCaptureIntent()
        startActivityForResult(permissionIntent, PERMISSION_CODE)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PERMISSION_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                startStreaming(resultCode, data)
            } else {
                Toast.makeText(this, "Разрешение на захват экрана не получено", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun startStreaming(resultCode: Int, data: Intent) {
        val serverUrl = etServerUrl.text.toString().trim()
        val streamId = etStreamId.text.toString().trim()
        
        if (serverUrl.isEmpty() || streamId.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }
        
        val mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, data)
        
        if (mediaProjection != null) {
            screenStreamer = ScreenStreamer(
                mediaProjection = mediaProjection,
                serverUrl = serverUrl,
                streamId = streamId,
                onStatusChange = { status ->
                    runOnUiThread {
                        tvStatus.text = status
                    }
                }
            )
            
            screenStreamer?.start()
            updateButtons(true)
        }
    }
    
    private fun stopStreaming() {
        screenStreamer?.stop()
        screenStreamer = null
        updateButtons(false)
        tvStatus.text = "Готово к трансляции"
    }
    
    private fun updateButtons(isStreaming: Boolean) {
        btnStart.isEnabled = !isStreaming
        btnStop.isEnabled = isStreaming
        etServerUrl.isEnabled = !isStreaming
        etStreamId.isEnabled = !isStreaming
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopStreaming()
    }
}
