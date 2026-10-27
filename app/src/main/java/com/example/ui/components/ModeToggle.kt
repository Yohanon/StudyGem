package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiMode

@Composable
fun ModeToggle(
    currentMode: AiMode,
    onModeSelected: (AiMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Learn Mode Card
                val isLearn = currentMode == AiMode.LEARN
                val learnBg by animateColorAsState(
                    targetValue = if (isLearn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    label = "learnBg"
                )
                val learnFg = if (isLearn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                Surface(
                    selected = isLearn,
                    onClick = { onModeSelected(AiMode.LEARN) },
                    shape = RoundedCornerShape(10.dp),
                    color = learnBg,
                    contentColor = learnFg,
                    tonalElevation = if (isLearn) 4.dp else 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mode_learn_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Learn Mode",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isLearn) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = "Concept breakdown",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Demonstrate Mode Card
                val isDemo = currentMode == AiMode.DEMONSTRATE
                val demoBg by animateColorAsState(
                    targetValue = if (isDemo) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                    label = "demoBg"
                )
                val demoFg = if (isDemo) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface

                Surface(
                    selected = isDemo,
                    onClick = { onModeSelected(AiMode.DEMONSTRATE) },
                    shape = RoundedCornerShape(10.dp),
                    color = demoBg,
                    contentColor = demoFg,
                    tonalElevation = if (isDemo) 4.dp else 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mode_demonstrate_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Construction,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Demonstrate Mode",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isDemo) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = "Practical execution",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Description of active mode
            Text(
                text = currentMode.fullDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}
