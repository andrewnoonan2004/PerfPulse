package com.droid.perfpulse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.droid.perfpulse.ui.Format
import kotlin.math.roundToInt

/**
 * Expressive header card featuring an M3 shape-badged icon, title, headline value,
 * and circular wavy progress gauge.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveGaugeCard(
  title: String,
  subtitle: String,
  valuePercent: Float?,
  detail: String,
  icon: Painter,
  modifier: Modifier = Modifier,
  accentColor: Color = MaterialTheme.colorScheme.primary,
  containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
  shapeType: ExpressiveShapeType = ExpressiveShapeType.Cookie,
  onClick: (() -> Unit)? = null,
) {
  val animatedProgress by animateFloatAsState(
    targetValue = (valuePercent ?: 0f).coerceIn(0f, 100f) / 100f,
    animationSpec = tween(durationMillis = 400),
    label = "gauge_progress",
  )

  val badgeShape = when (shapeType) {
    ExpressiveShapeType.Cookie -> MaterialShapes.Cookie12Sided.toShape()
    ExpressiveShapeType.Sunny -> MaterialShapes.Sunny.toShape()
    ExpressiveShapeType.Clover -> MaterialShapes.Clover4Leaf.toShape()
    ExpressiveShapeType.Pill -> RoundedCornerShape(16.dp)
  }

  Card(
    modifier = modifier,
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    onClick = onClick ?: {},
    enabled = onClick != null,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f),
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(badgeShape)
              .background(accentColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              painter = icon,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(24.dp),
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column {
            Text(
              text = title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        // Circular Wavy Progress Indicator (M3 Expressive)
        Box(contentAlignment = Alignment.Center) {
          CircularWavyProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(56.dp),
            color = accentColor,
            trackColor = accentColor.copy(alpha = 0.18f),
          )
          Text(
            text = valuePercent?.let { "${it.roundToInt()}%" } ?: "N/A",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = detail,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
      }
    }
  }
}

enum class ExpressiveShapeType {
  Cookie, Sunny, Clover, Pill
}

/**
 * Real-time sparkline graph drawn with canvas, featuring smooth line and gradient fill.
 */
@Composable
fun SparklineChart(
  history: List<Float>,
  modifier: Modifier = Modifier,
  lineColor: Color = MaterialTheme.colorScheme.primary,
  fillColor: Color = lineColor.copy(alpha = 0.25f),
  maxExpected: Float = 100f,
  minExpected: Float = 0f,
  showGrid: Boolean = true,
) {
  val surfaceVariant = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(MaterialTheme.colorScheme.surfaceContainerLowest),
  ) {
    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 12.dp)) {
      val width = size.width
      val height = size.height

      if (showGrid) {
        // Draw 25%, 50%, 75% guide lines
        for (i in 1..3) {
          val y = height * (i / 4f)
          drawLine(
            color = surfaceVariant,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.dp.toPx(),
          )
        }
      }

      if (history.size < 2) return@Canvas

      val range = (maxExpected - minExpected).coerceAtLeast(1f)
      val stepX = width / (history.size - 1).coerceAtLeast(1)

      val path = Path()
      val fillPath = Path()

      history.forEachIndexed { index, value ->
        val clamped = value.coerceIn(minExpected, maxExpected)
        val normalized = (clamped - minExpected) / range
        val x = index * stepX
        val y = height - (normalized * height)

        if (index == 0) {
          path.moveTo(x, y)
          fillPath.moveTo(x, height)
          fillPath.lineTo(x, y)
        } else {
          val prevX = (index - 1) * stepX
          val prevClamped = history[index - 1].coerceIn(minExpected, maxExpected)
          val prevNormalized = (prevClamped - minExpected) / range
          val prevY = height - (prevNormalized * height)

          // Smooth cubic bezier curve between points
          val midX = (prevX + x) / 2f
          path.cubicTo(midX, prevY, midX, y, x, y)
          fillPath.cubicTo(midX, prevY, midX, y, x, y)
        }
      }

      fillPath.lineTo(width, height)
      fillPath.close()

      // Draw gradient under the curve
      drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
          colors = listOf(fillColor, Color.Transparent),
          startY = 0f,
          endY = height,
        ),
      )

      // Draw curve outline
      drawPath(
        path = path,
        color = lineColor,
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
      )

      // Draw pulse dot on the latest point
      val latestVal = history.last().coerceIn(minExpected, maxExpected)
      val latestNormalized = (latestVal - minExpected) / range
      val lastX = (history.size - 1) * stepX
      val lastY = height - (latestNormalized * height)
      drawCircle(
        color = lineColor,
        radius = 4.dp.toPx(),
        center = Offset(lastX, lastY),
      )
    }
  }
}

/**
 * Expressive horizontal bar with LinearWavyProgressIndicator.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveProgressBar(
  label: String,
  valueText: String,
  progress: Float?,
  modifier: Modifier = Modifier,
  color: Color = MaterialTheme.colorScheme.primary,
  trackColor: Color = color.copy(alpha = 0.16f),
) {
  val animatedProgress by animateFloatAsState(
    targetValue = (progress ?: 0f).coerceIn(0f, 100f) / 100f,
    animationSpec = tween(350),
    label = "progress_bar",
  )

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium,
      )
      Text(
        text = valueText,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        fontWeight = FontWeight.Bold,
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    LinearWavyProgressIndicator(
      progress = { animatedProgress },
      modifier = Modifier
        .fillMaxWidth()
        .height(10.dp),
      color = color,
      trackColor = trackColor,
    )
  }
}

/**
 * Key-value stat pill or tile for hardware specs and metadata.
 */
@Composable
fun StatTile(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
  secondaryValue: String? = null,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      if (secondaryValue != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = secondaryValue,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.outline,
        )
      }
    }
  }
}

/**
 * Status badge pill (e.g. Normal, Moderate, Low Memory, etc.)
 */
@Composable
fun StatusBadge(
  text: String,
  isAlert: Boolean,
  modifier: Modifier = Modifier,
) {
  val bg = if (isAlert) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
  val fg = if (isAlert) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(8.dp),
    color = bg,
  ) {
    Text(
      text = text,
      color = fg,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    )
  }
}
