package com.sekaguchi.youthinfaith.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sekaguchi.youthinfaith.game.domain.TEAM_COLORS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSetupScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: GameSetupViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("팀 설정") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        val focusManager = LocalFocusManager.current

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
                Text("팀 수 (2~6)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { viewModel.setTeamCount(uiState.teamCount - 1) },
                        enabled = uiState.teamCount > 2
                    ) { Text("-", fontSize = 20.sp) }
                    Text(
                        text = "${uiState.teamCount}팀",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    OutlinedButton(
                        onClick = { viewModel.setTeamCount(uiState.teamCount + 1) },
                        enabled = uiState.teamCount < 6
                    ) { Text("+", fontSize = 20.sp) }
                }
                Spacer(Modifier.height(32.dp))
                Text("팀 이름", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
            }

            itemsIndexed(uiState.teamNames) { index, name ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(TEAM_COLORS[index])
                    )
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.setTeamName(index, it) },
                        modifier = Modifier.weight(1f),
                        label = { Text("${index + 1}번 팀") },
                        singleLine = true
                    )
                }
            }

            item {
                Spacer(Modifier.height(32.dp))
                Text("정답", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.correctAnswer,
                    onValueChange = viewModel::setCorrectAnswer,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("정답 가사를 입력하세요") },
                    singleLine = true
                )
                Spacer(Modifier.height(32.dp))
                Text("호스트 PIN", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.hostPin,
                    onValueChange = { if (it.length <= 8) viewModel.setHostPin(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("PIN (숫자, 최대 8자리)") },
                    placeholder = { Text("미설정 시 기본값 1234") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.save(onDone = onBack) },
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (uiState.isSaving) "저장 중..." else "저장",
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
