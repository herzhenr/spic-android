package com.henrikherzig.playintegritychecker.attestation

import com.google.api.client.util.Key

/**
 * A statement returned by the Play Integrity API.
 */
class PlayIntegrityStatement : Statement {
    /**
     * Details about the request
     */
    @Key
    val requestDetails: RequestDetails? = null

    /**
     * Details about the integrity of the app
     */
    @Key
    val appIntegrity: AppIntegrity? = null

    /**
     * Details about the device integrity
     */
    @Key
    val deviceIntegrity: DeviceIntegrity? = null

    /**
     * Details about the account (licensing)
     */
    @Key
    val accountDetails: AccountDetails? = null
}

class RequestDetails {
    /**
     * Request package name of the APK that submitted this request.
     */
    @Key
    val requestPackageName: String? = null

    /**
     * Timestamp of the request.
     * should be checked by server so request cant be collected by attacker and sent back to server
     * later on
     */
    @Key
    val timestampMillis: Long? = null

    /**
     * Embedded nonce sent as part of the request.
     * should be unique for each request and server should check if nonce belongs to this request
     */
    @Key
    val nonce: String? = null
}

class AppIntegrity {
    /**
     * general app recognition verdict. If this evaluates to 'UNEVALUATED', no further infos are
     * provided in the response, so other fields are empty
     */
    @Key
    val appRecognitionVerdict: String? = null

    /**
     * Package name of the APK that submitted this request.
     * should be checked by the server if it matches [BuildConfig.APPLICATION_ID]
     */
    @Key
    val packageName: String? = null

    /**
     * Digest of certificate of the APK that submitted this request.
     * cts profile match should be checked by server
     */
    @Key
    val certificateSha256Digest: ArrayList<String> = ArrayList()

    /**
     * versionCode of appIntegrity
     */
    @Key
    val versionCode: String? = null
}

class DeviceIntegrity {
    /**
     * deviceRecognitionVerdict. Types of measurements that contributed to this response.
     */
    @Key
    val deviceRecognitionVerdict: ArrayList<String> = ArrayList()

    /**
     * Details about the recent device activity
     */
    @Key
    val recentDeviceActivity: RecentDeviceActivity? = null
}

class RecentDeviceActivity {
    /**
     * deviceActivityLevel. Tells you how many times your app requested an integrity token
     * on a specific device in the last hour.
     */
    @Key
    val deviceActivityLevel: String? = null
}

class AccountDetails {
    /**
     * licensing of the app (only through google play distributed apps are licensed here)
     */
    @Key
    val appLicensingVerdict: String? = null
}
