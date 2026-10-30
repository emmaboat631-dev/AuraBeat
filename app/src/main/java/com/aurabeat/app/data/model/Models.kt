package com.aurabeat.app.data.model

import androidx.compose.ui.graphics.Color

data class Song(
    val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val albumId: String,
    val albumName: String,
    val durationMs: Long,
    val artworkGradient: List<Color>,
    val mediaUri: String? = null,
    val genre: String = "Electronic",
)

data class Artist(
    val id: String,
    val name: String,
    val followers: String,
    val gradient: List<Color>,
)

data class Album(
    val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val year: Int,
    val trackIds: List<String>,
    val gradient: List<Color>,
)

data class Playlist(
    val id: String,
    val title: String,
    val curator: String,
    val trackIds: List<String>,
    val gradient: List<Color>,
)

data class Genre(
    val id: String,
    val name: String,
    val gradient: List<Color>,
)
