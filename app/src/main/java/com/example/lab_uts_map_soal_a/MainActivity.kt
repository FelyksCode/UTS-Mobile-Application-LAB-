package com.example.lab_uts_map_soal_a

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), NavigationManager.OnNavigationItemSelectedListener {
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

        // Set the selected item to Home Page
        bottomNavigationView.selectedItemId = R.id.page_2

        navigationManager.setupWithBottomNavigationView(bottomNavigationView, this)
        // Load HomePageFragment by default
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.main, HomePageFragment())
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
                    replace(R.id.main, HomePageFragment())
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