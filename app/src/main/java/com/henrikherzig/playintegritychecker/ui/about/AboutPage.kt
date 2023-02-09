package com.henrikherzig.playintegritychecker.ui.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.ReadMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.henrikherzig.playintegritychecker.R
import com.henrikherzig.playintegritychecker.ui.*

@Composable
fun AboutPage(navController: NavHostController) {
    val context = LocalContext.current
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
            CustomCardTitle("About this App")
            CustomCard {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically/*, modifier = Modifier.padding(12.dp)*/){
                        Text(stringResource(id = R.string.about_this_app))
                    }
                }
            }
            Spacer(Modifier.size(12.dp))

            CustomCardTitle("API Info")
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                //.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    /* Source Code Link */
                    val link = stringResource(id = R.string.about_api_playIntegrityLink)
                    CustomButton(
                        { openLink(link, context) },
                        Icons.Outlined.OpenInNew,
                        stringResource(id = R.string.about_api_playIntegrityButton)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    /* Play Integrity API Link */
                    val link = stringResource(id = R.string.about_api_safetyNetLink)
                    CustomButton(
                        { openLink(link, context) },
                        Icons.Outlined.OpenInNew,
                        stringResource(id = R.string.about_api_safetyNetButton)
                    )
                }
            }
            Spacer(Modifier.size(12.dp))
            CustomCardTitle("Source code")
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                /* workaround: .weight is not accessible in button directly and also not if box
                   is extracted to other method */
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    /* Source Code Link */
                    val link = stringResource(id = R.string.about_sourceCode_appLink)
                    CustomButton(
                        { openLink(link, context) },
                        Icons.Outlined.Code,
                        stringResource(id = R.string.about_sourceCode_appButton)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    /* Play Integrity API Link */
                    val link = stringResource(id = R.string.about_sourceCode_serverLink)
                    CustomButton(
                        { openLink(link, context) },
                        Icons.Outlined.Code,
                        stringResource(id = R.string.about_sourceCode_serverButton)
                    )
                }
            }
            Spacer(Modifier.size(12.dp))
            CustomCardTitle(stringResource(id = R.string.about_licenseAndPrivacy))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                //.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    /* License */
                    val openedRecognition = remember { mutableStateOf(false) }
                    CustomButton(
                        { openedRecognition.value = true },
                        Icons.Outlined.ReadMore,
                        stringResource(id = R.string.about_licenseButton)
                    )
                    if (openedRecognition.value) {
                        openedRecognition.value=false
                        navController.navigate("licence") {

                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    /* Privacy */
                    val link = stringResource(id = R.string.about_privacyLink)
                    CustomButton(
                        { openLink(link, context) },
                        Icons.Outlined.ReadMore,
                        stringResource(id = R.string.about_privacyButton) // policy
                    )
                }
            }

        }
    }
}