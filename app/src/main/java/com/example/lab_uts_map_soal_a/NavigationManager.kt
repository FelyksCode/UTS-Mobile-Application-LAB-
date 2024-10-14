package com.example.lab_uts_map_soal_a

import android.content.Context
import com.google.android.material.bottomnavigation.BottomNavigationView

class NavigationManager(private val context: Context) {

    interface OnNavigationItemSelectedListener {
        fun onNavigationItemSelected(itemId: Int): Boolean
    }

    fun setupWithBottomNavigationView(
        bottomNavigationView: BottomNavigationView,
        listener: OnNavigationItemSelectedListener
    ) {
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            listener.onNavigationItemSelected(item.itemId)
        }
    }
}