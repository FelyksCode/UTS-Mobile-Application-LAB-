package com.example.lab_uts_map_soal_a

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ProfilePageFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var profileImageView: ImageView
    private var selectedImageUri: Uri? = null
    private var currentImageUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_page, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        profileImageView = view.findViewById(R.id.profile_image)

        view.findViewById<Button>(R.id.select_image_button).setOnClickListener {
            selectImage()
        }

        view.findViewById<Button>(R.id.save_button).setOnClickListener {
            val name = view.findViewById<EditText>(R.id.name).text.toString()
            val studentId = view.findViewById<EditText>(R.id.student_id).text.toString()
            saveUserData(name, studentId)
        }

        view.findViewById<Button>(R.id.logout_button).setOnClickListener {
            auth.signOut()
            val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNavigationView.visibility = View.GONE
            requireActivity().supportFragmentManager.commit {
                replace(R.id.main, LoginFragment())
            }
        }

        loadUserData()

        return view
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            profileImageView.setImageURI(selectedImageUri)
        }
    }

    private fun saveUserData(name: String, studentId: String) {
        val user = auth.currentUser
        if (user != null) {
            if (selectedImageUri != null && selectedImageUri.toString() != currentImageUrl) {
                // Delete the old image
                currentImageUrl?.let {
                    val oldImageRef = storage.getReferenceFromUrl(it)
                    oldImageRef.delete()
                }

                // Upload the new image
                val ref = storage.reference.child("profile_images/${UUID.randomUUID()}")
                ref.putFile(selectedImageUri!!)
                    .addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener { uri ->
                            saveToFirestore(user.uid, name, studentId, uri.toString())
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                saveToFirestore(user.uid, name, studentId, currentImageUrl)
            }
        }
    }

    private fun saveToFirestore(uid: String, name: String, studentId: String, imageUrl: String?) {
        val userData = hashMapOf(
            "name" to name,
            "studentId" to studentId,
            "imageUrl" to imageUrl
        )
        db.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(context, "Data saved successfully", Toast.LENGTH_SHORT).show()
                currentImageUrl = imageUrl
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val name = document.getString("name")
                        val studentId = document.getString("studentId")
                        currentImageUrl = document.getString("imageUrl")

                        view?.findViewById<EditText>(R.id.name)?.setText(name)
                        view?.findViewById<EditText>(R.id.student_id)?.setText(studentId)

                        if (!currentImageUrl.isNullOrEmpty()) {
                            Glide.with(this).load(currentImageUrl).into(profileImageView)
                        } else {
                            profileImageView.setImageResource(R.drawable.profile_ic)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}