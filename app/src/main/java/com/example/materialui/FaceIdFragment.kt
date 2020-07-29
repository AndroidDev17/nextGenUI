package com.example.materialui

import android.Manifest
import android.content.Context
import android.hardware.camera2.CaptureRequest
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Range
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.face_id_fragment.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


open class FaceIdFragment : Fragment() {

    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private lateinit var imageAnalyzer: ImageAnalysis
    private val cameraExecutor = Executors.newFixedThreadPool(2)
    private lateinit var faceAnalyzer: FaceAnalyzer
    private var cameraProvider: ProcessCameraProvider? = null

    // CAMERA Permissions Contract
    private val askCameraPermission by lazy {
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted ->
            if (isPermissionGranted) {
                log(TAG, "permission granted ...")
                startCamera()
            } else {
                log(TAG, "permission not granted ...")
                requireContext().toast("permission not granted ...")
            }
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.face_id_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        askCameraPermission.launch(Manifest.permission.CAMERA)
        btn_capture.setOnClickListener {
            takePhoto()
        }
        val faceBoundary = FaceBoundary(requireContext())
        faceAnalyzer = FaceAnalyzer(faceBoundary)
        faceBoundary.id = View.generateViewId()
        root_container.addView(faceBoundary)
        val constraint = ConstraintSet()
        constraint.clone(root_container as ConstraintLayout)
        constraint.connect(
            faceBoundary.id,
            ConstraintSet.RIGHT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.RIGHT
        )
        constraint.connect(
            faceBoundary.id,
            ConstraintSet.LEFT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.LEFT
        )
        constraint.connect(
            faceBoundary.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP
        )
        constraint.connect(
            faceBoundary.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM
        )
        constraint.applyTo(root_container)
        faceAnalyzer.getFaceInBoundaryLiveData()
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                if (it) {
                    requireContext().toast("face inside")
                    takePhoto()
                }
            })
    }

    private fun startCamera() {
        //This is used to bind the lifecycle of cameras to the lifecycle owner.
        //This allows you to not worry about opening and closing the camera since CameraX is lifecycle aware.
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                .build()

            // Select Front camera
            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()

            try {
                // make sure nothing is bound to your cameraProvider
                // Unbind use cases before rebinding
                cameraProvider?.unbindAll()

                // Bind use cases to camera
                bindCameraUseCase()

                camera = cameraProvider?.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture, imageAnalyzer
                )
                // Attach the viewFinder's surface provider to the preview use case.
                preview?.setSurfaceProvider(camera_preview_view.createSurfaceProvider(camera?.cameraInfo))


            } catch (exc: Exception) {
                log(TAG, "Use case binding failed exception :: $exc")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create timestamped output file to hold the image
        val photoFile = File(
            getOutputDirectory(requireContext()),
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Setup image capture listener which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    log(TAG, "Photo capture failed: ${exc.message} , exc :: $exc")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    requireContext().toast(msg)
                    navigateToFacePhoto(savedUri.toString())
                    log(TAG, msg)
                }
            })
    }

    private fun navigateToFacePhoto(url:String) {
        findNavController().navigate(
            FaceIdFragmentDirections.actionFaceIdFragmentToFacePhotoFragment(ImageUrl =url )
        )
    }

    private fun bindCameraUseCase() {
        val metrics = DisplayMetrics().also { camera_preview_view.display.getRealMetrics(it) }
        log(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        log(TAG, "Preview aspect ratio: $screenAspectRatio")

        val rotation = camera_preview_view.display.rotation

        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // We request aspect ratio but no resolution to match preview config, but letting
            // CameraX optimize for whatever specific resolution best fits our use cases
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()


        val builder = ImageAnalysis.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)

        val ext: Camera2Interop.Extender<*> = Camera2Interop.Extender(builder)
        ext.setCaptureRequestOption(
            CaptureRequest.CONTROL_AE_MODE,
            CaptureRequest.CONTROL_AE_MODE_OFF
        )
        ext.setCaptureRequestOption(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range<Int>(30, 30))

        imageAnalyzer = builder.build().apply {
            setAnalyzer(cameraExecutor, faceAnalyzer)
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun getOutputDirectory(context: Context): File {
        val appContext = context.applicationContext
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraProvider?.unbindAll()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}