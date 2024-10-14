package com.example.lab_uts_map_soal_a

import android.app.Activity
import android.app.ProgressDialog
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddStoryFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storyImageView: ImageView
    private lateinit var storyEditText: EditText
    private var selectedImageUri: Uri? = null
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_story, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        storyImageView = view.findViewById(R.id.story_image)
        storyEditText = view.findViewById(R.id.story_text)

        view.findViewById<Button>(R.id.select_image_button).setOnClickListener {
            selectImage()
        }

        view.findViewById<Button>(R.id.submit_button).setOnClickListener {
            showLoading()
            submitStory()
        }

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
            storyImageView.setImageURI(selectedImageUri)
        }
    }

    private fun showLoading() {
        progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Posting your story...")
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    private fun hideLoading() {
        if (::progressDialog.isInitialized && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    private fun submitStory() {
        val user = auth.currentUser
        val storyText = storyEditText.text.toString()

        if (user != null && storyText.isNotEmpty()) {
            if (selectedImageUri != null) {
                val ref = storage.reference.child("story_images/${UUID.randomUUID()}")
                ref.putFile(selectedImageUri!!)
                    .addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener { uri ->
                            saveToFirestore(user.uid, storyText, uri.toString())
                        }
                    }
                    .addOnFailureListener { e ->
                        hideLoading()
                        Toast.makeText(context, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                saveToFirestore(user.uid, storyText, null)
            }
        } else {
            hideLoading()
            Toast.makeText(context, "Please write a story", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToFirestore(uid: String, storyText: String, imageUrl: String?) {
        val storyData = hashMapOf(
            "uid" to uid,
            "storyText" to storyText,
            "imageUrl" to imageUrl,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("stories").add(storyData)
            .addOnSuccessListener {
                hideLoading()
                Toast.makeText(context, "Story posted successfully", Toast.LENGTH_SHORT).show()
                // Optionally, clear the form or navigate away
            }
            .addOnFailureListener { e ->
                hideLoading()
                Toast.makeText(context, "Error posting story: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}