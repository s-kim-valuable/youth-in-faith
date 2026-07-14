package com.sekaguchi.youthinfaith.game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sekaguchi.youthinfaith.game.data.GameRepository
import com.sekaguchi.youthinfaith.game.domain.TeamConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamClientUiState(
    val inputText: String = "",
    val isSubmitted: Boolean = false
)

@HiltViewModel
class TeamClientViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamClientUiState())
    val uiState: StateFlow<TeamClientUiState> = _uiState.asStateFlow()

    private val teamIdFlow = MutableStateFlow("")
    private val inputFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val teamConfig: StateFlow<TeamConfig?> = teamIdFlow
        .flatMapLatest { id ->
            if (id.isEmpty()) flowOf(null)
            else repository.configFlow().map { config -> config.teams.find { it.id == id } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            @Suppress("OPT_IN_USAGE")
            inputFlow.debounce(500L).collect { input ->
                val id = teamIdFlow.value
                if (id.isNotEmpty()) repository.updateTeamInput(id, input)
            }
        }

        // 메인 스크린에서 리셋 시 로컬 상태 초기화
        viewModelScope.launch {
            @OptIn(ExperimentalCoroutinesApi::class)
            teamIdFlow
                .flatMapLatest { id ->
                    if (id.isEmpty()) flowOf(null)
                    else repository.teamsFlow().map { it[id] }
                }
                .collect { teamState ->
                    val local = _uiState.value
                    val shouldReset = teamState?.status == "typing" &&
                            teamState.input.isEmpty() &&
                            (local.isSubmitted || local.inputText.isNotEmpty())
                    if (shouldReset) _uiState.value = TeamClientUiState()
                }
        }
    }

    fun initializeTeam(team: String) {
        if (teamIdFlow.value.isNotEmpty()) return
        teamIdFlow.value = team
    }

    fun onInputChanged(text: String) {
        if (_uiState.value.isSubmitted) return
        _uiState.update { it.copy(inputText = text) }
        inputFlow.tryEmit(text)
    }

    fun submit() {
        val id = teamIdFlow.value
        if (_uiState.value.isSubmitted || id.isEmpty()) return
        _uiState.update { it.copy(isSubmitted = true) }
        viewModelScope.launch { repository.submitTeam(id) }
    }
}
