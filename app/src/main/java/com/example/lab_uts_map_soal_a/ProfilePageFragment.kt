package com.example.lab_uts_map_soal_a

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class ProfilePageFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_page, container, false)

        auth = FirebaseAuth.getInstance()

        view.findViewById<Button>(R.id.logout_button).setOnClickListener {
            auth.signOut()
            // Hide the bottom navigation bar
            val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNavigationView.visibility = View.GONE
            // Redirect to LoginFragment
            requireActivity().supportFragmentManager.commit {
                replace(R.id.main, LoginFragment())
            }
        }

        return view
    }
}