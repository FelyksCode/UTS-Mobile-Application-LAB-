package com.example.lab_uts_map_soal_a

data class Story(
    var id: String = "",
    var uid: String = "",
    var storyText: String = "",
    var imageUrl: String? = null,
    var timestamp: Long = 0,
    var likesCount: Int = 0,
    var isBookmarked: Boolean = false
)