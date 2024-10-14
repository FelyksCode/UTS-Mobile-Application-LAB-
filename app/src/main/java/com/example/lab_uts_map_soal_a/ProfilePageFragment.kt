package com.example.lab_uts_map_soal_a

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfilePageFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var bookmarksRecyclerView: RecyclerView
    private lateinit var bookmarksAdapter: StoriesAdapter
    private val bookmarksList = mutableListOf<Story>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_page, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        bookmarksRecyclerView = view.findViewById(R.id.bookmarks_recycler_view)
        bookmarksRecyclerView.layoutManager = LinearLayoutManager(context)
        bookmarksAdapter = StoriesAdapter(bookmarksList)
        bookmarksRecyclerView.adapter = bookmarksAdapter

        loadBookmarkedStories()

        return view
    }

    private fun loadBookmarkedStories() {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).collection("bookmarks").get()
                .addOnSuccessListener { documents ->
                    bookmarksList.clear()
                    for (document in documents) {
                        val storyId = document.id
                        db.collection("stories").document(storyId).get()
                            .addOnSuccessListener { storyDoc ->
                                val story = storyDoc.toObject(Story::class.java)
                                if (story != null) {
                                    story.id = storyDoc.id
                                    bookmarksList.add(story)
                                    bookmarksAdapter.notifyDataSetChanged()
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error loading bookmarks: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}