package com.aurabeat.app.navigation

sealed class Screen(val route: String) {
    data object Splash     : Screen("splash")
    data object Home       : Screen("home")
    data object Search     : Screen("search")
    data object Library    : Screen("library")
    data object Settings   : Screen("settings")
    data object NowPlaying : Screen("nowplaying")
    data object Queue      : Screen("queue")
    data object Lyrics     : Screen("lyrics")
    data object Playlist   : Screen("playlist/{id}") { fun of(id: String) = "playlist/$id" }
    data object Album      : Screen("album/{id}")    { fun of(id: String) = "album/$id" }
    data object Artist     : Screen("artist/{id}")   { fun of(id: String) = "artist/$id" }
    data object Favorites  : Screen("favorites")
    data object Device     : Screen("device")
}

val bottomBarRoutes = setOf(
    Screen.Home.route,
    Screen.Search.route,
    Screen.Library.route,
    Screen.Settings.route,
)
