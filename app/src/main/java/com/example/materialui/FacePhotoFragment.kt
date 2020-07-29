package com.example.materialui

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.materialui.faceCongnito.ImageHelper
import com.example.materialui.viewmodels.FacePhotoViewModelFactory
import kotlinx.android.synthetic.main.fragment_face_photo.*

class FacePhotoFragment : Fragment() {

    private val TAG = "FacePhotoFragment"
    private val args: FacePhotoFragmentArgs by navArgs()
    private var bitmap: Bitmap? = null
    private val factory: FacePhotoViewModelFactory by lazy {
        FacePhotoViewModelFactory(Uri.parse(args.ImageUrl), requireContext().applicationContext)
    }
    private val viewModel: FacePhotoViewModel by viewModels {
        factory
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_face_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        log(TAG, "image uri :: ${args.ImageUrl}")
        viewModel.loadBitmap()
        setObserver()
    }

    private fun setObserver() {
        viewModel.getBitmap().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                bitmap = it
                image.setImageBitmap(it)
                viewModel.detectFace(it)
            }
        })
        viewModel.getLiveFaceDetectData().observe(viewLifecycleOwner, Observer {
            var info:String = ""
            if (it.isSuccess ) {
                info = (it.faces?.size.toString() + " face"
                        + (if (it.faces?.size != 1) "s" else "") + " detected")
                if (it.faces != null) {
                    image.setImageBitmap(
                        ImageHelper.drawFaceRectanglesOnBitmap(
                            bitmap!!, it.faces, true
                        )
                    )

                    if (it.faces.size ==1 ) {
                        viewModel.saveFaceId(it.faces[0].faceId)
                        viewModel.saveLoginTypeFaceId()
                        findNavController().navigate(FacePhotoFragmentDirections.actionFacePhotoFragmentToLoginWithFaceIdFragment())
                    }
                } else {
                    info = "0 face detected"
                }
            } else {
                info = " error :: ${it.exception?.message}"
                log(TAG,"trace :: ${it.exception?.stackTrace}")
            }
            tv_info.text = info
        })

        viewModel.getProgressBarStatus().observe(viewLifecycleOwner, Observer {
            if (it) {
                enableProgress()
            } else {
                disableProgress()
            }
        })
    }

    private fun enableProgress() {
        progressBar.visibility = View.VISIBLE
    }

    private fun disableProgress() {
        progressBar.visibility = View.GONE
    }
}