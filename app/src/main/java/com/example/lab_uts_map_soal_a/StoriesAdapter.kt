package com.example.lab_uts_map_soal_a

import android.content.res.ColorStateList
import android.graphics.Color
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
class StoriesAdapter(
    private val storiesList: MutableList<Story>,
    private val onBookmarkRemoved: () -> Unit
) : RecyclerView.Adapter<StoriesAdapter.StoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(storiesList[position])
    }

    override fun getItemCount(): Int = storiesList.size

    fun removeStory(position: Int) {
        storiesList.removeAt(position)
        notifyItemRemoved(position)
    }

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
            likesCountTextView.text = "${story.likesCount}"

            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val storyRef = FirebaseFirestore.getInstance().collection("stories").document(story.id)
                storyRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val likedBy = document.get("likedBy") as? List<String> ?: emptyList()
                            if (likedBy.contains(user.uid)) {
                                likeButton.isSelected = true
                                likeButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF4D4D"))
                            } else {
                                likeButton.isSelected = false
                                likeButton.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(itemView.context, "Error retrieving story: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                val bookmarksRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .collection("bookmarks").document(story.id)
                bookmarksRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            bookmarkButton.setBackgroundResource(R.drawable.bookmark_full_ic)
                        } else {
                            bookmarkButton.setBackgroundResource(R.drawable.bookmark_outline_ic)
                        }
                    }
            }

            likeButton.setOnClickListener {
                likeStory(story)
            }

            bookmarkButton.setOnClickListener {
                bookmarkStory(story, adapterPosition)
            }
        }

        private fun likeStory(story: Story) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val storyRef =
                    FirebaseFirestore.getInstance().collection("stories").document(story.id)
                storyRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val likedBy = document.get("likedBy") as? List<String> ?: emptyList()
                            val currentLikes = document.getLong("likesCount") ?: 0
                            if (likedBy.contains(user.uid)) {
                                // Unlike the story
                                val newLikes = currentLikes - 1
                                storyRef.update(
                                    mapOf(
                                        "likesCount" to newLikes,
                                        "likedBy" to likedBy - user.uid
                                    )
                                )
                                    .addOnSuccessListener {
                                        likesCountTextView.text = "$newLikes"
                                        likeButton.isSelected = false
                                        likeButton.backgroundTintList =
                                            ColorStateList.valueOf(Color.BLACK)
                                        Toast.makeText(
                                            itemView.context,
                                            "Unliked",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            itemView.context,
                                            "Error unliking story: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                // Like the story
                                val newLikes = currentLikes + 1
                                storyRef.update(
                                    mapOf(
                                        "likesCount" to newLikes,
                                        "likedBy" to likedBy + user.uid
                                    )
                                )
                                    .addOnSuccessListener {
                                        likesCountTextView.text = "$newLikes"
                                        likeButton.isSelected = true
                                        likeButton.backgroundTintList =
                                            ColorStateList.valueOf(Color.parseColor("#FF4D4D"))
                                        Toast.makeText(
                                            itemView.context,
                                            "Liked",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            itemView.context,
                                            "Error liking story: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            itemView.context,
                            "Error retrieving story: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }

        private fun bookmarkStory(story: Story, position: Int) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val bookmarksRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .collection("bookmarks").document(story.id)
                bookmarksRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // Remove bookmark
                            bookmarksRef.delete()
                                .addOnSuccessListener {
                                    Toast.makeText(itemView.context, "Bookmark removed", Toast.LENGTH_SHORT).show()
                                    bookmarkButton.setBackgroundResource(R.drawable.bookmark_outline_ic)
                                    removeStory(position)
                                    onBookmarkRemoved()
                                }
                        } else {
                            // Add bookmark
                            bookmarksRef.set(mapOf("bookmarked" to true))
                                .addOnSuccessListener {
                                    Toast.makeText(itemView.context, "Bookmarked", Toast.LENGTH_SHORT).show()
                                    bookmarkButton.setBackgroundResource(R.drawable.bookmark_full_ic)
                                }
                        }
                    }
            }
        }
    }
}