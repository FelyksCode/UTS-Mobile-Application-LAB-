package com.example.lab_uts_map_soal_a

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storiesRecyclerView: RecyclerView
    private lateinit var storiesAdapter: StoriesAdapter
    private val storiesList = mutableListOf<Story>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        storiesRecyclerView = view.findViewById(R.id.stories_recycler_view)
        storiesRecyclerView.layoutManager = LinearLayoutManager(context)
        storiesAdapter = StoriesAdapter(storiesList, onBookmarkRemoved = {})
        storiesRecyclerView.adapter = storiesAdapter

        loadStories()

        return view
    }

    private fun loadStories() {
        db.collection("stories").get()
            .addOnSuccessListener { documents ->
                storiesList.clear()
                val tempStoriesList = mutableListOf<Story>()
                for (document in documents) {
                    val story = document.toObject(Story::class.java)
                    story.id = document.id
                    tempStoriesList.add(story)
                }
                loadLikesForStories(tempStoriesList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading stories: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadLikesForStories(stories: List<Story>) {
        val tasks = stories.map { story ->
            db.collection("stories").document(story.id).collection("likes").get()
                .addOnSuccessListener { likes ->
                    story.likesCount = likes.size()
                }
        }

        // Wait for all tasks to complete
        Tasks.whenAllSuccess<Void>(tasks).addOnSuccessListener {
            storiesList.addAll(stories)
            storiesAdapter.notifyDataSetChanged()
        }
    }
}