package com.henrikherzig.playintegritychecker.attestation.safetynet;

import androidx.annotation.Nullable;

import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.util.Key;
import com.google.common.io.BaseEncoding;
import com.henrikherzig.playintegritychecker.attestation.Statement;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * A statement returned by the SafetyNet Attestation API.
 */
public class SafetyNetStatement extends JsonWebSignature.Payload implements Statement {
    /**
     * Embedded nonce sent as part of the request.
     * should be unique for each request and server should check if nonce belongs to this request
     */
    @Key
    private String nonce;

    /**
     * Timestamp of the request.
     * should be checked by server so request cant be collected by attacker and sent back to server
     * later on
     */
    @Key
    private long timestampMs;

    /**
     * Package name of the APK that submitted this request.
     * should be checked by the server if it matches [BuildConfig.APPLICATION_ID]
     */
    @Key
    private String apkPackageName;

    /**
     * Digest of certificate of the APK that submitted this request.
     * cts profile match should be checked by server
     */
    @Key
    private String[] apkCertificateDigestSha256;

    /**
     * Digest of the APK that submitted this request.
     */
    @Key
    private String apkDigestSha256;

    /**
     * The device passed CTS and matches a known profile.
     */
    @Key
    private boolean ctsProfileMatch;

    /**
     * The device has passed a basic integrity test, but the CTS profile could not be verified.
     */
    @Key
    private boolean basicIntegrity;

    /**
     * Types of measurements that contributed to this response.
     */
    @Key
    private String evaluationType;

    //TODO parse advice field, and error (and show it in error UI element)

    public SafetyNetStatement() {
    }

    public SafetyNetStatement(String nonce, long timestampMs, @Nullable String apkPackageName, String[] apkCertificateDigestSha256, @Nullable String apkDigestSha256, boolean ctsProfileMatch, boolean basicIntegrity, String evaluationType) {
        this.nonce = nonce;
        this.timestampMs = timestampMs;
        this.apkPackageName = apkPackageName;
        this.apkCertificateDigestSha256 = apkCertificateDigestSha256;
        this.apkDigestSha256 = apkDigestSha256;
        this.ctsProfileMatch = ctsProfileMatch;
        this.basicIntegrity = basicIntegrity;
        this.evaluationType = evaluationType;
    }

    public String getNonce() {
        return new String(BaseEncoding.base64().decode(nonce), StandardCharsets.UTF_8);
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public String getApkPackageName() {
        return String.valueOf(apkPackageName);
    }

    public String getApkDigestSha256() {
        return String.valueOf(apkDigestSha256);
    }

    public List<String> getApkCertificateDigestSha256() {
        return Arrays.asList(apkCertificateDigestSha256);
    }

    public boolean isCtsProfileMatch() {
        return ctsProfileMatch;
    }

    public boolean hasBasicIntegrity() {
        return basicIntegrity;
    }

    public boolean hasBasicEvaluationType() {
        return evaluationType.contains("BASIC");
    }

    public boolean hasHardwareBackedEvaluationType() {
        return evaluationType.contains("HARDWARE_BACKED");
    }

    public String integrityType() {
        return evaluationType;
    }
}