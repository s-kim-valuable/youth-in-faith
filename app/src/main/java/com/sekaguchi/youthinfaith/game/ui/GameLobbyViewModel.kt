package com.sekaguchi.youthinfaith.game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sekaguchi.youthinfaith.game.data.GameRepository
import com.sekaguchi.youthinfaith.game.domain.GameConfig
import com.sekaguchi.youthinfaith.game.domain.HostAuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameLobbyViewModel @Inject constructor(
    private val repository: GameRepository,
    private val hostAuthState: HostAuthState
) : ViewModel() {

    val config: StateFlow<GameConfig?> = repository.configFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isHostUnlocked: StateFlow<Boolean> = hostAuthState.isUnlocked

    private val _showPinDialog = MutableStateFlow(false)
    val showPinDialog: StateFlow<Boolean> = _showPinDialog.asStateFlow()

    private val _pinError = MutableStateFlow(false)
    val pinError: StateFlow<Boolean> = _pinError.asStateFlow()

    fun openPinDialog() {
        _pinError.value = false
        _showPinDialog.value = true
    }

    fun dismissPinDialog() {
        _showPinDialog.value = false
        _pinError.value = false
    }

    fun tryUnlock(pin: String) {
        viewModelScope.launch {
            val correct = repository.getHostPin()
            if (pin == correct) {
                hostAuthState.unlock()
                _showPinDialog.value = false
                _pinError.value = false
            } else {
                _pinError.value = true
            }
        }
    }
}
