package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MiscSubCategory
import com.example.data.model.StudySection
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlueCoding
import com.example.ui.theme.EmeraldScience
import com.example.ui.theme.PurpleMath

@Composable
fun SubjectSelector(
    selectedSection: StudySection,
    selectedMisc: MiscSubCategory,
    onSectionSelected: (StudySection) -> Unit,
    onMiscSelected: (MiscSubCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Main 4 Subject Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StudySection.entries.forEach { section ->
                    val isSelected = selectedSection == section
                    val (icon, color) = when (section) {
                        StudySection.CODING -> Pair(Icons.Default.Code, BlueCoding)
                        StudySection.MATH -> Pair(Icons.Default.Calculate, PurpleMath)
                        StudySection.SCIENCE -> Pair(Icons.Default.Science, EmeraldScience)
                        StudySection.MISC -> Pair(Icons.Default.Category, AmberAccent)
                    }

                    Surface(
                        selected = isSelected,
                        onClick = { onSectionSelected(section) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) color else MaterialTheme.colorScheme.surface,
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        tonalElevation = if (isSelected) 6.dp else 1.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("section_${section.name.lowercase()}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = section.displayName,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = section.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Animated Sub-options for MISC
            AnimatedVisibility(
                visible = selectedSection == StudySection.MISC,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "MISC SUB-TOPICS:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MiscSubCategory.entries.forEach { subCat ->
                            val isSubSelected = selectedMisc == subCat
                            val subIcon: ImageVector = when (subCat) {
                                MiscSubCategory.RESEARCH -> Icons.Default.AutoStories
                                MiscSubCategory.HISTORY -> Icons.Default.HistoryEdu
                                MiscSubCategory.GEOGRAPHY -> Icons.Default.Public
                                MiscSubCategory.BUSINESS_STUDY -> Icons.Default.BusinessCenter
                                MiscSubCategory.NEWS -> Icons.Default.Newspaper
                                MiscSubCategory.ENGLISH -> Icons.Default.MenuBook
                                MiscSubCategory.OPEN_CONVERSATION -> Icons.Default.ChatBubbleOutline
                            }

                            FilterChip(
                                selected = isSubSelected,
                                onClick = { onMiscSelected(subCat) },
                                label = {
                                    Text(
                                        text = subCat.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = subIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AmberAccent,
                                    selectedLabelColor = Color.Black,
                                    selectedLeadingIconColor = Color.Black
                                ),
                                modifier = Modifier.testTag("misc_${subCat.name.lowercase()}")
                            )
                        }
                    }

                    Text(
                        text = selectedMisc.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
