package com.sekaguchi.youthinfaith.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.sekaguchi.youthinfaith.game.ui.GameLobbyScreen
import com.sekaguchi.youthinfaith.game.ui.GameSetupScreen
import com.sekaguchi.youthinfaith.game.ui.MainScreenDisplay
import com.sekaguchi.youthinfaith.game.ui.TeamClientScreen
import com.sekaguchi.youthinfaith.navigation.DetailRoute
import com.sekaguchi.youthinfaith.navigation.GameLobbyRoute
import com.sekaguchi.youthinfaith.navigation.GameSetupRoute
import com.sekaguchi.youthinfaith.navigation.HomeRoute
import com.sekaguchi.youthinfaith.navigation.MainScreenRoute
import com.sekaguchi.youthinfaith.navigation.TeamClientRoute
import com.sekaguchi.youthinfaith.ui.theme.YouthinfaithTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YouthinfaithTheme {
                YouthInFaithApp()
            }
        }
    }
}

@Composable
fun YouthInFaithApp() {
    val backStack = rememberNavBackStack(HomeRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryProvider = { key: NavKey ->
            when (key) {
                HomeRoute -> NavEntry(key) {
                    HomeScreen(
                        onNavigateToDetail = {
                            if (backStack.lastOrNull() != DetailRoute) backStack.add(DetailRoute)
                        },
                        onNavigateToGame = {
                            if (backStack.lastOrNull() != GameLobbyRoute) backStack.add(GameLobbyRoute)
                        }
                    )
                }
                DetailRoute -> NavEntry(key) {
                    DetailScreen(
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                GameLobbyRoute -> NavEntry(key) {
                    GameLobbyScreen(
                        onTeamSelected = { team ->
                            val route = TeamClientRoute(team)
                            if (backStack.lastOrNull() != route) backStack.add(route)
                        },
                        onMainScreen = {
                            if (backStack.lastOrNull() != MainScreenRoute) backStack.add(MainScreenRoute)
                        },
                        onSetup = {
                            if (backStack.lastOrNull() != GameSetupRoute) backStack.add(GameSetupRoute)
                        },
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                is TeamClientRoute -> NavEntry(key) {
                    TeamClientScreen(team = key.team)
                }
                MainScreenRoute -> NavEntry(key) {
                    MainScreenDisplay(
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                GameSetupRoute -> NavEntry(key) {
                    GameSetupScreen(
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                else -> error("Unknown route: $key")
            }
        }
    )
}
