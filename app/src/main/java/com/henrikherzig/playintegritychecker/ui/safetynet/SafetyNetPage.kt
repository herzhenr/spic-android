package com.henrikherzig.playintegritychecker.ui.safetynet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.henrikherzig.playintegritychecker.attestation.safetynet.SafetyNetStatement
import com.henrikherzig.playintegritychecker.R
import com.henrikherzig.playintegritychecker.ui.CustomButton
import com.henrikherzig.playintegritychecker.ui.CustomCard
import com.henrikherzig.playintegritychecker.ui.RequestSettings
import com.henrikherzig.playintegritychecker.ui.ResultContent
import com.henrikherzig.playintegritychecker.ui.ResponseType

/**
 * returns the main ui of the App
 * @param safetyNetResult api request result. Can be called with a initial value [ResponseType.None] as long
 * as no api request has been made
 * @param onSafetyNetRequest function to trigger when the API request is being made
 */
@Composable
fun SafetyNet(
    safetyNetResult: State<ResponseType<SafetyNetStatement>>,
    onSafetyNetRequest: () -> Unit,
    selectedIndexCheck: String,
    itemsCheck: List<List<String>>,
    changedCheck: (String) -> Unit,
    selectedIndexNonce: String,
    itemsNonce: List<List<String>>,
    changedNonce: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        /* Main column in which every other UI element for the main screen is in */
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                //.widthIn(max = 600.dp)
                .padding(all = 12.dp)
        ) {
            CustomCard {
                RequestSettings(
                    selectedIndexCheck,
                    itemsCheck,
                    changedCheck,
                    selectedIndexNonce,
                    itemsNonce,
                    changedNonce
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            /* "Make SafetyNet AttestationAPI Request" button */
            CustomButton(
                onSafetyNetRequest,
                Icons.Outlined.GppGood,
                stringResource(id = R.string.safetyNet_attestation_button)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // determines weather it is necessary to show the card with the API request result or not
            val showSafetyNetContent = safetyNetResult.value != ResponseType.None

            /* Result Card (only visible, when button was pressed) */
            AnimatedVisibility(visible = showSafetyNetContent) {
                Box(
                    // Animation of box getting bigger when API result is shown
                    modifier = Modifier.animateContentSize()
                ) {
                    CustomCard {
                        ResultContent(safetyNetResult){
                            //playIntegrityResult.value = ResponseType.None
                        }
                    }
                }
            }
        }
    }
}