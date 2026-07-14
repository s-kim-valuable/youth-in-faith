package com.sekaguchi.youthinfaith.game.data

import com.sekaguchi.youthinfaith.game.domain.GameConfig
import com.sekaguchi.youthinfaith.game.domain.GameState
import com.sekaguchi.youthinfaith.game.domain.TeamState
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun configFlow(): Flow<GameConfig>
    fun gameStateFlow(): Flow<GameState>
    fun teamsFlow(): Flow<Map<String, TeamState>>
    suspend fun saveConfig(config: GameConfig)
    suspend fun updateTeamInput(team: String, input: String)
    suspend fun submitTeam(team: String)
    suspend fun setCorrectAnswer(answer: String)
    suspend fun revealAnswer()
    suspend fun resetGame(teamIds: List<String>)
    suspend fun getHostPin(): String
    suspend fun setHostPin(pin: String)
}
