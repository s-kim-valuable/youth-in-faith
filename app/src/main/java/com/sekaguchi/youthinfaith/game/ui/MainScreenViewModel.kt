package com.sekaguchi.youthinfaith.game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sekaguchi.youthinfaith.game.data.GameRepository
import com.sekaguchi.youthinfaith.game.domain.GameConfig
import com.sekaguchi.youthinfaith.game.domain.GameState
import com.sekaguchi.youthinfaith.game.domain.TeamState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainScreenUiState(
    val config: GameConfig = GameConfig(),
    val gameState: GameState = GameState(),
    val teams: Map<String, TeamState> = emptyMap(),
    val timerSeconds: Int = 30,
    val isTimerRunning: Boolean = false,
    val wrongCounts: Map<String, Int> = emptyMap(),
    val successTeams: Set<String> = emptySet()
)

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                repository.configFlow(),
                repository.gameStateFlow(),
                repository.teamsFlow()
            ) { config, gameState, teams ->
                val (wrongCounts, successTeams) = if (gameState.isRevealed) {
                    calculateResults(teams, gameState.correctAnswer)
                } else {
                    emptyMap<String, Int>() to emptySet()
                }
                Triple(config, gameState to teams, wrongCounts to successTeams)
            }.collect { (config, gameAndTeams, results) ->
                val (gameState, teams) = gameAndTeams
                _uiState.update { current ->
                    current.copy(
                        config = config,
                        gameState = gameState,
                        teams = teams,
                        wrongCounts = results.first,
                        successTeams = results.second
                    )
                }
            }
        }
    }

    fun onTimerTap() {
        if (_uiState.value.isTimerRunning) resetTimer() else startTimer()
    }

    private fun startTimer() {
        _uiState.update { it.copy(isTimerRunning = true) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.timerSeconds > 0) {
                delay(1000L)
                _uiState.update { it.copy(timerSeconds = it.timerSeconds - 1) }
            }
            _uiState.update { it.copy(isTimerRunning = false) }
        }
    }

    private fun resetTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(timerSeconds = 30, isTimerRunning = false) }
    }

    fun revealAnswer() {
        viewModelScope.launch { repository.revealAnswer() }
    }

    fun resetGame() {
        resetTimer()
        val teamIds = _uiState.value.config.teams.map { it.id }
        viewModelScope.launch { repository.resetGame(teamIds) }
    }

    private fun calculateResults(
        teams: Map<String, TeamState>,
        correctAnswer: String
    ): Pair<Map<String, Int>, Set<String>> {
        val wrongCounts = teams.mapValues { (_, state) ->
            val matchCount = state.input.zip(correctAnswer).count { (a, b) -> a == b }
            correctAnswer.length - matchCount
        }
        val successTeams = wrongCounts.filter { (_, count) -> count == 0 }.keys.toSet()
        return wrongCounts to successTeams
    }
}
