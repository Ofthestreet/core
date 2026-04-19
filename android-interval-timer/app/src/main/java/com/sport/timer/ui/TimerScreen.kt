package com.sport.timer.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sport.timer.Phase
import com.sport.timer.TimerViewModel

@Composable
fun TimerScreen(viewModel: TimerViewModel) {
    val state by viewModel.state.collectAsState()

    val phaseColor by animateColorAsState(
        targetValue = when (state.phase) {
            Phase.PREP -> Color(0xFFFF9800)
            Phase.WORK -> Color(0xFF4CAF50)
            Phase.REST -> Color(0xFF2196F3)
            Phase.DONE -> Color(0xFF9C27B0)
            Phase.IDLE -> Color(0xFF555555)
        },
        animationSpec = tween(500),
        label = "phaseColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .systemBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "INTERVAL TIMER",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- 3 parameter pickers ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ParameterPicker(
                label = "TRAVAIL",
                value = formatTime(state.workTimeSeconds),
                onDecrement = { viewModel.setWorkTime(maxOf(10, state.workTimeSeconds - 20)) },
                onIncrement = { viewModel.setWorkTime(minOf(180, state.workTimeSeconds + 20)) },
                color = Color(0xFF4CAF50),
                enabled = state.phase == Phase.IDLE
            )
            ParameterPicker(
                label = "REPOS",
                value = formatTime(state.restTimeSeconds),
                onDecrement = { viewModel.setRestTime(maxOf(0, state.restTimeSeconds - 30)) },
                onIncrement = { viewModel.setRestTime(minOf(600, state.restTimeSeconds + 30)) },
                color = Color(0xFF2196F3),
                enabled = state.phase == Phase.IDLE
            )
            ParameterPicker(
                label = "REPS",
                value = state.repetitions.toString(),
                onDecrement = { viewModel.setRepetitions(maxOf(1, state.repetitions - 1)) },
                onIncrement = { viewModel.setRepetitions(minOf(99, state.repetitions + 1)) },
                color = Color(0xFFFF9800),
                enabled = state.phase == Phase.IDLE
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- Circular countdown ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(260.dp)
        ) {
            val totalSeconds = when (state.phase) {
                Phase.PREP -> 5f
                Phase.WORK -> state.workTimeSeconds.toFloat()
                Phase.REST -> state.restTimeSeconds.toFloat()
                else -> 1f
            }
            val progress by animateFloatAsState(
                targetValue = if (totalSeconds > 0f) state.currentSeconds / totalSeconds else 0f,
                animationSpec = tween(900),
                label = "progress"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 18.dp.toPx()
                val radius = size.minDimension / 2f - strokeWidth / 2f
                val topLeft = Offset(center.x - radius, center.y - radius)
                val arcSize = Size(radius * 2f, radius * 2f)

                drawArc(
                    color = Color(0xFF222222),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                if (progress > 0f) {
                    drawArc(
                        color = phaseColor,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when (state.phase) {
                        Phase.IDLE -> "PRÊT"
                        Phase.PREP -> "PRÉPARE-TOI"
                        Phase.WORK -> "TRAVAIL"
                        Phase.REST -> "REPOS"
                        Phase.DONE -> "TERMINÉ !"
                    },
                    color = phaseColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (state.phase) {
                        Phase.IDLE -> "--:--"
                        Phase.DONE -> "✓"
                        else -> formatTime(state.currentSeconds)
                    },
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                if (state.phase != Phase.IDLE && state.phase != Phase.DONE) {
                    Text(
                        text = "${state.currentRep} / ${state.repetitions}",
                        color = Color(0xFFAAAAAA),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- Control buttons ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset
            Button(
                onClick = { viewModel.reset() },
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A)),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("↺", fontSize = 28.sp, color = Color.White)
            }

            // Start / Pause (big)
            Button(
                onClick = { viewModel.toggleStartPause() },
                modifier = Modifier.size(96.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = phaseColor),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = if (state.isRunning) "⏸" else "▶",
                    fontSize = 40.sp,
                    color = Color.White
                )
            }

            // Spacer (symmetry)
            Spacer(modifier = Modifier.size(64.dp))
        }
    }
}

@Composable
fun ParameterPicker(
    label: String,
    value: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    color: Color,
    enabled: Boolean
) {
    val textColor = if (enabled) Color.White else Color(0xFF555555)
    val arrowColor = if (enabled) color else Color(0xFF333333)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1A1A1A))
            .padding(vertical = 12.dp, horizontal = 14.dp)
            .width(96.dp)
    ) {
        Text(
            text = label,
            color = color.copy(alpha = if (enabled) 1f else 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        IconButton(
            onClick = onIncrement,
            enabled = enabled,
            modifier = Modifier.size(44.dp)
        ) {
            Text("▲", color = arrowColor, fontSize = 22.sp, textAlign = TextAlign.Center)
        }
        Text(
            text = value,
            color = textColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        IconButton(
            onClick = onDecrement,
            enabled = enabled,
            modifier = Modifier.size(44.dp)
        ) {
            Text("▼", color = arrowColor, fontSize = 22.sp, textAlign = TextAlign.Center)
        }
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}
