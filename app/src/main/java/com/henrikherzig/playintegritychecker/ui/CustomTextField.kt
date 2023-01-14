package com.henrikherzig.playintegritychecker.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * custom implementation of textField in order to get rid of unnecessary padding
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CustomTextField(
    value: String,
    modifier: Modifier = Modifier,
    hideKeyboard: Boolean = false,
    onFocusClear: () -> Unit = {},
    onSearch: (String) -> Unit = {},
    onValueChange: (String) -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    placeholder: @Composable (() -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        interactionSource = interactionSource,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            focusManager.clearFocus()
            onSearch(value)
        }),
        enabled = true,
        singleLine = true,
        textStyle = TextStyle(color = MaterialTheme.colors.primary)
    ) { innerTextField ->
        TextFieldDefaults.OutlinedTextFieldDecorationBox(
            value = value,
            innerTextField = innerTextField,
            singleLine = true,
            enabled = true,
            interactionSource = interactionSource,
            contentPadding = PaddingValues(horizontal = 12.dp),
            visualTransformation = VisualTransformation.None,
            placeholder = placeholder,
            border = {
                TextFieldDefaults.BorderBox(
                    shape = RoundedCornerShape(8.dp),
                    enabled = true,
                    isError = false,
                    colors = TextFieldDefaults.outlinedTextFieldColors(),
                    interactionSource = interactionSource,
                )
            }
        )
    }
    if (hideKeyboard) {
        focusManager.clearFocus()
        // Call onFocusClear to reset hideKeyboard state to false
        onFocusClear()
    }
}