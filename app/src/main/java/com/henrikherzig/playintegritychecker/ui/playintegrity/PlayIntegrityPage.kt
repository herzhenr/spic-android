package com.henrikherzig.playintegritychecker.ui.playintegrity

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.henrikherzig.playintegritychecker.R
import com.henrikherzig.playintegritychecker.attestation.PlayIntegrityStatement
import com.henrikherzig.playintegritychecker.ui.*

/**
 * returns the main ui of the App
 * @param playIntegrityResult api request result. Can be called with a initial value [ResponseType.None] as long
 * as no api request has been made
 * @param onPlayIntegrityRequest function to trigger when the API request is being made
 */
@Composable
fun PlayIntegrity(
    playIntegrityResult: State<ResponseType<PlayIntegrityStatement>>,
    onPlayIntegrityRequest: () -> Unit,
    selectedIndexCheck: String,
    itemsCheck: List<List<String>>,
    changedCheck: (String) -> Unit,
    selectedIndexNonce: String,
    itemsNonce: List<List<String>>,
    changedNonce: (String) -> Unit,
) {
    val context = LocalContext.current

    /**
     * Opens the Link external within primary browser
     * function not used at the moment
     */
    fun openLinkExternal(url: String) {
        // shorter alternative
        // val uriHandler = LocalUriHandler.current
        // uriHandler.openUri(url)

        runCatching {
            ContextCompat.startActivity(
                context,
                Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)),
                null
            )
        }
    }

    /**
     * opens a link in a preview window within the app
     */
    fun openLink(url: String) {
        CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                //.widthIn(max = 600.dp)
                .padding(all = 12.dp)
        ) {
            CustomCard() {
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

            /* "Make Play Integrity Request" button */
            CustomButton(
                onPlayIntegrityRequest,
                Icons.Outlined.GppGood,
                stringResource(id = R.string.playIntegrity_button)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // determines weather it is necessary to show the card with the API request result or not
            val showPlayIntegrityContent = playIntegrityResult.value != ResponseType.None

            /* Result Card (only visible, when button was pressed) */
            AnimatedVisibility(visible = showPlayIntegrityContent) {
                Box(
                    // Animation of box getting bigger when API result is shown
                    modifier = Modifier.animateContentSize()
                ) {
                    CustomCard {
                        ResultContent(playIntegrityResult) {
                            //playIntegrityResult.value = ResponseType.None
                        }
                    }
                }
            }
        }
    }
}