package com.aurabeat.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.aurabeat.app.navigation.Screen
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraSpacing
import com.aurabeat.app.ui.theme.AuraSurfaceElev
import com.aurabeat.app.ui.theme.AuraTextMuted

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

private val items = listOf(
    NavItem(Screen.Home.route,       "Home",    Icons.Rounded.Home),
    NavItem(Screen.Search.route,     "Search",  Icons.Rounded.Search),
    NavItem(Screen.NowPlaying.route, "Player",  Icons.Rounded.Album),
    NavItem(Screen.Library.route,    "Library", Icons.Rounded.LibraryMusic),
    NavItem(Screen.Settings.route,   "Profile", Icons.Rounded.Person),
)

@Composable
fun AuraBottomNavigation(currentRoute: String?, onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm)
            .clip(RoundedCornerShape(28.dp))
            .background(AuraSurfaceElev)
            .padding(vertical = AuraSpacing.xs, horizontal = AuraSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            val color by animateColorAsState(if (selected) AuraPrimary else AuraTextMuted, label = "navColor")
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onNavigate(item.route) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.size(36.dp)
                        .clip(CircleShape)
                        .background(if (selected) AuraPrimary.copy(alpha = 0.15f) else Color.Transparent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(item.icon, contentDescription = item.label, tint = color, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(2.dp))
                Text(item.label, style = MaterialTheme.typography.labelSmall, color = color)
            }
        }
    }
}
