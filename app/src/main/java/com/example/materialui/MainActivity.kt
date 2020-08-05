package com.example.materialui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.materialui.viewmodels.MainViewModelFactory
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var navController : NavController

    private val factory : MainViewModelFactory by lazy {
        MainViewModelFactory(applicationContext)
    }

    private val viewModel : MainViewModel by viewModels {
        factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigateToStart()
//        navController = findNavController(R.id.nav_host_fragment)
//        navController.addOnDestinationChangedListener { controller, destination, arguments ->    }
    }


    override fun onBackPressed() {
        val navHostFragment: NavHostFragment? = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val fragment = navHostFragment!!.childFragmentManager.fragments[0]
        log(TAG,"onBackPressed -> ${fragment::class.simpleName}")
            if (fragment is BackPressAware)
                fragment.onBackPressed()
            else
                super.onBackPressed()
    }

    private fun navigateToStart() {
        val navHostFragment = nav_host_fragment as NavHostFragment
        val graphInflater = navHostFragment.navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.nav_graph)
        navController = navHostFragment.navController

        val destination = when(viewModel.getAuthenticationMode()) {
            AUTHENTICATION_MODE_PIN -> R.id.loginWithPinFragment
            AUTHENTICATION_MODE_FINGERPRINT-> R.id.loginWithFingerprintFragment
            AUTHENTICATION_MODE_FACE_ID -> R.id.loginWithFaceIdFragment
            else -> R.id.motionFragment
        }
        navGraph.startDestination = destination
        navController.graph = navGraph
    }
}