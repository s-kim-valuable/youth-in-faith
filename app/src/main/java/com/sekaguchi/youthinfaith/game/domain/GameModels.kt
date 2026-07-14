package com.sekaguchi.youthinfaith.game.domain

import androidx.compose.ui.graphics.Color

val TEAM_COLORS = listOf(
    Color(0xFFE53935),
    Color(0xFF1E88E5),
    Color(0xFF43A047),
    Color(0xFFFB8C00),
    Color(0xFF8E24AA),
    Color(0xFF00ACC1),
)

fun defaultTeams() = listOf(
    TeamConfig("team1", "레드팀", TEAM_COLORS[0]),
    TeamConfig("team2", "블루팀", TEAM_COLORS[1]),
    TeamConfig("team3", "그린팀", TEAM_COLORS[2]),
)

data class TeamConfig(val id: String, val displayName: String, val color: Color)

data class GameConfig(val teams: List<TeamConfig> = defaultTeams())

data class TeamState(val input: String = "", val status: String = "typing")

data class GameState(val correctAnswer: String = "", val isRevealed: Boolean = false)
