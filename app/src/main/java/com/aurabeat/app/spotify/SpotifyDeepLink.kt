package com.aurabeat.app.spotify

import android.content.Context
import android.content.Intent
import android.net.Uri

/** Opens a Spotify track/artist/album in the Spotify app when it's installed,
 *  falling back to open.spotify.com in the browser. */
object SpotifyDeepLink {
    fun openTrack(context: Context, trackId: String) {
        open(context, Uri.parse("spotify:track:$trackId"), Uri.parse("https://open.spotify.com/track/$trackId"))
    }

    private fun open(context: Context, appUri: Uri, webUri: Uri) {
        val appIntent = Intent(Intent.ACTION_VIEW, appUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val hasSpotify = context.packageManager.resolveActivity(appIntent, 0) != null
        val chosen = if (hasSpotify) appIntent
        else Intent(Intent.ACTION_VIEW, webUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chosen)
    }
}
