package com.example.materialui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.materialui.viewmodels.LoginWithPinViewModelFactory
import kotlinx.android.synthetic.main.fragment_with_login_pin.*

class LoginWithPinFragment : Fragment() {

    private val factory: LoginWithPinViewModelFactory by lazy {
        LoginWithPinViewModelFactory(requireContext().applicationContext)
    }

    private val viewModel : LoginWithPinViewModel by viewModels {
        factory
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_with_login_pin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edit_text_pin.addTextChangedListener {
            viewModel.validatePin(it.toString())
        }

        observeLiveData()
    }

    private fun observeLiveData() {
        viewModel.getValidationStatus().observe(viewLifecycleOwner, Observer {success->
            if (success) {
                requireActivity().toast("Welcome back User")
            }
        })
    }

}