package com.sekaguchi.youthinfaith.game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sekaguchi.youthinfaith.game.data.GameRepository
import com.sekaguchi.youthinfaith.game.domain.GameConfig
import com.sekaguchi.youthinfaith.game.domain.HostAuthState
import com.sekaguchi.youthinfaith.game.domain.TEAM_COLORS
import com.sekaguchi.youthinfaith.game.domain.TeamConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameSetupUiState(
    val teamCount: Int = 3,
    val teamNames: List<String> = listOf("레드팀", "블루팀", "그린팀"),
    val correctAnswer: String = "",
    val hostPin: String = "",
    val isSaving: Boolean = false
)

@HiltViewModel
class GameSetupViewModel @Inject constructor(
    private val repository: GameRepository,
    private val hostAuthState: HostAuthState
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameSetupUiState())
    val uiState: StateFlow<GameSetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val pin = repository.getHostPin()
            combine(
                repository.configFlow(),
                repository.gameStateFlow()
            ) { config, gameState -> config to gameState }.first().let { (config, gameState) ->
                _uiState.update {
                    GameSetupUiState(
                        teamCount = config.teams.size,
                        teamNames = config.teams.map { it.displayName },
                        correctAnswer = gameState.correctAnswer,
                        hostPin = pin
                    )
                }
            }
        }
    }

    fun setTeamCount(count: Int) {
        val clamped = count.coerceIn(2, 6)
        val current = _uiState.value.teamNames
        val newNames = List(clamped) { i -> current.getOrElse(i) { "${i + 1}팀" } }
        _uiState.update { it.copy(teamCount = clamped, teamNames = newNames) }
    }

    fun setCorrectAnswer(answer: String) {
        _uiState.update { it.copy(correctAnswer = answer) }
    }

    fun setHostPin(pin: String) {
        _uiState.update { it.copy(hostPin = pin) }
    }

    fun setTeamName(index: Int, name: String) {
        val newNames = _uiState.value.teamNames.toMutableList()
        if (index < newNames.size) newNames[index] = name
        _uiState.update { it.copy(teamNames = newNames) }
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val teams = _uiState.value.teamNames.mapIndexed { i, name ->
                TeamConfig("team${i + 1}", name, TEAM_COLORS[i])
            }
            repository.saveConfig(GameConfig(teams))
            repository.setCorrectAnswer(_uiState.value.correctAnswer)
            if (_uiState.value.hostPin.isNotBlank()) {
                repository.setHostPin(_uiState.value.hostPin)
            }
            _uiState.update { it.copy(isSaving = false) }
            hostAuthState.lock()
            onDone()
        }
    }
}
