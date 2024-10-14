package com.example.lab_uts_map_soal_a

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StoriesAdapter(private val storiesList: List<Story>) : RecyclerView.Adapter<StoriesAdapter.StoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(storiesList[position])
    }

    override fun getItemCount(): Int = storiesList.size

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val storyImageView: ImageView = itemView.findViewById(R.id.story_image)
        private val storyTextView: TextView = itemView.findViewById(R.id.story_text)
        private val likeButton: Button = itemView.findViewById(R.id.like_button)
        private val bookmarkButton: Button = itemView.findViewById(R.id.bookmark_button)
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

            bookmarkButton.setOnClickListener {
                bookmarkStory(story)
            }
        }

        private fun likeStory(story: Story) {
            // Implement like functionality
        }

        private fun bookmarkStory(story: Story) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val bookmarksRef = FirebaseFirestore.getInstance().collection("users").document(user.uid).collection("bookmarks").document(story.id)
                bookmarksRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            bookmarksRef.delete()
                                .addOnSuccessListener {
                                    Toast.makeText(itemView.context, "Bookmark removed", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            bookmarksRef.set(mapOf("bookmarked" to true))
                                .addOnSuccessListener {
                                    Toast.makeText(itemView.context, "Bookmarked", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
            }
        }
    }
}