package com.henrikherzig.playintegritychecker


import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.common.GoogleApiAvailability
import com.henrikherzig.playintegritychecker.attestation.playintegrity.AttestationCallPlayIntegrity
import com.henrikherzig.playintegritychecker.attestation.safetynet.AttestationCallSafetyNet
import com.henrikherzig.playintegritychecker.ui.navigationbar.BottomNavigationBar
import com.henrikherzig.playintegritychecker.ui.theme.PlayIntegrityCheckerTheme

// PreferenceDataStore for settings
val Context.dataStore by preferencesDataStore("settings")

class MainActivity : ComponentActivity() {

    private val viewSafetyNet: AttestationCallSafetyNet by viewModels()
    private val viewPlayIntegrity: AttestationCallPlayIntegrity by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlayIntegrityCheckerTheme() {
                // function to be called when safetyNetRequest is made
                val onSafetyNetRequest: (String, String, String?) -> Unit =
                    { nonceGeneration: String, verifyType: String, url: String? ->
                        viewSafetyNet.safetyNetAttestationRequest(
                            this@MainActivity,
                            nonceGeneration,
                            verifyType,
                            url
                        )
                    }

                // function to be called when playIntegrityRequest is made
                val onPlayIntegrityRequest: (String, String, String?) -> Unit =
                    { nonceGeneration: String, verifyType: String, url: String? ->
                        viewPlayIntegrity.playIntegrityRequest(
                            this@MainActivity,
                            nonceGeneration,
                            verifyType,
                            url
                        )
                    }

                // get play services Version
                val playServiceVersion = try {
                    val packageInfo = packageManager.getPackageInfo(
                        GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE,
                        0
                    )
                    packageInfo.versionName
                } catch (e: Throwable) {
                    null
                }

                // create MainUI of App
                BottomNavigationBar(
                    viewSafetyNet.safetyNetResult,
                    onSafetyNetRequest,
                    viewPlayIntegrity.playIntegrityResult,
                    onPlayIntegrityRequest,
                    playServiceVersion
                )
            }
        }
    }
}