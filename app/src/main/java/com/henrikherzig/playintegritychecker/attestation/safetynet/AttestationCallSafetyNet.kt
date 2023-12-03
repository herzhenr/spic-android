package com.henrikherzig.playintegritychecker.attestation.safetynet

import android.content.ContentValues
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.safetynet.SafetyNet
import com.henrikherzig.playintegritychecker.BuildConfig
import com.henrikherzig.playintegritychecker.attestation.AttestationException
import com.henrikherzig.playintegritychecker.attestation.RateLimiterSafetyNet
import com.henrikherzig.playintegritychecker.attestation.getApiCall
import com.henrikherzig.playintegritychecker.ui.ResponseType
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class AttestationCallSafetyNet : ViewModel() {

    val safetyNetResult: MutableState<ResponseType<SafetyNetStatement>> =
        mutableStateOf(ResponseType.None)

    /**
     * API depricated
     * calls Googles Safety Net attestation API and process its contents
     */
    fun safetyNetAttestationRequest(
        context: Context,
        nonceGeneration: String,
        verifyType: String,
        url: String?
    ) =
        viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, _ ->

        }) {
            // Set UI to loading screen
            safetyNetResult.value = ResponseType.Loading

            // rate limiting
            RateLimiterSafetyNet(context).shouldRequestBeMade()?.let {
                safetyNetResult.value = it
                return@launch
            }

            // Receive the nonce from the secure server.
            val nonce: String = try {
                generateNonce(nonceGeneration, url)
            } catch (e: Exception) {
                e.printStackTrace()
                // print received error message to the UI
                safetyNetResult.value = ResponseType.Failure(e)
                return@launch
            }

            val apikey = BuildConfig.api_key

            // check if Google Services are available
            if (GoogleApiAvailability.getInstance()
                    .isGooglePlayServicesAvailable(context, 13000000) !=
                ConnectionResult.SUCCESS
            ) {
                // if not, show error message
                println("ERROR: Outdated Google Play Services")
                safetyNetResult.value = ResponseType.Failure(
                    AttestationException("GooglePlay Services Error: Outdated Google Play Services")
                )
                return@launch
                // TODO Prompt user to update Google Play Services.
            }
            // The SafetyNet Attestation API is available after this check

            // actual call to attestation API
            SafetyNet.getClient(context.applicationContext).attest(nonce.toByteArray(), apikey)
                .addOnSuccessListener {
                    // Indicates communication with the service was successful.
                    try {
                        val token = it.jwsResult
                            ?: throw AttestationException("No token from SafetyNet Attestation Received")
                        decodeAndVerify(verifyType, token, nonceGeneration, url, nonce)
                    } catch (e: Exception) {
                        Log.w(ContentValues.TAG, "Error in Offline Verify: ", e)
                        safetyNetResult.value = ResponseType.Failure(e)
                    }

                }
                .addOnFailureListener { e ->
                    // An error occurred while communicating with the service.
                    if (e is ApiException) {
                        // An error with the Google Play services API contains some additional details.
                        e.statusCode
                        // You can retrieve the status code using the apiException.statusCode property.
                        Log.d(
                            ContentValues.TAG,
                            "Error with Google Play Services API: " + e.message
                        )
                    } else {
                        // A different, unknown type of error occurred.
                        Log.d(ContentValues.TAG, "Unknown Error Type: " + e.message)
                    }
                    safetyNetResult.value = ResponseType.Failure(e)
                }
        }


    /**
     * generates a nonce. Depending on [nonceGeneration] this happens locally on the device or
     * remotely on a server with the url [apiServerUrl]
     */
    private fun generateNonce(nonceGeneration: String, apiServerUrl: String?): String {
        val nonce: String = when (nonceGeneration) {
            // Generate nonce locally
            "local" -> {
                getNonceLocal()
            }
            // Receive nonce from the secure server
            "server" -> getNonceServer(apiServerUrl)
            // unknown nonceGeneration
            else -> throw (Throwable(message = "nonceGeneration '$nonceGeneration' is unknown"))
        }
        return nonce
    }

    /**
     * generates a nonce locally
     */
    private fun getNonceLocal(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * get nonce form secure server. sends a api request to the server to get the nonce
     */
    private fun getNonceServer(url: String?): String {
        if (url == null || url == "") throw AttestationException("no url for server provided. check server url in settings")
        return getApiCall(url, "/api/safetynet/nonce")
    }

    /**
     *
     */
    private fun decodeAndVerify(
        verifyType: String,
        integrityToken: String,
        nonceGeneration: String,
        url: String?,
        nonce: String
    ) {
        when (verifyType) {
            "local" -> {
                // decrypt verdict locally and update UI
                val decoded: SafetyNetStatement =
                    SafetyNetProcess.decode(integrityToken)

                // verify token
                // SafetyNetProcess.verify(decoded, nonce)

                safetyNetResult.value = ResponseType.SuccessSafetyNet(decoded)
            }
            "server" -> {
                // Create executor for async execution of API call
                val executor: Executor = Executors.newSingleThreadExecutor()
                val handler = Handler(Looper.getMainLooper())
                executor.execute {
                    try {
                        val decoded = checkPlayIntegrityServer(
                            integrityToken,
                            verifyType,
                            nonceGeneration,
                            url
                        )
                        // perform task asynchronously
                        handler.post {
                            safetyNetResult.value =
                                ResponseType.SuccessSafetyNet(decoded)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        handler.post {
                            safetyNetResult.value = ResponseType.Failure(e)
                        }
                    }
                }
            }
            else -> {
                safetyNetResult.value =
                    ResponseType.Failure(Throwable(message = "verifyType '$verifyType' is unknown"))
            }
        }
    }


    private fun checkPlayIntegrityServer(
        apiToken: String,
        verifyType: String,
        nonceGeneration: String,
        apiURL: String?
    ): SafetyNetStatement {
        // make api call
        if (apiURL == null) throw AttestationException("no url for server provided. check server url in settings")
        val response = getApiCall(
            apiURL,
            "/api/safetynet/check",
            "token=$apiToken&mode=$verifyType&nonce=$nonceGeneration"
        )

        val json = JSONObject(response)

        val nonce = json.get("nonce").toString()
        val timestampMs = json.getLong("timestampMs")
        val apkPackageName = json.get("apkPackageName").toString()
        val apkCertificateDigestSha256Array = json.getJSONArray("apkCertificateDigestSha256")
        val apkCertificateDigestSha256 =
            arrayOfNulls<String>(apkCertificateDigestSha256Array.length())
        for (i in 0 until apkCertificateDigestSha256Array.length()) {
            apkCertificateDigestSha256[i] = apkCertificateDigestSha256Array.optString(i)
        }
        val apkDigestSha256 = json.get("apkDigestSha256").toString()
        val ctsProfileMatch: Boolean = json.getBoolean("ctsProfileMatch")
        val basicIntegrity: Boolean = json.getBoolean("basicIntegrity")
        val evaluationType = json.get("evaluationType").toString()

        //val jsonString = json.toString()
        return SafetyNetStatement(
            nonce,
            timestampMs,
            apkPackageName,
            apkCertificateDigestSha256,
            apkDigestSha256,
            ctsProfileMatch,
            basicIntegrity,
            evaluationType
        )
        //return Gson().fromJson(jsonString, SafetyNetStatement::class.java)
    }

}