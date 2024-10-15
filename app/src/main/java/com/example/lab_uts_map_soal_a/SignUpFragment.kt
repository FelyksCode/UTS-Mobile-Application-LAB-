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
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        auth = FirebaseAuth.getInstance()
        emailEditText = view.findViewById(R.id.email)
        passwordEditText = view.findViewById(R.id.password)
        confirmPasswordEditText = view.findViewById(R.id.confirm_password)

        view.findViewById<Button>(R.id.sign_up_button).setOnClickListener {
            if (validateForm()) {
                signUpUser()
            }
        }

        return view
    }

    private fun validateForm(): Boolean {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        return when {
            email.isEmpty() -> {
                emailEditText.error = "Email is required"
                false
            }
            password.isEmpty() -> {
                passwordEditText.error = "Password is required"
                false
            }
            confirmPassword.isEmpty() -> {
                confirmPasswordEditText.error = "Confirm Password is required"
                false
            }
            password != confirmPassword -> {
                confirmPasswordEditText.error = "Passwords do not match"
                false
            }
            else -> true
        }
    }

    private fun signUpUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Sign up successful", Toast.LENGTH_SHORT).show()
                    val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
                    bottomNavigationView.visibility = View.VISIBLE
                    bottomNavigationView.selectedItemId = R.id.page_2 // Set home page as active
                    requireActivity().supportFragmentManager.commit {
                        replace(R.id.main, HomeFragment())
                    }
                } else {
                    Toast.makeText(context, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}