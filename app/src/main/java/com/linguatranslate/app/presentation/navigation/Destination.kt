package com.linguatranslate.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Destination("home", "Home", Icons.Default.Home)
    data object History : Destination("history", "History", Icons.Default.History)
    data object Favorites : Destination("favorites", "Favorites", Icons.Default.Star)
    data object Conversation : Destination("conversation", "Conversation", Icons.Default.Forum)
    data object Settings : Destination("settings", "Settings", Icons.Default.Settings)

    companion object {
        val bottomNavItems = listOf(Home, History, Favorites, Conversation, Settings)
    }
}
