package com.aurabeat.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MockCatalogTest {

    @Test fun `catalog has at least one song, artist, and album`() {
        assertTrue(MockCatalog.songs.isNotEmpty())
        assertTrue(MockCatalog.artists.isNotEmpty())
        assertTrue(MockCatalog.albums.isNotEmpty())
    }

    @Test fun `songById returns null for unknown id`() {
        assertNull(MockCatalog.songById("no-such-song"))
    }

    @Test fun `songById returns the right song for a known id`() {
        val first = MockCatalog.songs.first()
        val roundTrip = MockCatalog.songById(first.id)
        assertNotNull(roundTrip)
        assertEquals(first.title, roundTrip!!.title)
    }

    @Test fun `every album's track ids resolve to real songs`() {
        MockCatalog.albums.forEach { album ->
            album.trackIds.forEach { id ->
                assertNotNull("Album ${album.title} references missing song $id", MockCatalog.songById(id))
            }
        }
    }

    @Test fun `every song references a real artist and album`() {
        MockCatalog.songs.forEach { song ->
            assertNotNull("Song ${song.title} has missing artist ${song.artistId}", MockCatalog.artistById(song.artistId))
            assertNotNull("Song ${song.title} has missing album ${song.albumId}",  MockCatalog.albumById(song.albumId))
        }
    }

    @Test fun `every song has a non-blank media uri`() {
        MockCatalog.songs.forEach { song ->
            assertFalse("Song ${song.title} has no mediaUri", song.mediaUri.isNullOrBlank())
        }
    }

    @Test fun `every playlist references real songs`() {
        MockCatalog.playlists.forEach { p ->
            p.trackIds.forEach { id ->
                assertNotNull("Playlist ${p.title} references missing song $id", MockCatalog.songById(id))
            }
        }
    }

    @Test fun `songsIn preserves order and skips unknown ids`() {
        val known = MockCatalog.songs.take(2).map { it.id }
        val result = MockCatalog.songsIn(listOf(known[0], "unknown", known[1]))
        assertEquals(2, result.size)
        assertEquals(known[0], result[0].id)
        assertEquals(known[1], result[1].id)
    }
}
