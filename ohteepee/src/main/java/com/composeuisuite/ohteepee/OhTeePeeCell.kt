package com.composeuisuite.ohteepee

import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composeuisuite.ohteepee.configuration.OhTeePeeConfigurations
import com.composeuisuite.ohteepee.configuration.cellBackground
import com.composeuisuite.ohteepee.utils.conditional

private val MIN_HEIGHT_CELL_SIZE = 48.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun OhTeePeeCell(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    configurations: OhTeePeeConfigurations,
    placeHolder: String,
    visualTransformation: VisualTransformation,
    isErrorOccurred: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val cellConfiguration by remember(
        key1 = value,
        key2 = isFocused,
        key3 = isErrorOccurred,
    ) {
        val config = getCellConfig(
            enabled = enabled,
            cellText = value,
            configurations = configurations,
            isErrorOccurred = isErrorOccurred,
            isFocused = isFocused,
        )
        mutableStateOf(config)
    }
    val textStyle = remember(cellConfiguration.textStyle) {
        cellConfiguration.textStyle.copy(textAlign = TextAlign.Center)
    }
    val placeHolderTextStyle = remember(cellConfiguration.placeHolderTextStyle) {
        cellConfiguration.placeHolderTextStyle.copy(textAlign = TextAlign.Center)
    }
    // Holds the latest internal TextFieldValue state. We need to keep it to have the correct value
    // of the composition.
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value)) }
    // Holds the latest TextFieldValue that BasicTextField was recomposed with. We couldn't simply
    // pass `TextFieldValue(text = value)` to the CoreTextField because we need to preserve the
    // composition.
    val textFieldValue = textFieldValueState.copy(text = value, selection = TextRange(value.length))

    SideEffect {
        if (textFieldValue.selection != textFieldValueState.selection ||
            textFieldValue.composition != textFieldValueState.composition
        ) {
            textFieldValueState = textFieldValue
        }
    }
    // Last String value that either text field was recomposed with or updated in the onValueChange
    // callback. We keep track of it to prevent calling onValueChange(String) for same String when
    // CoreTextField's onValueChange is called multiple times without recomposition in between.
    var lastTextValue by remember(value) { mutableStateOf(value) }
    val borderModifier = if (configurations.enableBottomLine) {
        Modifier.drawBehind {
            val y = size.height

            drawLine(
                color = cellConfiguration.borderColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = cellConfiguration.borderWidth.toPx(),
            )
        }
    } else {
        Modifier.border(
            border = BorderStroke(
                width = cellConfiguration.borderWidth,
                color = cellConfiguration.borderColor,
            ),
            shape = cellConfiguration.shape,
        )
    }

    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = MIN_HEIGHT_CELL_SIZE)
            .then(borderModifier),
        color = Color.Transparent,
        contentColor = Color.Unspecified,
        elevation = configurations.elevation,
        shape = if (configurations.enableBottomLine) RoundedCornerShape(0.dp) else cellConfiguration.shape,
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValueState = it

                val stringChangedSinceLastInvocation = lastTextValue != it.text
                lastTextValue = it.text

                if (stringChangedSinceLastInvocation) {
                    onValueChange(it.text)
                }
            },
            visualTransformation = visualTransformation,
            textStyle = textStyle,
            modifier = Modifier
                .conditional(value.isEmpty()) {
                    onPreviewKeyEvent {
                        val isDeleteKey = it.key == Key.Backspace || it.key == Key.Delete

                        if (isDeleteKey && it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                            onValueChange("")
                            return@onPreviewKeyEvent true
                        }
                        false
                    }
                }
                .onFocusEvent { isFocused = it.isFocused }
                .cellBackground(cellConfiguration.cellBackground),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next,
                autoCorrect = false,
            ),
            keyboardActions = KeyboardActions(
                onNext = {},
                onDone = {},
            ),
            singleLine = true,
            enabled = enabled,
            cursorBrush = SolidColor(configurations.cursorColor),
        ) { innerTextField ->
            TextFieldDefaults.TextFieldDecorationBox(
                value = value,
                visualTransformation = VisualTransformation.None,
                innerTextField = innerTextField,
                singleLine = true,
                enabled = enabled,
                interactionSource = interactionSource,
                contentPadding = PaddingValues(0.dp),
                placeholder = {
                    CellPlaceHolder(
                        placeHolder = placeHolder,
                        placeHolderTextStyle = placeHolderTextStyle,
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                ),
            )
        }
    }
}

private fun getCellConfig(
    enabled: Boolean,
    cellText: String,
    configurations: OhTeePeeConfigurations,
    isErrorOccurred: Boolean,
    isFocused: Boolean,
) = when {
    !enabled -> if (cellText.isEmpty()) configurations.emptyCellConfig else configurations.filledCellConfig
    isErrorOccurred -> configurations.errorCellConfig
    isFocused -> configurations.activeCellConfig
    cellText.isNotEmpty() -> configurations.filledCellConfig
    else -> configurations.emptyCellConfig
}

@Composable
private fun CellPlaceHolder(
    placeHolder: String,
    placeHolderTextStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    Text(
        text = placeHolder,
        style = placeHolderTextStyle,
        modifier = modifier.fillMaxWidth(),
    )
}
