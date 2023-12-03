package com.henrikherzig.playintegritychecker.attestation.playintegrity

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse
import com.google.gson.Gson
import com.henrikherzig.playintegritychecker.BuildConfig
import com.henrikherzig.playintegritychecker.attestation.AttestationException
import com.henrikherzig.playintegritychecker.attestation.PlayIntegrityStatement
import com.henrikherzig.playintegritychecker.attestation.RateLimiterPlayIntegrity
import com.henrikherzig.playintegritychecker.attestation.getApiCall
import com.henrikherzig.playintegritychecker.ui.ResponseType
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jose4j.jwe.JsonWebEncryption
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwx.JsonWebStructure
import org.jose4j.lang.JoseException
import org.json.JSONObject
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.math.floor

class AttestationCallPlayIntegrity : ViewModel() {

    val playIntegrityResult: MutableState<ResponseType<PlayIntegrityStatement>> =
        mutableStateOf(ResponseType.None)

    fun playIntegrityRequest(
        context: Context,
        nonceGeneration: String,
        verifyType: String,
        url: String?
    ) = viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, _ -> }) {
        // set UI to loading (api calls etc. take some time...)
        playIntegrityResult.value = ResponseType.Loading

        // rate limiting
        RateLimiterPlayIntegrity(context).shouldRequestBeMade()?.let {
            playIntegrityResult.value = it
            return@launch
        }

        // Generate nonce
        val nonce: String = try {
            generateNonce(nonceGeneration, url)
        } catch (e: Exception) {
            e.printStackTrace()
            // print received error message to the UI
            playIntegrityResult.value = ResponseType.Failure(e)
            return@launch
        }

        // Create an instance of a manager.
        val integrityManager = IntegrityManagerFactory.create(context)

        // Request the integrity token by providing a nonce.
        val integrityTokenResponse: Task<IntegrityTokenResponse> =
            integrityManager.requestIntegrityToken(
                IntegrityTokenRequest.builder()
                    .setNonce(nonce)
                    // .setCloudProjectNumber(757430732184) // hardcoded for now
                    .build()
            )

        // do play integrity api call
        integrityTokenResponse.addOnSuccessListener { response ->
            run {
                // get token
                val integrityToken: String = response.token()
                // println(integrityToken)

                // show received token in UI
                // playIntegrityResult.value = ResponseType.SuccessSimple(integrityToken)

                // decode and verify
                try {
                    decodeAndVerify(verifyType, integrityToken, nonceGeneration, url)
                } catch (e: JoseException) {
                    Log.d(TAG, "can't decode Play Integrity response")
                    e.printStackTrace()
                    playIntegrityResult.value =
                        ResponseType.Failure(Throwable("can't decode Play Integrity response"))
                    return@run
                }
            }
        }.addOnFailureListener { e ->
            Log.d(TAG, "API Error, see Android UI for error message")
            playIntegrityResult.value = ResponseType.Failure(e)
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
                getNonceLocal(50)
            }
            // Receive nonce from the secure server
            "server" -> getNonceServer(apiServerUrl)
            // unknown nonceGeneration
            else -> throw (Throwable(message = "nonceGeneration '$nonceGeneration' is unknown"))
        }
        return nonce
    }

    private fun String.encode(): String {
        return Base64.encodeToString(this.toByteArray(charset("UTF-8")), Base64.URL_SAFE)
    }

    /**
     * generates a nonce locally
     */
    private fun getNonceLocal(length: Int): String {
        var nonce = ""
        val allowed = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        for (i in 0 until length) {
            nonce += allowed[floor(Math.random() * allowed.length).toInt()].toString()
        }
        return nonce.encode()
    }

    /**
     * get nonce form secure server. sends a api request to the server to get the nonce
     */
    private fun getNonceServer(url: String?): String {
        if (url == null || url == "") throw AttestationException("no url for server provided. check server url in settings")
        return getApiCall(url, "/api/playintegrity/nonce")
    }

    /**
     *
     */
    private fun decodeAndVerify(
        verifyType: String,
        integrityToken: String,
        nonceGeneration: String,
        url: String?
    ) {
        when (verifyType) {
            "local" -> {
                // decrypt verdict locally and update UI
                val decoded: PlayIntegrityStatement = decodeLocally(integrityToken)
                playIntegrityResult.value = ResponseType.SuccessPlayIntegrity(decoded)
            }
            "google", "server" -> {
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
                            playIntegrityResult.value =
                                ResponseType.SuccessPlayIntegrity(decoded)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        handler.post {
                            playIntegrityResult.value = ResponseType.Failure(e)
                        }
                    }
                }
            }
            else -> {
                playIntegrityResult.value =
                    ResponseType.Failure(Throwable(message = "verifyType '$verifyType' is unknown"))
            }
        }
    }

    /**
     * decodes the integrity verdict locally on the device for test purposes (not recommended for
     * production use)
     **/
    private fun decodeLocally(integrityToken: String): PlayIntegrityStatement {
        val base64OfEncodedDecryptionKey = BuildConfig.base64_of_encoded_decryption_key

        // base64OfEncodedDecryptionKey is provided through Play Console.
        val decryptionKeyBytes: ByteArray =
            Base64.decode(base64OfEncodedDecryptionKey, Base64.DEFAULT)

        // Deserialized encryption (symmetric) key.
        val decryptionKey: SecretKey = SecretKeySpec(
            decryptionKeyBytes,
            /* offset= */ 0,
            decryptionKeyBytes.size,
            "AES"
        )

        val base64OfEncodedVerificationKey =BuildConfig.base64_of_encoded_verification_key
        // base64OfEncodedVerificationKey is provided through Play Console.
        val encodedVerificationKey: ByteArray =
            Base64.decode(base64OfEncodedVerificationKey, Base64.DEFAULT)

        // Deserialized verification (public) key.
        val verificationKey: PublicKey = KeyFactory.getInstance("EC")
            .generatePublic(X509EncodedKeySpec(encodedVerificationKey))


        val jwe: JsonWebEncryption =
            JsonWebStructure.fromCompactSerialization(integrityToken) as JsonWebEncryption
        jwe.key = decryptionKey

        // This also decrypts the JWE token.
        val compactJws = jwe.payload

        val jws: JsonWebSignature =
            JsonWebStructure.fromCompactSerialization(compactJws) as JsonWebSignature
        jws.key = verificationKey

        // This also verifies the signature.
        val payload: String = jws.payload
        println(payload)
        return Gson().fromJson(payload, PlayIntegrityStatement::class.java)
    }

    private fun checkPlayIntegrityServer(
        apiToken: String,
        verifyType: String,
        nonceGeneration: String,
        apiURL: String?
    ): PlayIntegrityStatement {
        // make api call
        if (apiURL == null) throw AttestationException("no url for server provided. check server url in settings")
        val response = getApiCall(
            apiURL,
            "/api/playintegrity/check",
            "token=$apiToken&mode=$verifyType&nonce=$nonceGeneration"
        )

        val json= JSONObject(response)

        if (json.has("error")) {
            Log.d(TAG, "Api request error: " + json.getString("error"))
            throw AttestationException("Api request error: " + json.getString("error"))
            // return "Api request error: " + json.getString("error")
        }

        if (!json.has("deviceIntegrity")) {
            Log.d(TAG, "Api request error: Response does not contain deviceIntegrity")
            throw AttestationException("Api request error: Response does not contain deviceIntegrity")
            // return "Api request error: Response does not contain deviceIntegrity"
        }
        val jsonString = json.toString()
        return Gson().fromJson(jsonString, PlayIntegrityStatement::class.java)
    }
}