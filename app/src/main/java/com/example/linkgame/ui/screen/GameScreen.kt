package com.example.linkgame.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.linkgame.game.engine.GameController
import com.example.linkgame.game.model.GameMode
import com.example.linkgame.game.model.ALL_LEVELS
import com.example.linkgame.ui.components.GameBoard
import com.example.linkgame.ui.components.NicknameDialog
import com.example.linkgame.ui.components.SaveScoreDialog
import com.example.linkgame.ui.components.ScoreBar
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    mode: GameMode,
    onReturnToStart: () -> Unit,
    key: String
) {
    val context = LocalContext.current
    val factory = remember(mode) {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return GameController(context, mode) as T
            }
        }
    }
    val viewModel: GameController = viewModel(key = key, factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(viewModel) {
        viewModel.setReturnCallback(onReturnToStart)
        onDispose {
            viewModel.setReturnCallback(null)
        }
    }

    BackHandler(enabled = true) {
        when {
            uiState.showExitGameDialog -> viewModel.dismissExitGameDialog()
            uiState.showSaveDialog -> viewModel.dismissSaveDialog()
            uiState.showNicknameForSave -> viewModel.dismissNicknameDialogOnly()
            else -> viewModel.showExitGameDialog()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.title) },
                actions = {
                    IconButton(onClick = { viewModel.showHint() }) {
                        Icon(Icons.Default.Lightbulb, contentDescription = "提示")
                    }
                    TextButton(onClick = { viewModel.exitGame() }) { Text("退出") }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 在 GameScreen.kt 的 Scaffold 内容中，将 ScoreBar 调用改为：
                ScoreBar(
                    score = uiState.score,
                    timeLeft = uiState.timeLeft,
                    totalTime = uiState.currentConfig.timeLimit,
                    remainingPairs = uiState.remainingPairs,   // 新增
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .shadow(8.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    GameBoard(
                        board = uiState.board,
                        selectedFirst = uiState.selectedFirst,
                        selectedSecond = uiState.selectedSecond,
                        pathCoords = uiState.pathCoords,
                        hintFirst = uiState.hintFirst,
                        hintSecond = uiState.hintSecond,
                        onTileClick = { r, c -> viewModel.onTileClick(r, c) },
                        modifier = Modifier
                    )
                }

                Spacer(Modifier.height(12.dp))

                if (uiState.levelCleared && !uiState.gameFinished) {
                    Button(
                        onClick = { viewModel.nextLevel() },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(if (mode is GameMode.Challenge && uiState.challengeIndex == ALL_LEVELS.size - 1) "查看成绩" else "下一关")
                    }
                }
            }
        }
    }

    if (uiState.showExitGameDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissExitGameDialog() },
            title = { Text("退出游戏") },
            text = { Text("是否保存本次游戏成绩？") },
            confirmButton = {
                TextButton(onClick = { viewModel.saveScoreAndReturn() }) {
                    Text("保存并返回")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.returnWithoutSaving() }) {
                    Text("直接返回")
                }
            }
        )
    }

    if (uiState.showSaveDialog) {
        SaveScoreDialog(
            score = uiState.score,
            timeSeconds = uiState.totalTimeSeconds,
            onSave = { viewModel.saveScoreAndReturn() },
            onDismiss = { viewModel.dismissSaveDialogAndReturn() }
        )
    }

    if (uiState.showNicknameForSave) {
        NicknameDialog(
            currentNickname = null,
            onDismiss = { viewModel.dismissNicknameDialogOnly() },
            onConfirm = { nickname ->
                viewModel.saveScoreWithNickname(nickname)
            }
        )
    }
}