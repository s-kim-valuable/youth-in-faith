package com.sekaguchi.youthinfaith.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Serializable
data object DetailRoute : NavKey

@Serializable
data object GameLobbyRoute : NavKey

@Serializable
data class TeamClientRoute(val team: String) : NavKey

@Serializable
data object MainScreenRoute : NavKey

@Serializable
data object GameSetupRoute : NavKey
