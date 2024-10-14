package com.example.lab_uts_map_soal_a

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class SignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        auth = FirebaseAuth.getInstance()

        view.findViewById<Button>(R.id.sign_up_button).setOnClickListener {
            val email = view.findViewById<EditText>(R.id.email).text.toString()
            val password = view.findViewById<EditText>(R.id.password).text.toString()
            signUpWithEmail(email, password)
        }

        return view
    }

    private fun signUpWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign up success, navigate to HomePageFragment
                    val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
                    bottomNavigationView.visibility = View.VISIBLE
                    bottomNavigationView.selectedItemId = R.id.page_2 // Set home page as active
                    requireActivity().supportFragmentManager.commit {
                        replace(R.id.main, HomePageFragment())
                    }
                } else {
                    // If sign up fails, display a message to the user.
                    Toast.makeText(context, "Sign Up Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}