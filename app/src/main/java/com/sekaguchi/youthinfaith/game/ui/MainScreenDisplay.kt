package com.sekaguchi.youthinfaith.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sekaguchi.youthinfaith.game.domain.GameState
import com.sekaguchi.youthinfaith.game.domain.TeamConfig
import com.sekaguchi.youthinfaith.game.domain.TeamState

@Composable
fun MainScreenDisplay(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: MainScreenViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)) {
        // 상단 바: 타이머 + 공개 상태
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF212121))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "나가기",
                    tint = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .clickable { viewModel.onTimerTap() }
                    .background(
                        if (uiState.isTimerRunning) Color(0xFFB71C1C) else Color(0xFF424242)
                    )
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${uiState.timerSeconds}",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = if (uiState.gameState.isRevealed) "   ✓ 공개됨" else "   ⏳ 미공개",
                fontSize = 22.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        // 팀 행 레이아웃 (각 행: [팀 이름] | [입력 텍스트])
        Column(modifier = Modifier.weight(1f)) {
            uiState.config.teams.forEachIndexed { index, teamConfig ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.Gray.copy(alpha = 0.4f))
                    )
                }
                TeamRow(
                    teamConfig = teamConfig,
                    teamState = uiState.teams[teamConfig.id] ?: TeamState(),
                    gameState = uiState.gameState,
                    wrongCount = uiState.wrongCounts[teamConfig.id],
                    isSuccess = teamConfig.id in uiState.successTeams,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
        }

        // 하단 바: 리셋 버튼(왼쪽) + 정답 공개 버튼(오른쪽)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF212121))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .alpha(0.6f)
                    .clickable { viewModel.resetGame() }
                    .padding(12.dp)
            ) {
                Text("↺ 리셋", fontSize = 16.sp, color = Color.White)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .alpha(0.25f)
                    .clickable { viewModel.revealAnswer() }
                    .padding(12.dp)
            ) {
                Text("●", fontSize = 20.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun TeamRow(
    teamConfig: TeamConfig,
    teamState: TeamState,
    gameState: GameState,
    wrongCount: Int?,
    isSuccess: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Row(modifier = Modifier.fillMaxSize()) {
            // 팀 이름 (왼쪽, 팀 색상 배경)
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .background(teamConfig.color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = teamConfig.displayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // 세로 구분선
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color.Gray.copy(alpha = 0.4f))
            )

            // 입력 텍스트 (중앙)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (teamState.status == "submitted" && !gameState.isRevealed)
                            teamConfig.color.copy(alpha = 0.08f)
                        else Color.Transparent
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (gameState.isRevealed) {
                    Text(
                        text = buildComparisonAnnotatedString(teamState.input, gameState.correctAnswer),
                        fontSize = 26.sp
                    )
                } else {
                    Text(
                        text = teamState.input,
                        fontSize = 26.sp
                    )
                }
            }

            // 틀린 개수 표시 (정답 공개 후)
            if (wrongCount != null) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color.Gray.copy(alpha = 0.4f))
                )
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .fillMaxHeight()
                        .background(
                            if (isSuccess) Color(0xFF00C853).copy(alpha = 0.15f)
                            else Color(0xFFD50000).copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSuccess) "✓ 정답!" else "✗ ${wrongCount}개 틀림",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSuccess) Color(0xFF00C853) else Color(0xFFD50000)
                    )
                }
            }
        }

        // 성공! 오버레이 애니메이션
        AnimatedVisibility(
            visible = isSuccess,
            enter = fadeIn(animationSpec = tween(500)) +
                    scaleIn(initialScale = 0.5f, animationSpec = tween(500))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(teamConfig.color.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "성공!🎉",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

private fun buildComparisonAnnotatedString(input: String, answer: String): AnnotatedString =
    buildAnnotatedString {
        input.forEachIndexed { index, char ->
            val isCorrect = index < answer.length && char == answer[index]
            withStyle(
                SpanStyle(
                    color = if (isCorrect) Color(0xFF00C853) else Color(0xFFD50000),
                    fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal
                )
            ) {
                append(char)
            }
        }
    }
