package com.example.linkgame.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkgame.audio.AudioManager
import com.example.linkgame.data.repository.NicknameRepository
import com.example.linkgame.data.repository.SettingsRepository
import com.example.linkgame.game.model.ALL_LEVELS
import com.example.linkgame.game.model.LevelConfig
import com.example.linkgame.ui.components.NicknameDialog
import com.example.linkgame.ui.components.SettingsDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    onStartChallenge: () -> Unit,
    onStartEndless: (LevelConfig) -> Unit,
    onShowLeaderboard: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showEndlessOptions by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf(ALL_LEVELS[0]) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showNicknameDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    var nickname by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        nickname = NicknameRepository.getNickname(context)
    }

    val bgmEnabled by SettingsRepository.isBgmEnabled(context).collectAsState(initial = true)
    val soundEnabled by SettingsRepository.isSoundEnabled(context).collectAsState(initial = true)
    val bgmType by SettingsRepository.getBgmType(context).collectAsState(initial = "classic")

    BackHandler(enabled = true) {
        when {
            showSettingsDialog -> showSettingsDialog = false
            showNicknameDialog -> showNicknameDialog = false
            showExitDialog -> showExitDialog = false
            else -> showExitDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("连连看") },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 欢迎卡片（位于最顶部）
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Games,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            "连连看",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "开始你的消除之旅",   // 修改后的副标题
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                // 与下方昵称胶囊保持间距
                // 昵称胶囊（简化显示，不占用独立卡片）
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "昵称：${nickname ?: "未设置"}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        TextButton(onClick = { showNicknameDialog = true }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(if (nickname == null) "设置昵称" else "修改昵称")
                        }
                    }
                }

                // 双卡片并列排布（挑战模式 + 无尽模式）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 挑战模式卡片
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(4.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "挑战模式",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "依次挑战4个难度关卡，全部通关即获胜",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = onStartChallenge,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(40.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, null)
                                Spacer(Modifier.width(6.dp))
                                Text("开始")
                            }
                        }
                    }

                    // 无尽模式卡片
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(4.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Autorenew,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "无尽模式",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "选择难度后不断挑战，挑战更高关卡",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { showEndlessOptions = !showEndlessOptions },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(40.dp)
                            ) {
                                Icon(
                                    if (showEndlessOptions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    null
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(if (showEndlessOptions) "收起" else "难度")
                            }
                        }
                    }
                }

                // 难度选择区域（可展开）
                if (showEndlessOptions) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                "选择难度",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ALL_LEVELS.forEach { level ->
                                    FilterChip(
                                        selected = selectedDifficulty == level,
                                        onClick = { selectedDifficulty = level },
                                        label = { Text(level.name) },
                                        shape = RoundedCornerShape(32.dp),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(20.dp))
                            Button(
                                onClick = { onStartEndless(selectedDifficulty) },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(Icons.Default.PlayArrow, null)
                                Spacer(Modifier.width(6.dp))
                                Text("开始无尽模式 (${selectedDifficulty.name})")
                            }
                        }
                    }
                }

                // 排行榜按钮
                Button(
                    onClick = onShowLeaderboard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(Icons.Default.Leaderboard, null)
                    Spacer(Modifier.width(8.dp))
                    Text("查看排行榜", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }

                // 退出按钮（次要）
                OutlinedButton(
                    onClick = { showExitDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, null)
                    Spacer(Modifier.width(8.dp))
                    Text("退出游戏", fontSize = 16.sp)
                }
            }
        }
    }

    // 以下对话框代码保持不变
    if (showSettingsDialog) {
        SettingsDialog(
            bgmEnabled = bgmEnabled,
            soundEnabled = soundEnabled,
            currentBgmType = bgmType,
            onDismiss = { showSettingsDialog = false },
            onConfirm = { newBgm, newSound, newBgmType ->
                scope.launch {
                    SettingsRepository.setBgmEnabled(context, newBgm)
                    SettingsRepository.setSoundEnabled(context, newSound)
                    SettingsRepository.setBgmType(context, newBgmType)
                }
                showSettingsDialog = false
            }
        )
    }

    if (showNicknameDialog) {
        NicknameDialog(
            currentNickname = nickname,
            onDismiss = { showNicknameDialog = false },
            onConfirm = { newNickname ->
                scope.launch {
                    NicknameRepository.saveNickname(context, newNickname)
                    nickname = newNickname
                }
                showNicknameDialog = false
            }
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("退出游戏") },
            text = { Text("确定要退出游戏吗？") },
            confirmButton = {
                TextButton(onClick = { onExit() }) {
                    Text("退出")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}