package com.example.lab_uts_map_soal_a

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavigationManager.OnNavigationItemSelectedListener {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navigationManager = NavigationManager(this)
        navigationManager.setupWithBottomNavigationView(bottomNavigationView, this)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // User is not logged in, hide the bottom navigation bar
            bottomNavigationView.visibility = View.GONE
            // Load LoginFragment by default
            if (savedInstanceState == null) {
                supportFragmentManager.commit {
                    replace(R.id.main, LoginFragment())
                }
            }
        } else {
            // User is logged in, show the bottom navigation bar
            bottomNavigationView.visibility = View.VISIBLE
            bottomNavigationView.selectedItemId = R.id.page_2 // Set home page as active

            // Load HomePageFragment by default
            if (savedInstanceState == null) {
                supportFragmentManager.commit {
                    replace(R.id.main, HomeFragment())
                }
            }
        }
    }

    override fun onNavigationItemSelected(itemId: Int): Boolean {
        return when (itemId) {
            R.id.page_1 -> {
                supportFragmentManager.commit {
                    replace(R.id.main, AddStoryFragment())
                }
                true
            }
            R.id.page_2 -> {
                supportFragmentManager.commit {
                    replace(R.id.main, HomeFragment())
                }
                true
            }
            R.id.page_3 -> {
                supportFragmentManager.commit {
                    replace(R.id.main, ProfilePageFragment())
                }
                true
            }
            else -> false
        }
    }
}