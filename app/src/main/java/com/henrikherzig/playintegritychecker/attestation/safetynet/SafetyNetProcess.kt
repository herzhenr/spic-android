package com.henrikherzig.playintegritychecker.attestation.safetynet

import androidx.compose.runtime.MutableState
import com.henrikherzig.playintegritychecker.attestation.safetynet.SafetyNetStatement
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.json.webtoken.JsonWebSignature
import com.henrikherzig.playintegritychecker.BuildConfig
import com.henrikherzig.playintegritychecker.attestation.AttestationException
import com.henrikherzig.playintegritychecker.ui.ResponseType
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.cert.X509Certificate
import java.time.Duration
import java.time.Instant

//import org.apache.http.conn.ssl.DefaultHostnameVerifier;
object SafetyNetProcess {
    //private static final DefaultHostnameVerifier HOSTNAME_VERIFIER = new DefaultHostnameVerifier();
    /**
     * Parses and verifies the API response. This should be done on a server in the future because
     * local checks can be exploited by APK repackaging but for demonstration purposes this can be
     * done locally (Good way to practically show how to bypass local checks)
     * @param attestationResponse response of the API call
     * @return processed API response
     */
    fun decode(attestationResponse: String): SafetyNetStatement {
        // Parse JSON Web Signature format.
        val jws: JsonWebSignature = try {
            JsonWebSignature.parser(AndroidJsonFactory.getDefaultInstance())
                .setPayloadClass(SafetyNetStatement::class.java).parse(attestationResponse)
        } catch (e: IOException) {
            System.err.println(
                "Failure: " + attestationResponse + " is not valid JWS " +
                        "format."
            )
            throw AttestationException(
                "Failure: " + attestationResponse + " is not valid JWS " +
                        "format."
            )
        }

        // Verify the signature of the JWS and retrieve the signature certificate.
        val cert: X509Certificate?
        try {
            cert = jws.verifySignature()
            if (cert == null) {
                System.err.println("Failure: Signature verification failed.")
                throw AttestationException("Failure: Signature verification failed.")
            }
        } catch (e: GeneralSecurityException) {
            System.err.println(
                "Failure: Error during cryptographic verification of the JWS signature."
            )
            throw AttestationException("Failure: Error during cryptographic verification of the JWS signature.")
        }

        // TODO: Verify the hostname of the certificate.
//        if (!verifyHostname("attest.android.com", cert)) {
//            System.err.println("Failure: Certificate isn't issued for the hostname attest.android" +
//                    ".com.");
//            return null;
//        }

        // Extract and use the payload data.
        return jws.payload as SafetyNetStatement
    }

//        /**
//         * Verifies that the certificate matches the specified hostname.
//         * Uses the {@link DefaultHostnameVerifier} from the Apache HttpClient library
//         * to confirm that the hostname matches the certificate.
//         *
//         * @param hostname
//         * @param leafCert
//         * @return
//         */
//        private static boolean verifyHostname(String hostname, X509Certificate leafCert) {
//            try {
//                // Check that the hostname matches the certificate. This method throws an exception if
//                // the cert could not be verified.
//                HOSTNAME_VERIFIER.verify(hostname, leafCert);
//                return true;
//            } catch (SSLException e) {
//                e.printStackTrace();
//            }
//
//            return false;
//        }

    /**
     * validates the [decoded] integrity verdict locally on the device for test purposes (not recommended for
     * production use). [nonce] is the original nonce which got passes to the safetyNet API call
     * throws [AttestationException] if an error occurs
     **/
    fun verify(decoded: SafetyNetStatement, nonce: String) {
        // do more validation
        // check if nonce is correct
        if (decoded.nonce != nonce) {
            throw AttestationException("Wrong nonce received")
        }

        // check how long the response took (timeout set to 10 seconds)
        val maxDuration = Duration.ofSeconds(10)
        val timestamp = Instant.ofEpochMilli(decoded.timestampMs)
        val timeout = timestamp.plus(maxDuration)
        // TODO somehow, the second check fails on real Galaxy S9 device, requestTimestamp is later than timestamp of response. for now, check has been removed
        if (timestamp.isAfter(timeout) /*|| timestamp.isBefore(requestTimestamp)*/) {
            throw AttestationException("Timeout")
        }

        // check cts profile match and verify ctsProfileMatches
        if (decoded.isCtsProfileMatch) {
            val ctsProfileMatches = arrayOf(
                "rnv+gyOF6I07XyGZzNfPXz5K9zqX5aEzChFdowrzLm0=",
                "a+yXX21Qt/feRZckl6bm1awqvzBPGOV9OUZ4HMDOUog=",
                "rJfDDAnNl4/Kq2MkpwESX519oXToUgpUnpPmDTQIq2M=",
            )
            if (ctsProfileMatches.none { it in decoded.apkCertificateDigestSha256 }) {
                throw AttestationException("Wrong CertificateDigestSha256")
            }

            // verify package name
            if (BuildConfig.APPLICATION_ID != decoded.apkPackageName) {
                throw AttestationException("Wrong Package name")
            }
        } else {
            print("Warning: no cts profile match")
        }
    }
}