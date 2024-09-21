package com.example.plantparenhood

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
//import com.example.plantidentifier.databinding.ActivityMainBinding
import com.example.plantparenhood.BadgesFragment
import com.example.plantparenhood.CameraFragment
import com.example.plantparenhood.GardenFragment
import com.example.plantparenhood.R
import com.example.plantparenhood.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Initializing fragments
    private val gardenFragment = GardenFragment()
    private val cameraFragment = CameraFragment()
    private val badgesFragment = BadgesFragment()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up navigation listener
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_garden -> {
                    navigateToFragment(R.id.navigation_garden)
                    true
                }
                R.id.navigation_camera -> {
                    navigateToFragment(R.id.navigation_camera)
                    true
                }
                R.id.navigation_badges -> {
                    navigateToFragment(R.id.navigation_badges)
                    true
                }
                else -> false
            }
        }
    }
    private fun navigateToFragment(fragmentId: Int){
        findNavController(R.id.nav_host_fragment).navigate(fragmentId)
    }
}
