package com.example.linkgame.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkgame.game.model.Board
import com.example.linkgame.utils.colorForValue
import com.example.linkgame.utils.labelForValue

@Composable
fun GameBoard(
    board: Board,
    selectedFirst: Pair<Int, Int>?,
    selectedSecond: Pair<Int, Int>?,
    pathCoords: List<Pair<Int, Int>>?,
    hintFirst: Pair<Int, Int>?,
    hintSecond: Pair<Int, Int>?,
    onTileClick: (r: Int, c: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = board.rows
    val cols = board.cols
    val tileSize = 48.dp
    val displayRows = -1..rows
    val displayCols = -1..cols

    // 提示动画（无限缩放）
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(modifier = modifier) {
        for (r in displayRows) {
            Row {
                for (c in displayCols) {
                    val isInside = r in 0 until rows && c in 0 until cols
                    val v = if (isInside) board.cells[r][c] else 0
                    val isSelected = isInside && (
                            (selectedFirst?.first == r && selectedFirst?.second == c) ||
                                    (selectedSecond?.first == r && selectedSecond?.second == c)
                            )
                    val isPath = pathCoords?.any { it.first == r && it.second == c } == true
                    val isHint = isInside && (
                            (hintFirst?.first == r && hintFirst?.second == c) ||
                                    (hintSecond?.first == r && hintSecond?.second == c)
                            )
                    val backgroundColor = when {
                        isHint -> Color.Transparent  // 让金色边框更明显，背景不做额外覆盖
                        isPath && !isInside -> Color.Transparent
                        isPath -> Color.Cyan.copy(alpha = 0.8f)
                        isSelected -> Color.Yellow
                        !isInside -> Color(0xFFEEEEEE).copy(alpha = 0.0f)
                        else -> Color(0xFFCCCCCC)
                    }

                    Box(
                        modifier = Modifier
                            .size(tileSize)
                            .scale(if (isHint) scale else 1f)
                            .padding(4.dp)
                            .shadow(
                                elevation = if (isSelected) 8.dp else 2.dp,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(backgroundColor)
                            .then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)) else Modifier)
                            .then(if (isPath && !isInside) {
                                Modifier.drawBehind {
                                    val strokeWidth = 2.dp.toPx()
                                    val dashLength = 6.dp.toPx()
                                    val gapLength = 4.dp.toPx()
                                    drawRect(
                                        color = Color(0xFF6200EE),
                                        style = Stroke(
                                            width = strokeWidth,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
                                        ),
                                        topLeft = Offset.Zero,
                                        size = size
                                    )
                                }
                            } else Modifier)
                            .then(if (isHint) Modifier.border(4.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp)) else Modifier)
                            .clickable(enabled = isInside && v != 0) {
                                onTileClick(r, c)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isInside && v != 0) {
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colorForValue(v))
                                    .shadow(2.dp, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(labelForValue(v), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (!isInside && isPath) {
                            Box(Modifier.size(12.dp).background(Color(0xFF6200EE)).clip(CircleShape))
                        }
                    }
                }
            }
        }
    }
}