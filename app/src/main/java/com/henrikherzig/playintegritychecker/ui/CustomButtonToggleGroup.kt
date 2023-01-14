package com.henrikherzig.playintegritychecker.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex


/**
 * Custom MaterialButtonToggleGroup
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ToggleGroup(
    selectedIndex: String,
    items: List<List<String>>,
    indexChanged: (String) -> Unit,
    height: Dp
) {
    val cornerRadius = 8.dp
    Row {
        items.forEachIndexed { index, item ->
            CompositionLocalProvider(
                LocalMinimumTouchTargetEnforcement provides false,
            ) {
                OutlinedButton(
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    modifier = when (index) {
                        0 ->
                            Modifier
                                .offset(0.dp, 0.dp)
                                .zIndex(if (selectedIndex == item[0]) 1f else 0f)
                                .fillMaxWidth().weight(1f)
                                .height(height)
                        else ->
                            Modifier
                                .offset((-1 * index).dp, 0.dp)
                                .zIndex(if (selectedIndex == item[0]) 1f else 0f)
                                .fillMaxWidth().weight(1f)
                                .height(height)
                    },
                    onClick = {
                        indexChanged(item[0])
                        //selectedIndex = index
                    },
                    shape = when (index) {
                        // left outer button
                        0 -> RoundedCornerShape(
                            topStart = cornerRadius,
                            topEnd = 0.dp,
                            bottomStart = cornerRadius,
                            bottomEnd = 0.dp
                        )
                        // right outer button
                        items.size - 1 -> RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = cornerRadius,
                            bottomStart = 0.dp,
                            bottomEnd = cornerRadius
                        )
                        // middle button
                        else -> RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    },
                    border = BorderStroke(
                        1.dp, if (selectedIndex == item[0]) {
                            MaterialTheme.colors.primary
                        } else {
                            Color.DarkGray.copy(alpha = 0.75f)
                        }
                    ),
                    colors = if (selectedIndex == item[0]) {
                        // selected colors
                        ButtonDefaults.outlinedButtonColors(
                            backgroundColor = MaterialTheme.colors.primary.copy(
                                alpha = 0.1f
                            ), contentColor = MaterialTheme.colors.primary
                        )
                    } else {
                        // not selected colors
                        ButtonDefaults.outlinedButtonColors(
                            backgroundColor = MaterialTheme.colors.surface,
                            contentColor = MaterialTheme.colors.primary
                        )
                    },
                ) {
                    Text(
                        text = item[1],
                        color = if (selectedIndex == items[index][0]) {
                            MaterialTheme.colors.primary
                        } else {
                            Color.DarkGray.copy(alpha = 0.9f)
                        },
                    )
                }
            }
        }
    }
}