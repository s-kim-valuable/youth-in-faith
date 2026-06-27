package com.sekaguchi.youthinfaith.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.sekaguchi.youthinfaith.navigation.DetailRoute
import com.sekaguchi.youthinfaith.navigation.HomeRoute
import com.sekaguchi.youthinfaith.ui.DetailScreen
import com.sekaguchi.youthinfaith.ui.HomeScreen
import com.sekaguchi.youthinfaith.ui.theme.YouthinfaithTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YouthinfaithTheme {
                val backStack = rememberNavBackStack(HomeRoute)

                NavDisplay(
                    backStack = backStack,
                    onBack = { if (backStack.size > 1) backStack.removeLast() },
                    entryProvider = { key ->
                        when (key) {
                            HomeRoute -> NavEntry(key) {
                                HomeScreen(
                                    onNavigateToDetail = { backStack.add(DetailRoute) }
                                )
                            }
                            DetailRoute -> NavEntry(key) {
                                DetailScreen(
                                    onBack = { backStack.removeLast() }
                                )
                            }
                            else -> error("Unknown route: $key")
                        }
                    }
                )
            }
        }
    }
}
