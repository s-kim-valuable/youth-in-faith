package com.sekaguchi.youthinfaith.game.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sekaguchi.youthinfaith.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamClientScreen(
    team: String,
    modifier: Modifier = Modifier
) {
    val viewModel: TeamClientViewModel = hiltViewModel(key = team)
    val uiState by viewModel.uiState.collectAsState()
    val teamConfig by viewModel.teamConfig.collectAsState()
    val focusManager = LocalFocusManager.current

    // ゲーム中の誤タップでロビーに戻らないよう back を無効化する。
    // 抜けたい場合はホスト側の「リセット」から復帰する運用を想定。
    BackHandler(enabled = true) {}

    LaunchedEffect(team) {
        viewModel.initializeTeam(team)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = teamConfig?.displayName ?: team,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = teamConfig?.color ?: Color.Gray
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = viewModel::onInputChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = { Text(stringResource(R.string.game_input_hint)) },
                    enabled = !uiState.isSubmitted
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = viewModel::submit,
                    enabled = !uiState.isSubmitted,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (uiState.isSubmitted) stringResource(R.string.game_submitted)
                               else stringResource(R.string.game_submit),
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
