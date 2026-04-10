// SettingsDialog.kt (完整修改后)
package com.example.linkgame.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsDialog(
    bgmEnabled: Boolean,
    soundEnabled: Boolean,
    currentBgmType: String,
    onDismiss: () -> Unit,
    onConfirm: (bgm: Boolean, sound: Boolean, bgmType: String) -> Unit
) {
    var bgm by remember { mutableStateOf(bgmEnabled) }
    var sound by remember { mutableStateOf(soundEnabled) }
    var bgmType by remember { mutableStateOf(currentBgmType) }

    val bgmOptions = listOf(
        "classic" to "经典",
        "beat" to "节奏",
        "stimulate" to "刺激",
        "china" to "古风"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置") },
        text = {
            Column {
                // 背景音乐开关
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("背景音乐", modifier = Modifier.weight(1f))
                    Switch(checked = bgm, onCheckedChange = { bgm = it })
                }
                Spacer(modifier = Modifier.height(8.dp))

                // 背景音乐类型选择（仅在背景音乐开启时显示）
                if (bgm) {
                    Text("选择音乐风格", modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                    bgmOptions.forEach { (value, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = bgmType == value,
                                onClick = { bgmType = value }
                            )
                            Text(text = label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 音效开关
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("音效（点击/消除）", modifier = Modifier.weight(1f))
                    Switch(checked = sound, onCheckedChange = { sound = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(bgm, sound, bgmType) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}