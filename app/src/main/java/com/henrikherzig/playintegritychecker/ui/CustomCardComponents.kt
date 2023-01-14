package com.henrikherzig.playintegritychecker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.henrikherzig.playintegritychecker.R


/**
 * Custom Card Element
 * In this case, it is being used as a wrapper for the API result, loading screen or error screen
 */
@Composable
fun CustomCard(content: @Composable () -> Unit) {
    Card(
        //elevation = 0.dp,
        border = ButtonDefaults.outlinedBorder,
        modifier = Modifier
            .fillMaxWidth(),
        content = content,
        shape = RoundedCornerShape(10.dp),
        //backgroundColor = MaterialTheme.colors.primary
    )
}

/**
 * Title Element within the Card
 */
@Composable
fun CustomCardTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        modifier = Modifier.padding(bottom = 2.dp),
        color = MaterialTheme.colors.primary
    )
    Spacer(modifier = Modifier.height(6.dp))
}

/**
 * Title Element within the Card (one layer deeper than Title)
 */
@Composable
fun CustomCardTitle2(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        modifier = Modifier.padding(bottom = 2.dp),
        color = MaterialTheme.colors.primary
    )
    Spacer(modifier = Modifier.height(3.dp))
}

/**
 * Title Element within the Card (another layer deeper than title2)
 */
@Composable
fun CustomCardTitle3(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.caption,
        color = MaterialTheme.colors.primary
    )
}

/**
 * Creates 2 Text Elements on top of each other which resemble the title and the corresponding content
 * @param text1 the title of the group
 * @param text2 the content of the group
 */
@Composable
fun CustomCardGroup(text1: String, text2: String) {
    CustomCardTitle3(text1)
    //Spacer(modifier = Modifier.height(12.dp))
    Text(text = text2)
    Spacer(modifier = Modifier.height(12.dp))
}

/**
 * Similar to [CustomCardGroup] but the content is either true or false and is shown with a
 * icon next to it
 * @param text the title of the group
 * @param passed the content of the group (either passed or failed)
 */
@Composable
fun CustomCardBool(text: String, passed: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.caption,
        color = MaterialTheme.colors.primary
    )
    Spacer(modifier = Modifier.height(1.dp))

    val result =
        if (passed) stringResource(R.string.sn_passed) else stringResource(R.string.sn_failed)
    val color = if (passed) MaterialTheme.colors.primary else MaterialTheme.colors.error
    val icon = if (passed) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            tint = color,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = result,
            color = color
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
}

/**
 * Similar to [CustomCardGroup] but the content is either true, false or unevaluated and is shown
 * with a icon next to it
 * @param text the title of the group
 * @param passed the content of the group (either passed or failed)
 */
@Composable
fun CustomCardBoolHorizontal(text: String, passed: Boolean?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val color =
            if (passed == true) MaterialTheme.colors.primary else if (passed == false) MaterialTheme.colors.error else Color.Gray
        val icon = if (passed == true) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel

        Icon(
            imageVector = icon,
            tint = color,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = color
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

/**
 * Custom button element
 */
@Composable
fun CustomButton(onClick: () -> Unit, icon: ImageVector, text: String) {
    OutlinedButton(
        onClick = onClick,
        //shape = MaterialTheme.shapes.small,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        //enabled = enabled
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = text)
    }
}

/**
 * shows a alert dialog with a title on top and a ok and copy button in the bottom which has the
 * ability to copy the text to the clipboard of the device
 * @param titleString Title Text to be displayed
 * @param content Text which is being shown within the alert dialog body
 * @param opened controls weather the dialog is opened or not
 */
@Composable
fun CustomContentAlertDialog(
    modifier: Modifier = Modifier,
    titleString: String,
    content: @Composable (() -> Unit)? = null,
    opened: MutableState<Boolean>,
) {
    // val localClipboardManager = LocalClipboardManager.current
    CustomAlertDialog(
        modifier= modifier,
        titleString = titleString,
        titleIcon = Icons.Outlined.Help,
        opened = opened,
        content = content,
    )
}

/**
 * shows a alert dialog with a title on top and a ok and copy button in the bottom which has the
 * ability to copy the text to the clipboard of the device
 * @param content Text which is being shown within the alert dialog body
 * @param opened controls weather the dialog is opened or not
 */
@Composable
fun CustomCodeAlertDialog(
    content: String,
    opened: MutableState<Boolean>,
) {
    val localClipboardManager = LocalClipboardManager.current
    CustomAlertDialog(
        titleString = stringResource(id = R.string.dialog_title),
        titleIcon = Icons.Outlined.Code,
        opened = opened,
        content = {
            SelectionContainer {
                Text(
                    text = content,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { localClipboardManager.setText(AnnotatedString(content)) }) {
                Text(stringResource(id = android.R.string.copy))
            }
        },
    )
}


/**
 * shows a alert dialog with a title on top and a ok button in the bottom
 * @param titleString Title Text to be displayed
 * @param content Text which is being shown within the alert dialog body
 * @param opened controls weather the dialog is opened or not
 */
@Composable
fun CustomTextAlertDialog(
    titleString: String,
    content: String,
    opened: MutableState<Boolean>,
) {
    CustomAlertDialog(
        titleString = titleString,
        titleIcon = Icons.Outlined.Help,
        opened = opened,
        content = {
            SelectionContainer {
                Text(
                    text = content,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }
        },
    )
}

/**
 * custom alert dialog which is the foundation for other custom alert dialogs
 * @param titleString Title Text to be displayed
 * @param titleIcon Title icon to be displayed
 * @param content Content which is being shown within the alert dialog body
 * @param opened controls weather the dialog is opened or not
 */
@Composable
fun CustomAlertDialog(
    modifier: Modifier = Modifier,
    titleString: String,
    titleIcon: ImageVector,
    opened: MutableState<Boolean>,
    dismissButton: @Composable (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null,
) {
    AlertDialog(
        modifier = modifier
            .fillMaxWidth(),
            //.wrapContentSize(),
        title = {
            Row {
                Icon(
                    titleIcon,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = titleString)
            }
        },
        shape = RoundedCornerShape(20.dp),
        onDismissRequest = {
            opened.value = false
        },
        text = content,
        confirmButton = {
            TextButton(
                onClick = { opened.value = false }) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = dismissButton,
    )
}

/**
 * Preview ui elements in design preview of android studio
 * TODO: add more UI elements
 */

@Preview
@Composable
fun PassText() {
    Box {
        CustomCardBool("True", true)
    }
}

@Preview
@Composable
fun FailText() {
    Box {
        CustomCardBool("False", false)
    }
}