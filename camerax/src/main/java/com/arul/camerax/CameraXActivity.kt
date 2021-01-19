package com.arul.camerax

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.arul.camerax.databinding.ActivityCameraXBinding
import com.arul.camerax.utils.LuminosityAnalyzer
import com.arul.camerax.utils.toast
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.android.synthetic.main.activity_camera_x.*
import java.io.File
import java.util.concurrent.Executors

class CameraXActivity : AppCompatActivity() {

    private val requestCodePermission = 10

    private val requestPermission = arrayOf(Manifest.permission.CAMERA)

    private lateinit var binding: ActivityCameraXBinding

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private lateinit var imagePreview: Preview

    private lateinit var imageAnalysis: ImageAnalysis

    private lateinit var imageCapture: ImageCapture

    private lateinit var previewView: PreviewView

    private val executor = Executors.newSingleThreadExecutor()

    private lateinit var outputFile: File

    private lateinit var cameraControl: CameraControl

    private lateinit var cameraInfo: CameraInfo

    private var linearZoom = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraXBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!intent.hasExtra(MediaStore.EXTRA_OUTPUT)) {
            toast("Something went wrong")
            setResult(RESULT_CANCELED)
            finish()
            return
        } else {
            outputFile = File(intent.getStringExtra(MediaStore.EXTRA_OUTPUT)!!)
        }

        previewView = binding.previewView
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        if (allPermissionsGranted()) {
            previewView.post { startCamera() }
        } else {
            Toast.makeText(
                applicationContext,
                "App  require camera permission to run",
                Toast.LENGTH_LONG
            ).show()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                requestPermissions(requestPermission, requestCodePermission)
            }
        }

        camera_capture_button.setOnClickListener {
            camera_capture_button.isEnabled = false
            takePicture()
        }

        binding.cameraTorchButton.setOnClickListener {
            toggleTorch()
        }
    }

    private fun toggleTorch() {
        if (cameraInfo.torchState.value == TorchState.ON) {
            cameraControl.enableTorch(false)
        } else {
            cameraControl.enableTorch(true)
        }
    }

    private fun takePicture() {
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        imageCapture.takePicture(
            outputFileOptions, executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    cameraControl.enableTorch(false)
                    previewView.post {
                        setResult(RESULT_OK)
                        finish()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    val msg = "Photo capture failed: ${exception.message}"
                    previewView.post {
                        Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                }
            }
        )
    }

    private fun startCamera() {
        imagePreview = Preview.Builder().apply {
            setTargetAspectRatio(AspectRatio.RATIO_16_9)
            setTargetRotation(previewView.display.rotation)
        }.build()

        imageAnalysis = ImageAnalysis.Builder().apply {
            setImageQueueDepth(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        }.build()
        imageAnalysis.setAnalyzer(executor, LuminosityAnalyzer())

        imageCapture = ImageCapture.Builder().apply {
            setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            setFlashMode(ImageCapture.FLASH_MODE_OFF)
        }.build()

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                val camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    imagePreview,
                    imageCapture
                )
                previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                imagePreview.setSurfaceProvider(previewView.surfaceProvider)

                cameraControl = camera.cameraControl
                cameraInfo = camera.cameraInfo
                setTorchStateObserver()
                setZoomStateObserver()
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun setTorchStateObserver() {
        cameraInfo.torchState.observe(
            this,
            { state ->
                if (state == TorchState.ON) {
                    binding.cameraTorchButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_flash_on_24dp
                        )
                    )
                } else {
                    binding.cameraTorchButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_flash_off_24dp
                        )
                    )
                }
            }
        )
    }

    private fun setZoomStateObserver() {
        cameraInfo.zoomState.observe(
            this,
            { state ->
                // state.linearZoom

                // state.zoomRatio
                // state.maxZoomRatio
                // state.minZoomRatio
                Log.d(TAG, "${state.linearZoom}")
            }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodePermission) {
            if (allPermissionsGranted()) {
                previewView.post { startCamera() }
            } else {
                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = requestPermission.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (linearZoom <= 0.9) {
                    linearZoom += 0.1f
                }
                cameraControl.setLinearZoom(linearZoom)
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (linearZoom >= 0.1) {
                    linearZoom -= 0.1f
                }
                cameraControl.setLinearZoom(linearZoom)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    companion object {
        private const val TAG = "CameraActivity"
    }

    fun onGoBack(view: View) {
        setResult(RESULT_CANCELED)
        finish()
    }
}
