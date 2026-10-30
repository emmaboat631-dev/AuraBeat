package com.aurabeat.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aurabeat.app.player.AuraBeatPlayer
import com.aurabeat.app.presentation.PlayerViewModel
import com.aurabeat.app.presentation.album.AlbumScreen
import com.aurabeat.app.presentation.artist.ArtistScreen
import com.aurabeat.app.presentation.device.DeviceLibraryScreen
import com.aurabeat.app.presentation.favorites.FavoritesScreen
import com.aurabeat.app.presentation.home.HomeScreen
import com.aurabeat.app.presentation.library.LibraryScreen
import com.aurabeat.app.presentation.lyrics.LyricsScreen
import com.aurabeat.app.presentation.nowplaying.NowPlayingScreen
import com.aurabeat.app.presentation.playlist.PlaylistScreen
import com.aurabeat.app.presentation.queue.QueueScreen
import com.aurabeat.app.presentation.search.SearchScreen
import com.aurabeat.app.presentation.settings.SettingsScreen
import com.aurabeat.app.presentation.splash.SplashScreen
import com.aurabeat.app.ui.components.AuraBottomNavigation
import com.aurabeat.app.ui.components.MiniPlayer
import com.aurabeat.app.ui.theme.AuraBackground

@Composable
fun AppNavigation(nav: NavHostController = rememberNavController()) {
    val playerVm: PlayerViewModel = hiltViewModel()
    val state by playerVm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { playerVm.initialize() }

    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomNav = currentRoute in bottomBarRoutes
    // Mini player follows the music: any screen that isn't Splash or Now Playing itself.
    val showMiniPlayer = currentRoute != null &&
        currentRoute != Screen.Splash.route &&
        currentRoute != Screen.NowPlaying.route

    Box(Modifier.fillMaxSize().background(AuraBackground)) {
        NavHost(
            navController = nav,
            startDestination = Screen.Splash.route,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(
                Screen.Splash.route,
                exitTransition = { fadeOut(tween(400)) },
            ) {
                SplashScreen(onFinished = {
                    nav.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(
                Screen.Home.route,
                enterTransition = { fadeIn(tween(500)) + scaleIn(initialScale = 1.04f, animationSpec = tween(500)) },
            ) { HomeScreen(nav, playerVm) }
            composable(Screen.Search.route)   { SearchScreen(nav, playerVm) }
            composable(Screen.Library.route)  { LibraryScreen(nav, playerVm) }
            composable(Screen.Settings.route) { SettingsScreen(nav) }
            composable(Screen.NowPlaying.route) { NowPlayingScreen(nav, playerVm) }
            composable(Screen.Queue.route)    { QueueScreen(nav, playerVm) }
            composable(Screen.Lyrics.route)   { LyricsScreen(nav, playerVm) }
            composable(Screen.Favorites.route) { FavoritesScreen(nav, playerVm) }
            composable(Screen.Device.route)    { DeviceLibraryScreen(nav, playerVm) }
            composable(Screen.Playlist.route) { entry ->
                PlaylistScreen(entry.arguments?.getString("id").orEmpty(), nav, playerVm)
            }
            composable(Screen.Album.route) { entry ->
                AlbumScreen(entry.arguments?.getString("id").orEmpty(), nav, playerVm)
            }
            composable(Screen.Artist.route) { entry ->
                ArtistScreen(entry.arguments?.getString("id").orEmpty(), nav, playerVm)
            }
        }

        Column(Modifier.fillMaxSize()) {
            androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
            if (showMiniPlayer) {
                com.aurabeat.app.presentation.spotify.SpotifyMiniBar()
            }
            AnimatedVisibility(
                visible = showMiniPlayer && state.currentSong != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                MiniPlayer(
                    state = state,
                    onTogglePlay = playerVm::togglePlay,
                    onClick = { nav.navigate(Screen.NowPlaying.route) },
                    onNext = playerVm::next,
                    onPrevious = playerVm::previous,
                )
            }
            AnimatedVisibility(visible = showBottomNav) {
                AuraBottomNavigation(currentRoute = currentRoute, onNavigate = { route ->
                    if (route != currentRoute) {
                        nav.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                })
            }
        }
    }
}
