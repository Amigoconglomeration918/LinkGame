// LeaderboardScreen.kt
package com.example.linkgame.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkgame.data.model.LeaderboardEntry
import com.example.linkgame.data.repository.LeaderboardRepository
import com.example.linkgame.game.model.ALL_LEVELS
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("挑战模式", "无尽模式")
    var endlessDifficultyFilter by remember { mutableStateOf<String?>(null) }
    val entries by LeaderboardRepository.getAllEntries(context).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf("") }
    // 新增：清空所有记录的确认对话框状态
    var showClearAllConfirm by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        onBack()
    }

    val filteredEntries = when (selectedTab) {
        0 -> entries.filter { it.mode == "challenge" }
        else -> entries.filter { it.mode == "endless" && (endlessDifficultyFilter == null || it.difficulty == endlessDifficultyFilter) }
    }.sortedWith(compareByDescending<LeaderboardEntry> { it.score }.thenBy { it.timeSeconds })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("排行榜") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    // 新增：清空所有记录按钮（仅当有记录时显示）
                    if (entries.isNotEmpty()) {
                        IconButton(onClick = { showClearAllConfirm = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "清空所有记录")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }

            if (selectedTab == 1) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = endlessDifficultyFilter == null,
                        onClick = { endlessDifficultyFilter = null },
                        label = { Text("全部") }
                    )
                    ALL_LEVELS.forEach { level ->
                        FilterChip(
                            selected = endlessDifficultyFilter == level.name,
                            onClick = { endlessDifficultyFilter = level.name },
                            label = { Text(level.name) }
                        )
                    }
                }
            }

            if (filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无记录", fontSize = 16.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(filteredEntries) { index, entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .shadow(2.dp, shape = MaterialTheme.shapes.medium),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${index + 1}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("昵称：${entry.nickname}", fontWeight = FontWeight.Bold)
                                    Text("得分：${entry.score}  |  用时：${entry.timeSeconds}秒")
                                    if (entry.difficulty != null) Text("难度：${entry.difficulty}")
                                    Text(
                                        "日期：${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(entry.date)}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                IconButton(onClick = {
                                    pendingDeleteId = entry.id
                                    showDeleteConfirm = true
                                }) {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 删除单条记录的确认对话框
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除记录") },
            text = { Text("确定要删除这条排行榜记录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        LeaderboardRepository.deleteEntry(context, pendingDeleteId)
                        showDeleteConfirm = false
                    }
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }

    // 新增：清空所有记录的确认对话框
    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text("清空所有记录") },
            text = { Text("确定要清空所有排行榜记录吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        LeaderboardRepository.clearAll(context)
                        showClearAllConfirm = false
                    }
                }) { Text("清空") }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) { Text("取消") }
            }
        )
    }
}