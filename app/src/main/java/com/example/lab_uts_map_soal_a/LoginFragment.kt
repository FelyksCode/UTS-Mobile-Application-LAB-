package com.example.lab_uts_map_soal_a

import android.content.Intent
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
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        view.findViewById<Button>(R.id.login_button).setOnClickListener {
            val email = view.findViewById<EditText>(R.id.email).text.toString()
            val password = view.findViewById<EditText>(R.id.password).text.toString()
            signInWithEmail(email, password)
        }

        view.findViewById<Button>(R.id.google_sign_in_button).setOnClickListener {
            signInWithGoogle()
        }

        view.findViewById<Button>(R.id.sign_up_button).setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                replace(R.id.main, SignUpFragment())
                addToBackStack(null)
            }
        }

        return view
    }

    private fun signInWithEmail(email: String, password: String) {
        if (email.isEmpty()) {
            Toast.makeText(context, "Email is required.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(context, "Password is required.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, navigate to HomePageFragment and show bottom navigation bar
                    val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
                    bottomNavigationView.visibility = View.VISIBLE
                    bottomNavigationView.selectedItemId = R.id.page_2 // Set home page as active
                    requireActivity().supportFragmentManager.commit {
                        replace(R.id.main, HomeFragment())
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(context, "Login Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(context, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, navigate to HomePageFragment and show bottom navigation bar
                    val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
                    bottomNavigationView.visibility = View.VISIBLE
                    bottomNavigationView.selectedItemId = R.id.page_2 // Set home page as active
                    requireActivity().supportFragmentManager.commit {
                        replace(R.id.main, HomeFragment())
                    }
                } else {
                    Toast.makeText(context, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}