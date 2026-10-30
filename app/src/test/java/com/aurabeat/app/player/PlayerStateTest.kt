package com.aurabeat.app.player

import androidx.compose.ui.graphics.Color
import com.aurabeat.app.data.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerStateTest {

    private fun s(id: String, duration: Long = 200_000) = Song(
        id = id, title = "T-$id", artistId = "a", artistName = "A",
        albumId = "al", albumName = "AL", durationMs = duration,
        artworkGradient = listOf(Color.Red, Color.Blue),
    )

    @Test fun `progress is zero when duration is zero`() {
        val state = PlayerState(durationMs = 0, positionMs = 100)
        assertEquals(0f, state.progress, 0.0001f)
    }

    @Test fun `progress is clamped to one`() {
        val state = PlayerState(durationMs = 100, positionMs = 500)
        assertEquals(1f, state.progress, 0.0001f)
    }

    @Test fun `progress is a fraction of duration`() {
        val state = PlayerState(durationMs = 100, positionMs = 25)
        assertEquals(0.25f, state.progress, 0.0001f)
    }

    @Test fun `hasNext true when index is before last`() {
        val state = PlayerState(queue = listOf(s("a"), s("b"), s("c")), queueIndex = 1)
        assertTrue(state.hasNext)
        assertTrue(state.hasPrevious)
    }

    @Test fun `hasNext false at last index`() {
        val state = PlayerState(queue = listOf(s("a"), s("b")), queueIndex = 1)
        assertFalse(state.hasNext)
        assertTrue(state.hasPrevious)
    }

    @Test fun `hasPrevious false at first index`() {
        val state = PlayerState(queue = listOf(s("a"), s("b")), queueIndex = 0)
        assertTrue(state.hasNext)
        assertFalse(state.hasPrevious)
    }

    @Test fun `both false when queue empty`() {
        val state = PlayerState()
        assertFalse(state.hasNext)
        assertFalse(state.hasPrevious)
    }
}
