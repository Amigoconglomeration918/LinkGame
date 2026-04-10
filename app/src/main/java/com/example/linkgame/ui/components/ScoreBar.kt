package com.example.linkgame.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScoreBar(
    score: Int,
    timeLeft: Int,
    totalTime: Int,
    remainingPairs: Int,
    modifier: Modifier = Modifier
) {
    val isWarning = timeLeft <= 10

    // 字号动画：正常18.sp，警告22.sp（使用 Float 动画，然后 .sp）
    val targetFontSize = if (isWarning) 22f else 18f
    val animatedFontSize by animateFloatAsState(
        targetValue = targetFontSize,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fontSize"
    )

    // 脉冲缩放动画（仅在警告时循环）
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("得分: $score", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "剩余时间: ${timeLeft}s",
                    fontSize = animatedFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    modifier = if (isWarning) Modifier.scale(scale) else Modifier
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("剩余配对: $remainingPairs 对", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(0.dp))
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = timeLeft.toFloat() / totalTime,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (timeLeft < 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}