package com.example.lab_uts_map_soal_a

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        storiesAdapter = StoriesAdapter(storiesList)
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

    inner class StoriesAdapter(private val stories: List<Story>) :
        RecyclerView.Adapter<StoriesAdapter.StoryViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
            return StoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
            val story = stories[position]
            holder.bind(story)
        }

        override fun getItemCount(): Int = stories.size

        inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val storyImageView: ImageView = itemView.findViewById(R.id.story_image)
            private val storyTextView: TextView = itemView.findViewById(R.id.story_text)
            private val likeButton: Button = itemView.findViewById(R.id.like_button)
            private val likesCountTextView: TextView = itemView.findViewById(R.id.likes_count)

            fun bind(story: Story) {
                if (story.imageUrl.isNullOrEmpty()) {
                    storyImageView.visibility = View.GONE
                } else {
                    storyImageView.visibility = View.VISIBLE
                    Glide.with(itemView.context).load(story.imageUrl).into(storyImageView)
                }
                storyTextView.text = story.storyText
                likesCountTextView.text = "Likes: ${story.likesCount}"

                likeButton.setOnClickListener {
                    likeStory(story)
                }
            }
        }
    }

    private fun likeStory(story: Story) {
        val user = auth.currentUser
        if (user != null) {
            val likesRef = db.collection("stories").document(story.id).collection("likes").document(user.uid)
            likesRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        likesRef.delete()
                            .addOnSuccessListener {
                                story.likesCount -= 1
                                storiesAdapter.notifyDataSetChanged()
                                Toast.makeText(context, "Unliked", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        likesRef.set(mapOf("liked" to true))
                            .addOnSuccessListener {
                                story.likesCount += 1
                                storiesAdapter.notifyDataSetChanged()
                                Toast.makeText(context, "Liked", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        }
    }
}