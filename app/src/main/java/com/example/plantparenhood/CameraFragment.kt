package com.example.plantparenhood

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

import java.util.concurrent.Executors

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CameraFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CameraFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    companion object {
        private const val REQUEST_IMAGE_CAPTURE=1
        private const val REQUEST_EXTERNAL_STORAGE = 3
    }
    private lateinit var providerFileManager: ProviderFileManager
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var photoInfo: FileInfo? = null
    private var videoInfo: FileInfo? = null
    private var isCapturingVideo = false

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var takeVideoLauncher: ActivityResultLauncher<Uri>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        providerFileManager = ProviderFileManager(
            requireContext(),
            FileHelper(requireContext()),
            requireContext().contentResolver,
            Executors.newSingleThreadExecutor(),
            MediaContentHelper()
        )

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                handleImageCaptureResult(photoInfo?.uri)
            } else {
                Log.e("CameraFragment", "Failed to take picture")
            }
        }
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Handle the selected image URI (e.g., display or upload the image)
                val fileInfo = createFileInfoFromUri(uri)
                providerFileManager.insertImageToStore(fileInfo) // Assuming you have this method in ProviderFileManager
            }
        }


        view.findViewById<Button>(R.id.photo_button).setOnClickListener {
            isCapturingVideo = false
            checkStoragePermission {
                openImageCapture()
            }
        }
        takeVideoLauncher = registerForActivityResult(ActivityResultContracts.CaptureVideo()) {
            providerFileManager.insertVideoToStore(videoInfo)
        }

        view.findViewById<Button>(R.id.video_button).setOnClickListener {
            isCapturingVideo = false
            pickImageLauncher.launch("image/*")
        }

    }
    private fun openImageCapture() {
        photoInfo = providerFileManager.generatePhotoUri(System.currentTimeMillis())
        photoInfo?.uri?.let { takePictureLauncher.launch(it) }
    }
    private fun createFileInfoFromUri(uri: Uri): FileInfo {
        // Define your logic to create a FileInfo object from Uri
        val file = File(uri.path ?: "")
        return FileInfo(
            uri,
            file,
            file.name,
            providerFileManager.fileHelper.getPicturesFolder(),
            "image/jpeg"
        )
    }

    private fun openVideoCapture() {
        videoInfo = providerFileManager.generateVideoUri(System.currentTimeMillis())
        videoInfo?.uri?.let { takeVideoLauncher.launch(it) }
    }

    private fun checkStoragePermission(onPermissionGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            when (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )) {
                PackageManager.PERMISSION_GRANTED -> {
                    onPermissionGranted()
                }
                else -> {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_EXTERNAL_STORAGE
                    )
                }
            }
        } else {
            onPermissionGranted()
        }
    }
    private fun handleImageCaptureResult(uri: Uri?) {
        uri?.let {
            val fileInfo = createFileInfoFromUri(uri)
            providerFileManager.insertImageToStore(fileInfo)
        } ?: run {
            Log.e("CameraFragment", "Image capture was canceled or failed")
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (isCapturingVideo) {
                        openVideoCapture()
                    } else {
                        openImageCapture()
                    }
                }
            }
            else -> {
                // Handle other permissions if necessary
            }
        }
    }

}