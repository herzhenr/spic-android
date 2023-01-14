package com.henrikherzig.playintegritychecker.ui

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.henrikherzig.playintegritychecker.R

/**
 * opens a link in a preview window within the app
 */
fun openLink(url: String, context: Context) {
    CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
}

/**
 * shows device information in a card
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CustomButtonRow(context: Context, linkIdPairs: List<Pair<String, String>>) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        linkIdPairs.forEachIndexed { _, item ->
            CompositionLocalProvider(
                LocalMinimumTouchTargetEnforcement provides false,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    CustomButton(
                        { openLink(item.first, context) },
                        Icons.Outlined.OpenInNew,
                        item.second
                    )
                }
            }
        }
    }
}

/**
 * shows help card button
 */
@Composable
fun CustomHelpButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(ButtonDefaults.IconSize)
    ) {
        Icon(
            Icons.Outlined.Help,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
            tint = MaterialTheme.colors.primary
        )
    }
}


/**
 * shows close card button
 */
@Composable
fun CustomCloseButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(ButtonDefaults.IconSize)
    ) {
        Icon(
            Icons.Outlined.Close,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
            tint = MaterialTheme.colors.primary
        )
    }
}

/**
 * shows a Threema inspired three dot element indicating the verdict strength
 * [state] = [1,2,3] where 1 is low, 2 is medium and 3 is strong
 */
@Composable
fun CustomThreeStateIcons(state: Int, text: String) {
    val color = if (text == "MEETS_VIRTUAL_INTEGRITY") Color.Blue else when (state) {
        0 -> Color(0xFFE53935)
        1 -> Color(0xFFF57C00)
        2 -> Color(0xFFFFC107)
        3 -> Color(0xFF7CB342)
        else -> Color.Gray
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (state > 0) {
                Icons.Filled.Circle
            } else {
                Icons.Outlined.Circle
            },
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
            tint = color
        )
        Icon(
            if (state > 1) {
                Icons.Filled.Circle
            } else {
                Icons.Outlined.Circle
            },
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
            tint = color
        )
        Icon(
            if (state > 2) {
                Icons.Filled.Circle
            } else {
                Icons.Outlined.Circle
            },
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
            tint = color
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            color = color
        )
    }
}


data class HorizontalPagerContent(
    val threeState: Pair<Int, String>,
    val text: String,
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CustomSlideStack(initialPage: Int) {

    @Composable
    fun createItems() = listOf(
        HorizontalPagerContent(
            threeState = Pair(0,"NO_INTEGRITY"),
            text = stringResource(id = R.string.deviceRecognition_help_NO_INTEGRITY)
        ),
        HorizontalPagerContent(
            threeState = Pair(1,"MEETS_BASIC_INTEGRITY"),
            text = stringResource(id = R.string.deviceRecognition_help_MEETS_BASIC_INTEGRITY)
        ),
        HorizontalPagerContent(
            threeState = Pair(2,"MEETS_DEVICE_INTEGRITY"),
            text = stringResource(id = R.string.deviceRecognition_help_MEETS_DEVICE_INTEGRITY)
        ),
        HorizontalPagerContent(
            threeState = Pair(3,"MEETS_STRONG_INTEGRITY"),
            text = stringResource(id = R.string.deviceRecognition_help_MEETS_STRONG_INTEGRITY)
        ),
        HorizontalPagerContent(
            threeState = Pair(0,"MEETS_VIRTUAL_INTEGRITY"),
            text = stringResource(id = R.string.deviceRecognition_help_MEETS_VIRTUAL_INTEGRITY)
        )
    )

    val items = createItems()

    val pagerState = rememberPagerState(initialPage)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(count = items.size, state = pagerState) { page ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .fillMaxHeight()
                .padding(top = 60.dp, bottom = 30.dp)) {
                val item = items[currentPage]
                CustomThreeStateIcons(item.threeState.first, item.threeState.second)
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = item.text)
                //Spacer(modifier = Modifier.weight(1f))
            }
        }
        PageIndicator(
            numberOfPages = items.size,
            selectedPage = pagerState.currentPage,
            defaultRadius = 20.dp,
            selectedLength = 40.dp,
            space = 10.dp,
            animationDurationInMillis = 500,
        )
    }
}


@Composable
fun PageIndicator(
    numberOfPages: Int,
    modifier: Modifier = Modifier,
    selectedPage: Int = 0,
    selectedColor: Color = Color.Blue,
    defaultColor: Color = Color.LightGray,
    defaultRadius: Dp = 20.dp,
    selectedLength: Dp = 60.dp,
    space: Dp = 30.dp,
    animationDurationInMillis: Int = 300,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space),
        modifier = modifier,
    ) {
        for (i in 0 until numberOfPages) {
            val isSelected = i == selectedPage
            PageIndicatorView(
                isSelected = isSelected,
                selectedColor = selectedColor,
                defaultColor = defaultColor,
                defaultRadius = defaultRadius,
                selectedLength = selectedLength,
                animationDurationInMillis = animationDurationInMillis,
            )
        }
    }
}

@Composable
fun PageIndicatorView(
    isSelected: Boolean,
    selectedColor: Color,
    defaultColor: Color,
    defaultRadius: Dp,
    selectedLength: Dp,
    animationDurationInMillis: Int,
    modifier: Modifier = Modifier,
) {

    val color: Color by animateColorAsState(
        targetValue = if (isSelected) {
            selectedColor
        } else {
            defaultColor
        },
        animationSpec = tween(
            durationMillis = animationDurationInMillis,
        )
    )
    val width: Dp by animateDpAsState(
        targetValue = if (isSelected) {
            selectedLength
        } else {
            defaultRadius
        },
        animationSpec = tween(
            durationMillis = animationDurationInMillis,
        )
    )

    Canvas(
        modifier = modifier
            .size(
                width = width,
                height = defaultRadius,
            ),
    ) {
        drawRoundRect(
            color = color,
            topLeft = Offset.Zero,
            size = Size(
                width = width.toPx(),
                height = defaultRadius.toPx(),
            ),
            cornerRadius = CornerRadius(
                x = defaultRadius.toPx(),
                y = defaultRadius.toPx(),
            ),
        )
    }
}