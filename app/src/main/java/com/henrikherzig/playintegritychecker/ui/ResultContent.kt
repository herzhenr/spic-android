package com.henrikherzig.playintegritychecker.ui

import android.util.Base64
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.henrikherzig.playintegritychecker.attestation.PlayIntegrityStatement
import com.henrikherzig.playintegritychecker.attestation.safetynet.SafetyNetStatement
import com.henrikherzig.playintegritychecker.attestation.Statement
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.henrikherzig.playintegritychecker.R

/**
 * Determines the UI elements to be shown when the "Make Play Integrity Request button is shown"
 * [ResponseType.SuccessSafetyNet] on success, show the APIResult element
 * [ResponseType.Failure] if something failed, show the Error element
 * [ResponseType.Loading] while loading show the Loading element
 * [ResponseType.None] if the button hasn't been pressed yet, show nothing
 */
@Composable
fun ResultContent(state: State<ResponseType<Statement>>, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
    ) {
        when (val result = state.value) {
            is ResponseType.SuccessSafetyNet -> SafetyNetResult(result.value)
            is ResponseType.SuccessPlayIntegrity -> PlayIntegrityResult(result.value)
            is ResponseType.Failure -> Error(result.error, onClose)
            is ResponseType.Loading -> Loading()
            //ResultOf.Initial -> return@MainCardColumn
            is ResponseType.None -> return
            is ResponseType.SuccessSimple -> Success(result.value)
            is ResponseType.RateLimiting -> RateLimiting(result.error)
        }
    }
}

/**
 * Shows a simple text with an question mark button which opens a popup explaining why the rate limiting is necessary
 * @param text error to show.
 **/
@Composable
fun RateLimiting(e: Throwable) {
    val text = e.message ?: stringResource(R.string.result_UnknownError)
    val opened = remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically/*, modifier = Modifier.padding(12.dp)*/) {
        Icon(
            Icons.Outlined.Error,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(10.dp))
        //e.message?.let { Text(text = it) }
        // multiline text but not more than width of screen
        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        CustomHelpButton(onClick = { opened.value = true })
        if (opened.value) {
            CustomContentAlertDialog(
                // modifier = Modifier.height(350.dp),
                titleString = "Test title",
                content = {
                    Text("Test content")
                },
                opened = opened,
            )
        }
    }
}

/**
 * Shows a simple text
 * @param text error to show.
 * **/
@Composable
fun Success(text: String) {
    val localClipboardManager = LocalClipboardManager.current
    Row(verticalAlignment = Alignment.CenterVertically/*, modifier = Modifier.padding(12.dp)*/) {
        Text(text = text)
    }
    TextButton(
        onClick = {
            localClipboardManager.setText(AnnotatedString(text))
        },
        //shape = MaterialTheme.shapes.medium,
        shape = RoundedCornerShape(10.dp)
    ) {
        Icon(
            Icons.Outlined.ContentCopy,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = stringResource(id = android.R.string.copy))
    }
}

/**
 * Shows an error icon and the corresponding error message.
 * Is being shown if something fails with the API request.
 * @param e error to show.
 * **/
@Composable
fun Error(e: Throwable, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically/*, modifier = Modifier.padding(12.dp)*/) {
        Icon(
            Icons.Outlined.Error,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(10.dp))
        //e.message?.let { Text(text = it) }
        Text(text = ((if (e.message == null) stringResource(R.string.result_UnknownError) else e.message.toString())))
        Spacer(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        CustomCloseButton(onClick = onClick)
    }
}

/**
 * Shows an animated loading symbol.
 * Is being shown while the API request is being made and processed.
 */
@Composable
fun Loading() {
    Row(verticalAlignment = Alignment.CenterVertically/*, modifier = Modifier.padding(12.dp)*/) {
        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(10.dp))
        Text(text = stringResource(R.string.result_loading))
    }
}


/**
 * Shows the result of the API request
 * @param attest Received Attestation Statement class which contains the API request information
 */
// API level cause of time functions
@Composable
fun PlayIntegrityResult(attest: PlayIntegrityStatement) {
    // Title
    CustomCardTitle(text = stringResource(R.string.pi_result_title))

    /* DEVICE INTEGRITY */
    val openedRecognition = remember { mutableStateOf(false) }
    val recognitionVerdict = attest.deviceIntegrity?.deviceRecognitionVerdict
    var state = 0
    var text = "NO_INTEGRITY"
    if (recognitionVerdict?.size!! > 0) {
        if (recognitionVerdict.contains("MEETS_STRONG_INTEGRITY")) {
            state = 3
            text = "MEETS_STRONG_INTEGRITY"
        } else if (recognitionVerdict.contains("MEETS_DEVICE_INTEGRITY")) {
            state = 2
            text = "MEETS_DEVICE_INTEGRITY"
        } else if (recognitionVerdict.contains("MEETS_BASIC_INTEGRITY")) {
            state = 1
            text = "MEETS_BASIC_INTEGRITY"
        }
        if (recognitionVerdict.contains("MEETS_VIRTUAL_INTEGRITY")) {
            text = "MEETS_VIRTUAL_INTEGRITY"
        }
    }
    CustomCardTitle2(stringResource(R.string.pi_result_deviceIntegrity))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            CustomCardTitle3(text = stringResource(R.string.pi_result_deviceRecognitionVerdict))
            CustomThreeStateIcons(state, text)
        }
        Spacer(modifier = Modifier.weight(1.0f))
        CustomHelpButton(onClick = { openedRecognition.value = true })
        if (openedRecognition.value) {
            CustomContentAlertDialog(
                modifier = Modifier.height(350.dp),
                titleString = stringResource(R.string.pi_result_deviceRecognitionVerdict),
                content = {
                    //deviceIntegrityAlertContent()
                    CustomSlideStack(if (text == "MEETS_VIRTUAL_INTEGRITY") 4 else state)
                },
                opened = openedRecognition,
            )
        }
    }

    /* APP INTEGRITY */
    if (attest.appIntegrity != null) {
        CustomCardTitle2(stringResource(R.string.pi_result_appIntegrity))
        Column(
            modifier = Modifier
                .padding(horizontal = 6.dp)
        ) {
            // Nonce
            val recognition = attest.appIntegrity.appRecognitionVerdict
            if (recognition != null) {
                var passed: Boolean? = null
                if (recognition != "UNEVALUATED") passed = recognition == "PLAY_RECOGNIZED"
                CustomCardTitle3(text = stringResource(R.string.pi_result_appRecognitionVerdict))
                CustomCardBoolHorizontal(recognition, passed)
            }
            if (attest.appIntegrity.packageName != null) {
                CustomCardGroup(
                    text1 = stringResource(R.string.pi_result_apkPackageName),
                    text2 = attest.appIntegrity.packageName
                )
            }
            if (attest.appIntegrity.certificateSha256Digest.size > 0) {
                CustomCardGroup(
                    text1 = stringResource(R.string.pi_result_apkCertificateDigestSha256),
                    text2 = attest.appIntegrity.certificateSha256Digest.toString()
                )
            }
            if (attest.appIntegrity.versionCode != null) {
                CustomCardGroup(
                    text1 = stringResource(R.string.pi_result_versionCode),
                    text2 = attest.appIntegrity.versionCode
                )
            }
        }
    }

    /* ACCOUNT DETAILS */
    val licensed = attest.accountDetails?.appLicensingVerdict
    if (licensed != null) {
        CustomCardTitle2(stringResource(R.string.pi_result_accountDetails))
        var passed: Boolean? = null
        if (licensed != "UNEVALUATED") passed = licensed == "LICENSED"
        Column(
            modifier = Modifier
                .padding(horizontal = 6.dp)
        ) {
            CustomCardTitle3(text = stringResource(R.string.pi_result_appLicensingVerdict))
            CustomCardBoolHorizontal(licensed, passed)
        }
    }

    fun String.decode(): String {
        return Base64.decode(this, Base64.DEFAULT).toString(charset("UTF-8"))
    }

    /* REQUEST DETAILS */
    if (attest.requestDetails != null) {
        CustomCardTitle2(stringResource(R.string.pi_result_requestDetails))
        Column(
            modifier = Modifier
                .padding(horizontal = 6.dp)
        ) {
            // Nonce
            if (attest.requestDetails.nonce != null) {
                CustomCardGroup(
                    text1 = stringResource(R.string.pi_result_nonce),
                    text2 = attest.requestDetails.nonce.decode()
                )
            }
            if (attest.requestDetails.requestPackageName != null) {
                CustomCardGroup(
                    text1 = stringResource(R.string.pi_result_requestPackageName),
                    text2 = attest.requestDetails.requestPackageName
                )
            }
            if (attest.requestDetails.timestampMillis != null) {
                val time = Instant.ofEpochMilli(attest.requestDetails.timestampMillis)
                    .atZone(ZoneId.systemDefault())
                CustomCardGroup(
                    text1 = stringResource(R.string.pi_result_timestamp),
                    text2 = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).format(time)
                )
            }
        }
    }

    // button to show received raw json
    val openedJson = remember { mutableStateOf(false) }
    TextButton(
        onClick = { openedJson.value = true },
        //shape = MaterialTheme.shapes.medium,
        shape = RoundedCornerShape(10.dp)
    ) {
        Icon(
            Icons.Outlined.Code,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = stringResource(R.string.result_rawJsonResult))
    }

    if (openedJson.value) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val rawJson = gson.toJson(attest)
        CustomCodeAlertDialog(
            rawJson,
            opened = openedJson,
        )
    }
}

// not used anymore
@Composable
private fun DeviceIntegrityAlertContent() {
    SelectionContainer {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            //Text(text = "The following state are possible:\n")
            CustomThreeStateIcons(0, "NO_INTEGRITY")
            Text(text = "The device has signs of an attack (such as API hooking) or system compromise (such as being rooted), or it isn't a physical device (such as an emulator that does not pass Google Play integrity checks).\n")
            CustomThreeStateIcons(1, "MEETS_BASIC_INTEGRITY")
            Text(text = "The device passes basic system integrity checks. It may not meet Android compatibility requirements and may not be approved to run Google Play services (eg. device is running unrecognized version of Android, may have an unlocked bootloader, or may not have been certified by the manufacturer).\n")
            CustomThreeStateIcons(2, "MEETS_DEVICE_INTEGRITY")
            Text(text = "The Android device is powered by Google Play services. It passes system integrity checks and meets Android compatibility requirements.\n")
            CustomThreeStateIcons(3, "MEETS_STRONG_INTEGRITY")
            Text(text = "The Android device is powered by Google Play services and has a strong guarantee of system integrity such as a hardware-backed proof of boot integrity. It passes system integrity checks and meets Android compatibility requirements.\n")
            CustomThreeStateIcons(0, "MEETS_VIRTUAL_INTEGRITY")
            Text(text = "The app is running on an Android emulator powered by Google Play services. The emulator passes system integrity checks and meets core Android compatibility requirements.")
        }
    }
}

/**
 * Shows the result of the API request
 * @param attest Received Attestation Statement class which contains the API request information
 */
// API level cause of time functions
@Composable
fun SafetyNetResult(attest: SafetyNetStatement) {
    // Title
    CustomCardTitle(text = stringResource(R.string.sn_result_title))

    // Nonce
    CustomCardGroup(
        text1 = stringResource(R.string.sn_result_nonce),
        text2 = attest.nonce
    )

    // Timestamp (parse timestamp and show it in day, year, hour:minute:second Format)
    val time = Instant.ofEpochMilli(attest.timestampMs).atZone(ZoneId.systemDefault())
    CustomCardGroup(
        text1 = stringResource(R.string.sn_result_timestamp),
        text2 = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).format(time)
    )

    // Integrity Type
    CustomCardGroup(
        text1 = stringResource(R.string.sn_result_evaluationType),
        text2 = attest.integrityType()
    )

    // Basic Integrity
    CustomCardBool(
        text = stringResource(R.string.sn_result_basicIntegrity),
        passed = attest.hasBasicIntegrity()
    )

    // CTS Profile Match
    CustomCardBool(
        text = stringResource(R.string.sn_result_ctsProfileMatch),
        passed = attest.isCtsProfileMatch
    )

    // Package Name
    CustomCardGroup(
        text1 = stringResource(R.string.sn_result_apkPackageName),
        text2 = attest.apkPackageName
    )

    // APK digest sha256
    CustomCardGroup(
        text1 = stringResource(R.string.sn_result_digest),
        text2 = attest.apkDigestSha256
    )

    // APK Certificate Digest
    CustomCardGroup(
        text1 = stringResource(R.string.sn_result_certificateDigest),
        text2 = attest.apkCertificateDigestSha256.toString()
    )

    // button to show received raw json
    val opened = remember { mutableStateOf(false) }
    TextButton(
        onClick = { opened.value = true },
        shape = RoundedCornerShape(10.dp)
    ) {
        Icon(
            Icons.Outlined.Code,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = stringResource(R.string.result_rawJsonResult))
    }

    if (opened.value) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val rawJson = gson.toJson(attest)
        CustomCodeAlertDialog(
            rawJson,
            opened = opened,
        )
    }
}