package com.composeuisuite.ohteepee

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.input.KeyboardType
import com.composeuisuite.ohteepee.configuration.OhTeePeeConfigurations
import com.composeuisuite.ohteepee.example.BasicOhTeePeeExample
import com.composeuisuite.ohteepee.utils.EMPTY
import com.composeuisuite.ohteepee.utils.requestFocusSafely

/**
 * OhTeePeeInput is a composable that can be used to get OTP/Pin from user.
 *
 * Whenever the user edits the text, [onValueChange] is called with the most up to date state
 * including the empty values that represented by [OhTeePeeConfigurations.placeHolder].
 *
 * When the user fills all the cells, [onValueChange]'s isValid parameter will be `true`,
 * otherwise it will be `false`.
 *
 * To customize the appearance of cells you can pass [configurations] parameter with
 * a lot of options like , [OhTeePeeConfigurations.cellModifier], [OhTeePeeConfigurations.errorCellConfig] and more.
 *
 * If you don't want to pass all the configurations, you can use [OhTeePeeConfigurations.withDefaults] to customize
 * only the configurations you want.
 *
 * @param value will be split to chars and shown in the [OhTeePeeInput].
 * @param onValueChange Called when user enters or deletes any cell.
 * @param configurations [OhTeePeeConfigurations] to customize the appearance of cells.
 *
 * @param modifier optional [Modifier] for the whole [OhTeePeeInput].
 * You can use [OhTeePeeConfigurations.cellModifier] to customize each single cell.
 *
 * @param isValueInvalid when set to true, all cells will use [OhTeePeeConfigurations.errorCellConfig] and
 * focus will be requested on first cell. you should set this to false when user starts editing the text.
 *
 * @param keyboardType The keyboard type to be used for this text field.
 *
 * @param enabled when enabled is false, user will not be able to interact with the text fields.
 * you can set it to false when you are waiting for a response from server.
 *
 * @param autoFocusByDefault when set to true, first cell will be focused by default.
 *
 * @sample BasicOhTeePeeExample
 */
@Composable
fun OhTeePeeInput(
    value: String,
    onValueChange: (newValue: String, isValid: Boolean) -> Unit,
    configurations: OhTeePeeConfigurations,
    modifier: Modifier = Modifier,
    isValueInvalid: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Number,
    enabled: Boolean = true,
    autoFocusByDefault: Boolean = true
) {
    require(configurations.placeHolder.length == 1) {
        "PlaceHolder must be a single character"
    }
    require(configurations.obscureText.length <= 1) {
        "obscureText can't be more then 1 characters"
    }

    val obscureText = configurations.obscureText
    val placeHolder = configurations.placeHolder
    val cellsCount = configurations.cellsCount
    val placeHolderAsChar = placeHolder.first()
    val otpValue by remember(value, isValueInvalid, placeHolder) {
        val charList = CharArray(cellsCount) { index ->
            if (isValueInvalid) {
                placeHolderAsChar
            } else {
                value.getOrElse(index) { placeHolderAsChar }
            }
        }
        mutableStateOf(charList)
    }
    val focusRequester = remember(cellsCount) { List(cellsCount) { FocusRequester() } }
    val transparentTextSelectionColors: TextSelectionColors = remember {
        TextSelectionColors(
            handleColor = Transparent,
            backgroundColor = Transparent
        )
    }

    fun requestFocus(index: Int) {
        val nextIndex = index.coerceIn(0, cellsCount - 1)
        focusRequester[nextIndex].requestFocusSafely()
    }

    LaunchedEffect(autoFocusByDefault) {
        if (autoFocusByDefault) focusRequester.first().requestFocus()
    }

    LaunchedEffect(isValueInvalid) {
        if (isValueInvalid) focusRequester.first().requestFocus()
    }

    OhTeePeeInput(
        modifier = modifier,
        textSelectionColors = transparentTextSelectionColors,
        cellsCount = cellsCount,
        otpValue = otpValue,
        obscureText = obscureText,
        placeHolder = placeHolder,
        isErrorOccurred = isValueInvalid,
        keyboardType = keyboardType,
        ohTeePeeConfigurations = configurations,
        focusRequesters = focusRequester,
        enabled = enabled,
        onCellInputChange = onCellInputChange@{ currentCellIndex, newValue ->
            val currentCellText = otpValue[currentCellIndex].toString()
            val formattedNewValue = newValue.replace(placeHolder, String.EMPTY)
                .replace(obscureText, String.EMPTY)

            if (formattedNewValue == currentCellText) {
                requestFocus(currentCellIndex + 1)
                return@onCellInputChange
            }

            if (formattedNewValue.length == cellsCount) {
                onValueChange(formattedNewValue, true)
                focusRequester.last().requestFocusSafely()
                return@onCellInputChange
            }

            if (formattedNewValue.isNotEmpty()) {
                otpValue[currentCellIndex] = formattedNewValue.last()
                requestFocus(currentCellIndex + 1)
            } else if (currentCellText != placeHolder) {
                otpValue[currentCellIndex] = placeHolderAsChar
            } else {
                val previousIndex = (currentCellIndex - 1).coerceIn(0, cellsCount)
                otpValue[previousIndex] = placeHolderAsChar
                requestFocus(previousIndex)
            }
            val otpValueAsString = otpValue.joinToString(String.EMPTY)
            onValueChange(otpValueAsString, otpValueAsString.none { it == placeHolderAsChar })
        },
    )
}

@Composable
private fun OhTeePeeInput(
    modifier: Modifier = Modifier,
    textSelectionColors: TextSelectionColors,
    cellsCount: Int,
    otpValue: CharArray,
    obscureText: String,
    placeHolder: String,
    isErrorOccurred: Boolean,
    keyboardType: KeyboardType,
    ohTeePeeConfigurations: OhTeePeeConfigurations,
    focusRequesters: List<FocusRequester>,
    enabled: Boolean,
    onCellInputChange: (index: Int, value: String) -> Unit
) {
    CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(cellsCount) { index ->
                val displayValue = getCellDisplayCharacter(
                    currentChar = otpValue[index],
                    obscureText = obscureText,
                    placeHolder = placeHolder
                )
                OhTeePeeCell(
                    value = displayValue,
                    isErrorOccurred = isErrorOccurred,
                    keyboardType = keyboardType,
                    modifier = ohTeePeeConfigurations.cellModifier
                        .focusRequester(focusRequester = focusRequesters[index]),
                    enabled = enabled,
                    configurations = ohTeePeeConfigurations,
                    isCurrentCharAPlaceHolder = displayValue == placeHolder,
                    onValueChange = {
                        onCellInputChange(index, it)
                    }
                )
            }
        }
    }
}

@Composable
private fun getCellDisplayCharacter(
    currentChar: Char,
    obscureText: String,
    placeHolder: String
): String = currentChar
    .toString()
    .replace(placeHolder, "")
    .let {
        if (it.isNotEmpty() && obscureText.isNotEmpty()) {
            obscureText
        } else {
            it
        }
    }
    .takeIf { it.isNotEmpty() } ?: placeHolder