package com.aurabeat.app.data

import androidx.compose.ui.graphics.Color
import com.aurabeat.app.data.model.Album
import com.aurabeat.app.data.model.Artist
import com.aurabeat.app.data.model.Genre
import com.aurabeat.app.data.model.Playlist
import com.aurabeat.app.data.model.Song

object MockCatalog {

    private fun g(vararg hex: Long): List<Color> = hex.map { Color(it) }

    // Local files bundled in app/src/main/res/raw/. The extension is dropped —
    // Android resolves `raw/<name>` regardless of the underlying .mp3/.m4a.
    private fun raw(name: String) = "android.resource://com.aurabeat.app/raw/$name"

    val artists = listOf(
        Artist("a1", "Shallipopi",        "3.2M followers", g(0xFF7C3AED, 0xFF1E1B4B)),
        Artist("a2", "Amapiano Republic", "1.8M followers", g(0xFFFF7A00, 0xFF3B0764)),
        Artist("a3", "Kabza Sessions",    "2.4M followers", g(0xFFA855F7, 0xFF0F172A)),
        Artist("a4", "Late Drive Co.",    "612K followers", g(0xFF06B6D4, 0xFF1E1B4B)),
    )

    val albums = listOf(
        Album("al1", "Cast",             "a1", "Shallipopi",        2024, listOf("s1","s2","s3","s4"),                                    g(0xFF7C3AED, 0xFF09070D)),
        Album("al2", "Amapiano Republic","a2", "Amapiano Republic", 2024, listOf("s5","s6","s7","s8","s9","s10","s11"),                   g(0xFFFF7A00, 0xFF09070D)),
        Album("al3", "Kabza Sessions",   "a3", "Kabza Sessions",    2024, listOf("s12","s13","s14","s15","s16","s17"),                    g(0xFFA855F7, 0xFF09070D)),
        Album("al4", "Late Drive",       "a4", "Late Drive Co.",    2024, listOf("s18","s19","s20","s21","s22","s23","s24","s25","s26"), g(0xFF06B6D4, 0xFF09070D)),
    )

    // Durations are rough hints for the UI — ExoPlayer reports the real value
    // once each file loads. Values below are estimates from file size.
    val songs = listOf(
        // Cast — Shallipopi & Friends
        Song("s1",  "Cast",                    "a1", "Shallipopi",        "al1", "Cast",              218_000, g(0xFF7C3AED, 0xFF1E1B4B), mediaUri = raw("shallipopi_cast")),
        Song("s2",  "A Collision of Two Worlds","a1","Shallipopi",        "al1", "Cast",              720_000, g(0xFFA855F7, 0xFF0F172A), mediaUri = raw("a_collision_of_two_worlds")),
        Song("s3",  "Shu",                     "a1", "Diamond Platnumz",  "al1", "Cast",              440_000, g(0xFFEC4899, 0xFF3B0764), mediaUri = raw("diamond_platnumz_shu")),
        Song("s4",  "Abracadabra (Remix)",     "a1", "Shallipopi",        "al1", "Cast",              320_000, g(0xFF8B5CF6, 0xFF0B0714), mediaUri = raw("abracadabra_remix")),

        // Amapiano Republic
        Song("s5",  "Tshwala Bam",             "a2", "Amapiano Republic", "al2", "Amapiano Republic", 660_000, g(0xFFFF7A00, 0xFF3B0764), mediaUri = raw("tshwala_bam")),
        Song("s6",  "Tshwala Bam (Remix)",     "a2", "Amapiano Republic", "al2", "Amapiano Republic", 200_000, g(0xFFF59E0B, 0xFF3B0764), mediaUri = raw("tshwala_bam_remix")),
        Song("s7",  "Mnike",                   "a2", "Amapiano Republic", "al2", "Amapiano Republic", 680_000, g(0xFF8B5CF6, 0xFF0B0714), mediaUri = raw("mnike")),
        Song("s8",  "Yahyuppiyah",             "a2", "Amapiano Republic", "al2", "Amapiano Republic", 500_000, g(0xFFA855F7, 0xFF0F172A), mediaUri = raw("yahyuppiyah")),
        Song("s9",  "Turn Off the Lights",     "a2", "Amapiano Republic", "al2", "Amapiano Republic", 840_000, g(0xFFEC4899, 0xFF3B0764), mediaUri = raw("turn_off_the_lights")),
        Song("s10", "Piki Piki",               "a2", "Amapiano Republic", "al2", "Amapiano Republic", 470_000, g(0xFFFF7A00, 0xFF3B0764), mediaUri = raw("piki_piki")),
        Song("s11", "Amapiano Breakfast",      "a2", "Amapiano Republic", "al2", "Amapiano Republic", 170_000, g(0xFFF59E0B, 0xFF3B0764), mediaUri = raw("amapiano_breakfast")),

        // Kabza Sessions
        Song("s12", "Bhebha",                  "a3", "Kabza Sessions",    "al3", "Kabza Sessions",    580_000, g(0xFFA855F7, 0xFF0F172A), mediaUri = raw("bhebha")),
        Song("s13", "Champion Sound",          "a3", "Kabza Sessions",    "al3", "Kabza Sessions",    410_000, g(0xFF7C3AED, 0xFF09070D), mediaUri = raw("champion_sound")),
        Song("s14", "Yebo",                    "a3", "Kabza Sessions",    "al3", "Kabza Sessions",    440_000, g(0xFF8B5CF6, 0xFF0B0714), mediaUri = raw("yebo")),
        Song("s15", "Forgive Our Trespasses",  "a3", "Kabza Sessions",    "al3", "Kabza Sessions",    320_000, g(0xFFEC4899, 0xFF3B0764), mediaUri = raw("forgive_our_trespasses")),
        Song("s16", "Jealousy",                "a3", "Kabza Sessions",    "al3", "Kabza Sessions",    295_000, g(0xFFA855F7, 0xFF0F172A), mediaUri = raw("jealousy")),
        Song("s17", "Hmmm",                    "a3", "Kabza Sessions",    "al3", "Kabza Sessions",    305_000, g(0xFF06B6D4, 0xFF1E1B4B), mediaUri = raw("hmmm")),

        // Late Drive Collective
        Song("s18", "Drift",                   "a4", "Late Drive Co.",    "al4", "Late Drive",        280_000, g(0xFF06B6D4, 0xFF1E1B4B), mediaUri = raw("drift")),
        Song("s19", "Drift (Remix)",           "a4", "Late Drive Co.",    "al4", "Late Drive",        345_000, g(0xFF7C3AED, 0xFF09070D), mediaUri = raw("drift_remix")),
        Song("s20", "Dalie",                   "a4", "Late Drive Co.",    "al4", "Late Drive",        475_000, g(0xFFEC4899, 0xFF3B0764), mediaUri = raw("dalie")),
        Song("s21", "Daliwonga",               "a4", "Late Drive Co.",    "al4", "Late Drive",        165_000, g(0xFFA855F7, 0xFF0F172A), mediaUri = raw("daliwonga")),
        Song("s22", "Camera Man",              "a4", "Late Drive Co.",    "al4", "Late Drive",        205_000, g(0xFFF59E0B, 0xFF3B0764), mediaUri = raw("camera_man")),
        Song("s23", "La Maluka",               "a4", "Late Drive Co.",    "al4", "Late Drive",        290_000, g(0xFFFF7A00, 0xFF3B0764), mediaUri = raw("la_maluka")),
        Song("s24", "Ka Valungu",              "a4", "Tebza De DJ",       "al4", "Late Drive",        310_000, g(0xFF8B5CF6, 0xFF0B0714), mediaUri = raw("tebza_de_dj_ka_valungu")),
        Song("s25", "The Therapist",           "a4", "Late Drive Co.",    "al4", "Late Drive",        180_000, g(0xFF06B6D4, 0xFF1E1B4B), mediaUri = raw("the_therapist")),
        Song("s26", "Tyeja",                   "a4", "Late Drive Co.",    "al4", "Late Drive",        295_000, g(0xFF7C3AED, 0xFF09070D), mediaUri = raw("tyeja")),
    )

    val genres = listOf(
        Genre("g1", "Amapiano",   g(0xFF7C3AED, 0xFF1E1B4B)),
        Genre("g2", "Afrobeats",  g(0xFFFF7A00, 0xFF3B0764)),
        Genre("g3", "House",      g(0xFFA855F7, 0xFF0F172A)),
        Genre("g4", "Chill",      g(0xFF06B6D4, 0xFF1E1B4B)),
        Genre("g5", "Late Night", g(0xFFEC4899, 0xFF3B0764)),
        Genre("g6", "Sessions",   g(0xFF8B5CF6, 0xFF0B0714)),
    )

    val playlists = listOf(
        Playlist("p1", "Late Night Drive",   "Curated by AuraBeat", listOf("s18","s19","s25","s9","s24","s20"),        g(0xFF7C3AED, 0xFF09070D)),
        Playlist("p2", "Amapiano Heat",      "Curated by AuraBeat", listOf("s5","s7","s10","s8","s12","s14","s16"),    g(0xFFFF7A00, 0xFF09070D)),
        Playlist("p3", "Party Starters",     "Curated by AuraBeat", listOf("s1","s3","s4","s6","s11","s22","s21"),     g(0xFFEC4899, 0xFF09070D)),
        Playlist("p4", "After Hours",        "Curated by AuraBeat", listOf("s2","s13","s15","s17","s23","s26"),        g(0xFFA855F7, 0xFF09070D)),
    )

    val demoLyrics = """
        In the neon dark I hear your heart
        Pulsing through the wires and stars
        Every beat a signal
        Every silence a scar

        Hold the light, hold the line
        In this quiet, we are fine
        Every note a memory
        Every memory a sign
    """.trimIndent()

    fun songById(id: String): Song? = songs.firstOrNull { it.id == id }
    fun artistById(id: String): Artist? = artists.firstOrNull { it.id == id }
    fun albumById(id: String): Album? = albums.firstOrNull { it.id == id }
    fun playlistById(id: String): Playlist? = playlists.firstOrNull { it.id == id }
    fun songsIn(ids: List<String>): List<Song> = ids.mapNotNull { songById(it) }
}
