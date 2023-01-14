package com.henrikherzig.playintegritychecker.ui

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Help
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.henrikherzig.playintegritychecker.R

/**
 * settings ui for the request
 */
@Composable
fun RequestSettings(
    selectedIndexCheck: String,
    itemsCheck: List<List<String>>,
    changedCheck: (String) -> Unit,
    selectedIndexNonce: String,
    itemsNonce: List<List<String>>,
    changedNonce: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
    ) {
        val openedNonce = remember { mutableStateOf(false) }
        val openedCheck = remember { mutableStateOf(false) }
        CustomCardTitle(text = stringResource(id = R.string.requestSettings_title))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            CustomCardTitle3(text = stringResource(id = R.string.requestSettings_nonceCreation))
            CustomHelpButton(onClick = {openedNonce.value = true})
            if (openedNonce.value) {
                CustomTextAlertDialog(
                    stringResource(id = R.string.requestSettings_nonceCreation),
                    stringResource(id = R.string.requestSettings_nonceCreation_helpText),
                    opened = openedNonce,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        ToggleGroup(selectedIndexCheck, itemsCheck, changedCheck, 35.dp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            CustomCardTitle3(text = stringResource(id = R.string.requestSettings_checkVerdict))
            CustomHelpButton(onClick = {openedCheck.value = true})
            if (openedCheck.value) {
                CustomTextAlertDialog(
                    stringResource(id = R.string.requestSettings_checkVerdict),
                    stringResource(id = R.string.requestSettings_checkVerdict_helpText),
                    opened = openedCheck,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        ToggleGroup(selectedIndexNonce, itemsNonce, changedNonce, 35.dp)
        //CustomCard
    }
}

/**
 * shows device information in a card
 */
@Composable
fun DeviceInfoContent(playServiceVersion: String?) {
    CustomCard {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
        ) {
            CustomCardTitle(text = stringResource(R.string.info_device))

            CustomCardGroup(
                text1 = stringResource(R.string.info_model),
                text2 = "${Build.MODEL} (${Build.DEVICE})"
            )
            CustomCardGroup(
                text1 = stringResource(R.string.info_androidVersion),
                text2 = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
            )

            CustomCardGroup(
                text1 = stringResource(R.string.info_securityPatch),
                text2 = Build.VERSION.SECURITY_PATCH
            )

            if (playServiceVersion != null) {
                CustomCardGroup(
                    text1 = stringResource(R.string.info_playServicesVersion),
                    text2 = playServiceVersion
                )
            }
        }
    }
}
